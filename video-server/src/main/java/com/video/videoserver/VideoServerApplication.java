package com.video.videoserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@com.video.videoserver.EnableDiscoveryClient
// 扫描mapper接口包
@MapperScan("com.video.videoserver.mapper")
public class VideoServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoServerApplication.class, args);
    }
}