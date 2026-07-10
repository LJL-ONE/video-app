package com.video.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO 配置：连接客户端 + 启动时自动创建桶
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /** MinIO 服务地址 */
    private String endpoint;
    /** 账号 */
    private String accessKey;
    /** 密码 */
    private String secretKey;
    /** 桶名 */
    private String bucket;
    /** 公开访问前缀 */
    private String publicBaseUrl;

    @Bean
    public MinioClient minioClient() {
        log.info("初始化 MinIO 客户端 endpoint={}, bucket={}", endpoint, bucket);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 启动时检查桶是否存在，不存在则创建
     */
    @PostConstruct
    public void initBucket() {
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO 桶 {} 不存在，已自动创建", bucket);
            } else {
                log.info("MinIO 桶 {} 已存在，跳过创建", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO 初始化失败，请检查 MinIO 服务是否启动以及配置是否正确: {}", e.getMessage());
        }
    }
}
