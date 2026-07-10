package com.video.module.user.controller;

import com.video.common.result.PageResult;
import com.video.common.result.Result;
import com.video.common.security.UserContext;
import com.video.module.user.dto.LoginDTO;
import com.video.module.user.dto.PasswordDTO;
import com.video.module.user.dto.UserQueryDTO;
import com.video.module.user.dto.UserSaveDTO;
import com.video.module.user.entity.User;
import com.video.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户/登录相关接口
 * 前端 axios baseURL = '/api'，所以这里不用再带 /api
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 登录：POST /api/login */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    /** 用户列表（不分页）：GET /api/user/list */
    @GetMapping("/user/list")
    public Result<List<?>> userList() {
        return Result.success(userService.list());
    }

    /** 用户列表（分页）：GET /api/user/page */
    @GetMapping("/user/page")
    public Result<PageResult<User>> userPage(@Valid UserQueryDTO query) {
        return Result.success(userService.page(query));
    }

    /** 获取当前用户信息 */
    @GetMapping("/user/current")
    public Result<?> currentUser() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(userService.getById(userId));
    }

    /** 新增/编辑用户 */
    @PostMapping("/user/save")
    public Result<?> saveUser(@RequestBody @Valid UserSaveDTO dto) {
        return Result.success("保存成功", userService.save(dto));
    }

    /** 修改密码 */
    @PutMapping("/user/password")
    public Result<?> updatePassword(@RequestBody @Valid PasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success("密码修改成功", null);
    }

    /** 禁用（逻辑删除）用户 */
    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }

    /** 切换用户状态（启用/禁用） */
    @PutMapping("/user/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.success("状态已更新", null);
    }
}
