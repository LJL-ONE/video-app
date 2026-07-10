package com.video.videoserver.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // 交换机名称
    public static final String VIDEO_EXCHANGE = "video_topic_exchange";
    // 队列名称
    public static final String VIDEO_QUEUE = "video_process_queue";
    // 路由key
    public static final String VIDEO_ROUTING_KEY = "video.file.*";

    // Topic交换机
    @Bean
    public TopicExchange videoTopicExchange() {
        return ExchangeBuilder.topicExchange(VIDEO_EXCHANGE).durable(true).build();
    }

    // 持久化队列
    @Bean
    public Queue videoProcessQueue() {
        return QueueBuilder.durable(VIDEO_QUEUE).build();
    }

    // 队列绑定交换机
    @Bean
    public Binding videoQueueBinding(Queue videoProcessQueue, TopicExchange videoTopicExchange) {
        return BindingBuilder
                .bind(videoProcessQueue)
                .to(videoTopicExchange)
                .with(VIDEO_ROUTING_KEY);
    }
}