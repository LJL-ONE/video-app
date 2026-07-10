package com.video.common.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 消息转换器（JSON）
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * 监听器容器工厂配置
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ============ 视频处理队列 ============

    /**
     * 视频处理队列
     */
    public static final String VIDEO_QUEUE = "video.process.queue";

    /**
     * 视频处理交换机
     */
    public static final String VIDEO_EXCHANGE = "video.process.exchange";

    /**
     * 视频处理路由键
     */
    public static final String VIDEO_ROUTING_KEY = "video.process";

    @Bean
    public Queue videoQueue() {
        return QueueBuilder.durable(VIDEO_QUEUE)
                .withArgument("x-message-ttl", 60000) // 消息存活时间 60秒
                .build();
    }

    @Bean
    public DirectExchange videoExchange() {
        return new DirectExchange(VIDEO_EXCHANGE, true, false);
    }

    @Bean
    public Binding videoBinding() {
        return BindingBuilder.bind(videoQueue())
                .to(videoExchange())
                .with(VIDEO_ROUTING_KEY);
    }

    // ============ 视频转码完成队列 ============

    public static final String VIDEO_COMPLETE_QUEUE = "video.complete.queue";
    public static final String VIDEO_COMPLETE_ROUTING_KEY = "video.complete";

    @Bean
    public Queue videoCompleteQueue() {
        return QueueBuilder.durable(VIDEO_COMPLETE_QUEUE).build();
    }

    @Bean
    public Binding videoCompleteBinding() {
        return BindingBuilder.bind(videoCompleteQueue())
                .to(videoExchange())
                .with(VIDEO_COMPLETE_ROUTING_KEY);
    }

    // ============ 会议通知队列 ============

    public static final String MEETING_QUEUE = "meeting.notify.queue";
    public static final String MEETING_EXCHANGE = "meeting.notify.exchange";
    public static final String MEETING_ROUTING_KEY = "meeting.notify";

    @Bean
    public Queue meetingQueue() {
        return QueueBuilder.durable(MEETING_QUEUE).build();
    }

    @Bean
    public DirectExchange meetingExchange() {
        return new DirectExchange(MEETING_EXCHANGE, true, false);
    }

    @Bean
    public Binding meetingBinding() {
        return BindingBuilder.bind(meetingQueue())
                .to(meetingExchange())
                .with(MEETING_ROUTING_KEY);
    }
}