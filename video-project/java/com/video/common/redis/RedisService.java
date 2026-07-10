package com.video.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 自增
     *
     * @param key 键
     * @return 增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增（指定步长）
     *
     * @param key  键
     * @param delta 步长
     * @return 增后的值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减
     *
     * @param key 键
     * @return 减后的值
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * Token 缓存键前缀
     */
    private static final String TOKEN_PREFIX = "token:";

    /**
     * 缓存 Token
     *
     * @param userId 用户ID
     * @param token  JWT Token
     * @param expire 过期时间（秒）
     */
    public void cacheToken(Long userId, String token, long expire) {
        String key = TOKEN_PREFIX + userId;
        set(key, token, expire, TimeUnit.SECONDS);
        log.debug("缓存用户 Token: userId={}, expire={}s", userId, expire);
    }

    /**
     * 获取缓存的 Token
     *
     * @param userId 用户ID
     * @return Token
     */
    public String getToken(Long userId) {
        return get(TOKEN_PREFIX + userId);
    }

    /**
     * 删除缓存的 Token（退出登录）
     *
     * @param userId 用户ID
     */
    public void deleteToken(Long userId) {
        delete(TOKEN_PREFIX + userId);
        log.debug("删除用户 Token 缓存: userId={}", userId);
    }

    /**
     * 验证 Token 是否与缓存一致（防止多点登录）
     *
     * @param userId 用户ID
     * @param token  待验证 Token
     * @return 是否一致
     */
    public boolean validateToken(Long userId, String token) {
        String cachedToken = getToken(userId);
        return cachedToken != null && cachedToken.equals(token);
    }

    /**
     * 用户信息缓存键前缀
     */
    private static final String USER_PREFIX = "user:";

    /**
     * 缓存用户信息
     *
     * @param userId 用户ID
     * @param userInfo 用户信息 JSON
     */
    public void cacheUserInfo(Long userId, String userInfo) {
        String key = USER_PREFIX + userId;
        set(key, userInfo, 3600, TimeUnit.SECONDS); // 缓存 1 小时
    }

    /**
     * 获取用户信息缓存
     *
     * @param userId 用户ID
     * @return 用户信息 JSON
     */
    public String getUserInfo(Long userId) {
        return get(USER_PREFIX + userId);
    }

    /**
     * 删除用户信息缓存
     *
     * @param userId 用户ID
     */
    public void deleteUserInfo(Long userId) {
        delete(USER_PREFIX + userId);
    }
}