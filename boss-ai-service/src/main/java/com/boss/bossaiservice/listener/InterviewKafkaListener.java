package com.boss.bossaiservice.listener;

import com.boss.bosscommon.clients.ChatsClient;
import com.boss.bosscommon.constant.ChatConstant;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.boss.bosscommon.constant.AIUidConstant.AI_UID;

@Component
@Slf4j
public class InterviewKafkaListener {

    @Resource
    private ChatsClient chatsClient;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private ThreadPoolExecutor interviewTaskExecutor;
    @Value("${kafka.topics.retry-topic}")
    private String retryTopic;

    @KafkaListener(topics = "${kafka.topics.interview-topic}", groupId = "${spring.application.name}-group")
    public void listenInterviewMessages(ChatMessage chatMessage, Acknowledgment acknowledgment) {
        log.info("收到面试消息：fromUid={}, toUid={}", chatMessage.getFromUid(), chatMessage.getToUid());

        // 只处理用户消息，AI 响应由 chat-service 的监听器负责保存
        if (chatMessage.getFromUid().equals(AI_UID)) {
            acknowledgment.acknowledge();
            return;
        }

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                // 用户消息通过 Feign 客户端保存到 chat-service
                chatsClient.save(chatMessage, ChatConstant.CHAT_HUMAN_RESOURCES);
                log.info("用户消息已保存：fromUid={}, toUid={}", chatMessage.getFromUid(), chatMessage.getToUid());
                acknowledgment.acknowledge();
            } catch (Exception e) {
                log.error("保存聊天记录时发生错误", e);
                kafkaTemplate.send(retryTopic, chatMessage);
            }
        }, interviewTaskExecutor);

        future.orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("处理超时", ex);
                    return null;
                });
    }
}