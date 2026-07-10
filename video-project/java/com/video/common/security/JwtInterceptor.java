package com.video.common.security;

import com.video.common.exception.BusinessException;
import com.video.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取请求头中的 Token
        String authHeader = request.getHeader(jwtProperties.getHeader());
        if (!StringUtils.hasText(authHeader)) {
            log.warn("请求缺少 Authorization 头: {} {}", request.getMethod(), request.getRequestURI());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 去除前缀
        String token = authHeader;
        String prefix = jwtProperties.getPrefix();
        if (StringUtils.hasText(prefix) && authHeader.startsWith(prefix)) {
            token = authHeader.substring(prefix.length());
        }

        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 无效或已过期: {} {}", request.getMethod(), request.getRequestURI());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 解析用户信息并存入上下文
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        UserContext userContext = new UserContext();
        userContext.setUserId(userId);
        userContext.setUsername(username);
        userContext.setRole(role);
        UserContext.setCurrentUser(userContext);

        log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}