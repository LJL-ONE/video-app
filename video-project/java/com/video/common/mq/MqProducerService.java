package com.video.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 消息生产者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqProducerService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送视频处理任务
     *
     * @param videoId 视频ID
     * @param fileKey MinIO 文件 key
     * @param action  处理动作（transcode/extract_thumbnail/delete）
     */
    public void sendVideoProcessTask(Long videoId, String fileKey, String action) {
        Map<String, Object> message = new HashMap<>(4);
        message.put("videoId", videoId);
        message.put("fileKey", fileKey);
        message.put("action", action);
        message.put("timestamp", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.VIDEO_EXCHANGE,
                RabbitMQConfig.VIDEO_ROUTING_KEY,
                message
        );
        log.info("发送视频处理任务: videoId={}, action={}", videoId, action);
    }

    /**
     * 发送视频转码完成通知
     *
     * @param videoId 视频ID
     * @param result  处理结果
     */
    public void sendVideoCompleteNotice(Long videoId, Map<String, Object> result) {
        Map<String, Object> message = new HashMap<>(result);
        message.put("videoId", videoId);
        message.put("timestamp", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.VIDEO_EXCHANGE,
                RabbitMQConfig.VIDEO_COMPLETE_ROUTING_KEY,
                message
        );
        log.info("发送视频转码完成通知: videoId={}", videoId);
    }

    /**
     * 发送会议通知
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     * @param action    动作（start/end/remind）
     * @param extra     额外信息
     */
    public void sendMeetingNotice(Long meetingId, Long userId, String action, Map<String, Object> extra) {
        Map<String, Object> message = new HashMap<>(6);
        message.put("meetingId", meetingId);
        message.put("userId", userId);
        message.put("action", action);
        message.put("timestamp", LocalDateTime.now().toString());
        if (extra != null) {
            message.putAll(extra);
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MEETING_EXCHANGE,
                RabbitMQConfig.MEETING_ROUTING_KEY,
                message
        );
        log.info("发送会议通知: meetingId={}, userId={}, action={}", meetingId, userId, action);
    }

    /**
     * 发送延迟消息（需要插件 rabbitmq_delayed_message_exchange）
     * 如未安装插件，可用定时任务替代
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息内容
     * @param delayMs    延迟时间（毫秒）
     */
    public void sendDelayedMessage(String exchange, String routingKey, Object message, long delayMs) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setDelay((int) delayMs);
            return msg;
        });
        log.debug("发送延迟消息: exchange={}, routingKey={}, delayMs={}", exchange, routingKey, delayMs);
    }
}