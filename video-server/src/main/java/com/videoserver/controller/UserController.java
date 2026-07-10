package com.videoserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.video.videoserver.entity.User;
import com.video.videoserver.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 分页查询
    @GetMapping("/page")
    public Page<User> pageList(@RequestParam(defaultValue = "1") Long pageNum,
                               @RequestParam(defaultValue = "10") Long pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userService.page(page, new LambdaQueryWrapper<User>());
        return result;
    }

    // 查询全部用户
    @GetMapping("/list")
    public List<User> listAll() {
        return userService.list();
    }
}