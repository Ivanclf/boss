package com.boss.bosschatservice.listener;

import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.boss.bosscommon.constant.AIUidConstant.AI_UID;
import static com.boss.bosscommon.constant.ChatConstant.CHAT_ARTIFICIAL_INTELLIGENT;
import static com.boss.bosscommon.constant.ChatConstant.CHAT_HUMAN_RESOURCES;

@Component
@Slf4j
public class ChatRecordKafkaListener {

    @Resource
    private ConversationService conversationService;

    @KafkaListener(topics = "${kafka.topics.chat-record-topic}", groupId = "${spring.application.name}-group")
    public void listenChatMessages(ChatMessage chatMessage, Acknowledgment acknowledgment) {
        log.info("接收到聊天消息: {}", chatMessage);
        
        try {
            Integer role = CHAT_HUMAN_RESOURCES;
            if (chatMessage.getFromUid() != null && chatMessage.getFromUid().equals(AI_UID)) {
                role = CHAT_ARTIFICIAL_INTELLIGENT;
            }

            conversationService.saveChatRecordToDatabase(
                    chatMessage.getFromUid(),
                    chatMessage.getToUid(),
                    chatMessage.getMessage(),
                    role
            );

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理聊天消息时发生错误: ", e);
        }
    }
}