package com.video.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.video.common.exception.BusinessException;
import com.video.common.result.PageResult;
import com.video.common.result.ResultCode;
import com.video.common.security.JwtUtil;
import com.video.common.security.UserContext;
import com.video.module.user.dto.LoginDTO;
import com.video.module.user.dto.PasswordDTO;
import com.video.module.user.dto.UserQueryDTO;
import com.video.module.user.dto.UserSaveDTO;
import com.video.module.user.entity.User;
import com.video.module.user.mapper.UserMapper;
import com.video.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        // 使用 BCrypt 验证密码
        if (!StringUtils.hasText(user.getPassword()) || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 使用 JWT 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>(4);
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return data;
    }

    @Override
    public List<User> list() {
        return userMapper.selectList(null);
    }

    @Override
    public PageResult<User> page(UserQueryDTO query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 搜索关键字（账号或昵称）
        if (query.getKeyword() != null) {
            wrapper.and(w -> w.like(User::getUsername, query.getKeyword())
                    .or().like(User::getNickname, query.getKeyword()));
        }

        // 角色筛选
        if (query.getRole() != null) {
            wrapper.eq(User::getRole, query.getRole());
        }

        // 状态筛选
        if (query.getDeleted() != null) {
            wrapper.eq(User::getDeleted, query.getDeleted());
        }

        // 排序
        if (query.getOrderBy() != null) {
            wrapper.orderBy(true, query.getIsAsc(), User::getCreateTime);
        } else {
            wrapper.orderByDesc(User::getCreateTime);
        }

        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<User> result = userMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(UserSaveDTO dto) {
        if (dto.getId() == null) {
            // 新增：校验账号唯一
            if (getByUsername(dto.getUsername()) != null) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
            }
            User u = new User();
            BeanUtils.copyProperties(dto, u);
            // 使用 BCrypt 加密默认密码
            u.setPassword(passwordEncoder.encode("123456"));
            userMapper.insert(u);
            return u;
        } else {
            User u = userMapper.selectById(dto.getId());
            if (u == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }
            // 修改账号名校验唯一
            if (!u.getUsername().equals(dto.getUsername())
                    && getByUsername(dto.getUsername()) != null) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
            }
            u.setUsername(dto.getUsername());
            u.setNickname(dto.getNickname());
            u.setRole(dto.getRole());
            userMapper.updateById(u);
            return u;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 逻辑删除
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 验证旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        // 加密新密码并更新
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户 {} 修改密码成功", user.getUsername());
    }

    @Override
    public void toggleStatus(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        int newVal = Integer.valueOf(1).equals(u.getDeleted()) ? 0 : 1;
        LambdaUpdateWrapper<User> uw = new LambdaUpdateWrapper<>();
        uw.eq(User::getId, id).set(User::getDeleted, newVal);
        userMapper.update(null, uw);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
