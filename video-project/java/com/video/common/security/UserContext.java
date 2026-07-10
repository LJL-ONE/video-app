package com.video.common.security;

import lombok.Data;

/**
 * 用户上下文信息（存储在 ThreadLocal）
 */
@Data
public class UserContext {

    private Long userId;
    private String username;
    private String role;

    /**
     * ThreadLocal 存储当前用户
     */
    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(UserContext user) {
        CONTEXT.set(user);
    }

    /**
     * 获取当前用户
     */
    public static UserContext getCurrentUser() {
        return CONTEXT.get();
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        UserContext ctx = getCurrentUser();
        return ctx == null ? null : ctx.getUserId();
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserContext ctx = getCurrentUser();
        return ctx == null ? null : ctx.getUsername();
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin() {
        UserContext ctx = getCurrentUser();
        return ctx != null && "管理员".equals(ctx.getRole());
    }
}