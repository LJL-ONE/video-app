package com.video.videoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.video.videoserver.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}