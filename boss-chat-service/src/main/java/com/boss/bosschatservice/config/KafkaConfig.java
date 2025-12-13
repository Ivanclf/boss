package com.boss.bosschatservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.chat-record-topic}")
    private String chatRecordTopic;
    @Value("${kafka.topics.retry-topic}")
    private String retryTopic;
    @Value("${kafka.topics.dead-letter-topic}")
    private String deadLetterTopic;
    @Value("${kafka.partitions}")
    private int partitions;
    @Value("${kafka.replicas}")
    private short replicas;

    @Bean
    public RecordMessageConverter converter() {
        return new JsonMessageConverter();
    }

    @Bean
    public NewTopic interviewTopic() {
        return TopicBuilder.name(chatRecordTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic retryTopic() {
        return TopicBuilder.name(retryTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        // 创建死信队列恢复器
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template,
                        (record, exception) -> {
                            // 根据异常类型决定发送到重试队列还是死信队列
                            if (exception.getCause() instanceof RuntimeException) {
                                return new org.apache.kafka.common.TopicPartition(chatRecordTopic, record.partition());
                            }
                            return new org.apache.kafka.common.TopicPartition(deadLetterTopic, record.partition());
                        });

        // 配置错误处理器：重试3次，间隔1秒
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 3L)
        );

        // 添加可恢复的异常类型
        errorHandler.addNotRetryableExceptions(NullPointerException.class);

        return errorHandler;
    }
}