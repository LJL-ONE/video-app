package com.video.common.controller;

import com.video.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用健康检查 / 服务信息
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>(4);
        data.put("status", "UP");
        data.put("service", "video-server");
        data.put("time", LocalDateTime.now());
        return Result.success(data);
    }
}
