package com.video.service;

import com.video.videoserver.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class MqProducerService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送视频处理消息
     * @param msg 消息内容（视频文件信息、id等）
     */
    public void sendVideoMsg(String msg) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VIDEO_EXCHANGE,
                "video.file.upload",
                msg
        );
        System.out.println("消息发送成功：" + msg);
    }
}