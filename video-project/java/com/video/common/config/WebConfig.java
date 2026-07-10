package com.video.common.config;

import com.video.common.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/login",               // 登录接口
                        "/health",              // 健康检查
                        "/error",               // 错误页面
                        "/doc.html",            // Knife4j 文档页面
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",          // Knife4j 静态资源
                        "/favicon.ico"
                );
    }
}