package com.boss.bossaiservice.service.Impl;

import com.boss.bossaiservice.service.InterviewService;
import com.boss.bosscommon.clients.ChatsClient;
import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.boss.bosscommon.constant.AIUidConstant.AI_UID;
import static com.boss.bosscommon.constant.ChatConstant.CHAT_HUMAN_RESOURCES;
import static com.boss.bosscommon.constant.RedisConstant.INTERVIEW_SESSION_KEY;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;

@Service
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ChatClient chatClient;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private ChatsClient chatsClient;
    @Resource
    private ThreadPoolExecutor interviewTaskExecutor;

    @Value("${kafka.topics.interview-topic}")
    private String interviewTopic;

    @Override
    public Flux<String> start(String token) {
        String sessionId = UUID.randomUUID().toString();
        Long userUid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");

        if(userUid == null) {
            throw new clientException("用户未登录");
        }

        stringRedisTemplate.opsForValue().set(INTERVIEW_SESSION_KEY + sessionId, userUid.toString());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUid(userUid);
        chatMessage.setToUid(AI_UID);
        chatMessage.setMessage("可以开始了");

        Message<ChatMessage> kafkaChatMessage = MessageBuilder
                .withPayload(chatMessage)
                .setHeader(KafkaHeaders.TOPIC, interviewTopic)
                .setHeader("sessionId", sessionId)
                .setHeader("token", token)
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return kafkaTemplate.send(kafkaChatMessage).get();
                    } catch (Exception e) {
                        log.error("消息发送到消息队列失败");
                        throw new RuntimeException();
                    }
                }, interviewTaskExecutor);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("消息队列处理时发生异常", throwable);
            } else {
                log.info("已成功返回数据");
            }
        });

        return chatClient.prompt()
                .user("可以开始了")
                .stream().content();
    }

    @Override
    public Flux<String> question(ChatMessage userChatMessage, String token) {
        Long userUid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");
        userChatMessage.setFromUid(userUid);
        userChatMessage.setToUid(AI_UID);

        chatsClient.save(userChatMessage, CHAT_HUMAN_RESOURCES);

        Message<ChatMessage> kafkaMessage = MessageBuilder
                .withPayload(userChatMessage)
                .setHeader(KafkaHeaders.TOPIC, interviewTopic)
                .setHeader("token", token)
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return kafkaTemplate.send(kafkaMessage).get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, interviewTaskExecutor);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("消息队列处理时发生异常", throwable);
            } else {
                log.info("已成功返回数据");
            }
        });

        return chatClient.prompt()
                .user(userChatMessage.getMessage())
                .stream().content();
    }

    @Override
    public PageInfo<ChatRecordVO> getHistory(String token, int pageNum, int pageSize) {
        Long uid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");
        return chatsClient.getAiChatRecord(uid, pageNum, pageSize);
    }
}