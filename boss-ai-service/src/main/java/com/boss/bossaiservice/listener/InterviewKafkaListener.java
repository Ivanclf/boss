package com.boss.bossaiservice.listener;

import com.boss.bosscommon.clients.ChatsClient;
import com.boss.bosscommon.constant.ChatConstant;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.boss.bosscommon.constant.AIUidConstant.AI_UID;

@Component
@Slf4j
public class InterviewKafkaListener {

    @Resource
    private ChatClient chatClient;
    @Resource
    private ChatsClient chatsClient;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private ThreadPoolExecutor interviewTaskExecutor;
    @Value("${kafka.topics.retry-topic}")
    private String retryTopic;

    @KafkaListener(topics = "${kafka.topics.interview-topic}", groupId = "${spring.application.name}-group")
    public void listenInterviewMessages(ChatMessage chatMessage,
                                        Acknowledgment acknowledgment) {
        log.info("收到消息: {}", chatMessage.getMessage());

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                StringBuilder aiResponse = new StringBuilder();
                chatClient.prompt()
                        .user(chatMessage.getMessage())
                        .stream()
                        .content()
                        .subscribe(aiResponse::append);

                ChatMessage aiChatMessage = new ChatMessage();
                aiChatMessage.setFromUid(AI_UID);
                aiChatMessage.setToUid(chatMessage.getFromUid());
                aiChatMessage.setMessage(aiResponse.toString());

                chatsClient.save(aiChatMessage, ChatConstant.CHAT_ARTIFICIAL_INTELLIGENT);

                acknowledgment.acknowledge();
            } catch (Exception e) {
                log.error("处理ai信息时发生错误", e);
                kafkaTemplate.send(retryTopic, chatMessage);
            }
        }, interviewTaskExecutor);

        future.orTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("服务器繁忙，请稍后再试", ex);
                    return null;
                });
    }
}