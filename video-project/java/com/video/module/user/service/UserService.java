package com.video.module.user.service;

import com.video.common.exception.BusinessException;
import com.video.common.result.PageResult;
import com.video.common.result.ResultCode;
import com.video.module.user.dto.LoginDTO;
import com.video.module.user.dto.PasswordDTO;
import com.video.module.user.dto.UserQueryDTO;
import com.video.module.user.dto.UserSaveDTO;
import com.video.module.user.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    /**
     * 登录校验，返回 token + 用户信息
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * 查询用户列表（不分页，全量供前端筛选分页）
     */
    List<User> list();

    /**
     * 分页查询用户列表
     */
    PageResult<User> page(UserQueryDTO query);

    /**
     * 新增 / 编辑用户
     */
    User save(UserSaveDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(PasswordDTO dto);

    /**
     * 逻辑删除（禁用）用户
     */
    void delete(Long id);

    /**
     * 切换用户状态
     */
    void toggleStatus(Long id);

    /**
     * 内部使用：根据 username 查询
     */
    User getByUsername(String username);

    /**
     * 内部使用：根据 ID 查询
     */
    User getById(Long id);

    /**
     * 业务异常工具
     */
    default BusinessException biz(ResultCode rc) {
        return new BusinessException(rc);
    }
}
