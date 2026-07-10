package com.video.common.mq;

import com.video.module.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ 消息消费者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqConsumerService {

    private final VideoService videoService;

    /**
     * 消费视频处理任务
     */
    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE)
    public void handleVideoProcess(Map<String, Object> message) {
        log.info("接收视频处理任务: {}", message);

        Long videoId = Long.valueOf(message.get("videoId").toString());
        String fileKey = message.get("fileKey").toString();
        String action = message.get("action").toString();

        try {
            switch (action) {
                case "transcode":
                    // 视频转码处理（实际项目中需要调用 FFmpeg）
                    log.info("开始视频转码: videoId={}, fileKey={}", videoId, fileKey);
                    // TODO: 调用 FFmpeg 进行转码
                    break;

                case "extract_thumbnail":
                    // 提取视频封面（实际项目中需要调用 FFmpeg）
                    log.info("提取视频封面: videoId={}, fileKey={}", videoId, fileKey);
                    // TODO: 调用 FFmpeg 提取封面
                    break;

                case "delete":
                    // 删除视频文件
                    log.info("删除视频文件: videoId={}, fileKey={}", videoId, fileKey);
                    videoService.delete(videoId);
                    break;

                default:
                    log.warn("未知视频处理动作: {}", action);
            }
        } catch (Exception e) {
            log.error("视频处理失败: videoId={}, action={}", videoId, action, e);
            // 消息会根据配置自动重试
        }
    }

    /**
     * 消费视频转码完成通知
     */
    @RabbitListener(queues = RabbitMQConfig.VIDEO_COMPLETE_QUEUE)
    public void handleVideoComplete(Map<String, Object> message) {
        log.info("接收视频转码完成通知: {}", message);

        Long videoId = Long.valueOf(message.get("videoId").toString());

        try {
            // 更新视频状态
            videoService.updateStatus(videoId, "已发布");
            log.info("视频发布成功: videoId={}", videoId);
        } catch (Exception e) {
            log.error("更新视频状态失败: videoId={}", videoId, e);
        }
    }

    /**
     * 消费会议通知
     */
    @RabbitListener(queues = RabbitMQConfig.MEETING_QUEUE)
    public void handleMeetingNotice(Map<String, Object> message) {
        log.info("接收会议通知: {}", message);

        Long meetingId = Long.valueOf(message.get("meetingId").toString());
        Long userId = Long.valueOf(message.get("userId").toString());
        String action = message.get("action").toString();

        try {
            switch (action) {
                case "start":
                    // 会议开始通知
                    log.info("会议开始: meetingId={}, userId={}", meetingId, userId);
                    // TODO: 发送 WebSocket 或邮件通知
                    break;

                case "end":
                    // 会议结束通知
                    log.info("会议结束: meetingId={}, userId={}", meetingId, userId);
                    // TODO: 发送 WebSocket 或邮件通知
                    break;

                case "remind":
                    // 会议提醒
                    log.info("会议提醒: meetingId={}, userId={}", meetingId, userId);
                    // TODO: 发送 WebSocket 或邮件通知
                    break;

                default:
                    log.warn("未知会议通知动作: {}", action);
            }
        } catch (Exception e) {
            log.error("处理会议通知失败: meetingId={}, userId={}", meetingId, userId, e);
        }
    }
}