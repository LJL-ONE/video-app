package com.video.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 签名密钥
     */
    private String secret;

    /**
     * 过期时间（秒）
     */
    private Long expire;

    /**
     * 请求头名称
     */
    private String header;

    /**
     * Token 前缀
     */
    private String prefix;
}