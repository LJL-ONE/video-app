package com.video.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 存入缓存（无过期时间）
    public void set(String key, Object val) {
        redisTemplate.opsForValue().set(key, val);
    }

    // 存入缓存（带过期时间，单位秒）
    public void setExpire(String key, Object val, long second) {
        redisTemplate.opsForValue().set(key, val, second, TimeUnit.SECONDS);
    }

    // 获取缓存
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除缓存
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}