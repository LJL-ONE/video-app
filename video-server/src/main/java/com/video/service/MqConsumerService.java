package com.video.videoserver.service;

import com.video.videoserver.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MqConsumerService {

    // 监听指定队列，有消息自动执行
    @RabbitListener(queues = RabbitMqConfig.VIDEO_QUEUE)
    public void consumeVideoMsg(String message) {
        System.out.println("收到视频任务消息：" + message);
        // 这里写业务逻辑：视频转码、解析封面、存储等
    }
}