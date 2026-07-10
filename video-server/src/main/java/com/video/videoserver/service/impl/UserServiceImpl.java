package com.video.videoserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.video.videoserver.entity.User;
import com.video.videoserver.mapper.UserMapper;
import com.video.videoserver.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}