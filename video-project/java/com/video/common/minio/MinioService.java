package com.video.common.minio;

import com.video.common.config.MinioConfig;
import com.video.common.exception.BusinessException;
import com.video.common.result.ResultCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 文件存储工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 上传文件
     *
     * @param file     接收到的文件
     * @param dir      桶内目录，例如 "video/"、"avatar/"
     * @return 文件名（桶内唯一 key）
     */
    public String upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.VIDEO_FILE_EMPTY);
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String key = (dir == null ? "" : dir) + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(key)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("MinIO 上传成功 bucket={} key={} size={}",
                    minioConfig.getBucket(), key, file.getSize());
            return key;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new BusinessException(ResultCode.VIDEO_UPLOAD_FAIL);
        }
    }

    /**
     * 删除文件
     */
    public void delete(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(key)
                    .build());
        } catch (Exception e) {
            log.warn("MinIO 删除失败 key={}, err={}", key, e.getMessage());
        }
    }

    /**
     * 获取公开访问 URL
     */
    public String getPublicUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        String base = minioConfig.getPublicBaseUrl();
        if (base == null || base.isEmpty()) {
            return key;
        }
        return base.endsWith("/") ? base + key : base + "/" + key;
    }

    /**
     * 获取临时预签名 URL（私有桶读）
     */
    public String getPresignedUrl(String key, int expireSeconds) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(key)
                    .method(Method.GET)
                    .build());
        } catch (Exception e) {
            log.error("生成预签名 URL 失败 key={}", key, e);
            return null;
        }
    }
}
