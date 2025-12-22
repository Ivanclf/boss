package com.boss.bosschatservice.service.serviceImpl;

import com.boss.bosschatservice.mapper.ConversationMapper;
import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.entity.ChatRecord;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.boss.bosscommon.constant.AIUidConstant.AI_UID;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;

@Service
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    @Resource
    private ConversationMapper conversationMapper;
    @Resource
    private UserClient userClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Value("${kafka.topics.chat-record-topic}")
    private String chatRecordTopic;
    
    @Override
    public PageInfo<ChatLatestListVO> getConversationList(String token, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        Long userUid = Long.valueOf((String) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid"));

        List<ChatRecord> allChatList = conversationMapper.getAllRelatedChat(userUid);

        List<ChatLatestListVO> chatLatestListVOList = allChatList.stream().map(chatRecord -> {
            return ChatLatestListVO.builder()
                    .userBasicVO(userClient.getBasicInfo(token))
                    .latestTime(chatRecord.getCreateTime())
                    .context(chatRecord.getContext())
                    .build();
        }).toList();

        return new PageInfo<>(chatLatestListVOList);
    }

    @Override
    public PageInfo<ChatRecordVO> getChatRecord(String token, int pageNum, int pageSize, Long uid) {
        PageHelper.startPage(pageNum, pageSize);

        Long fromUid = Long.valueOf((String) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid"));

        List<ChatRecord> chatRecords = conversationMapper.getChatByUids(fromUid, uid);

        List<ChatRecordVO> chatRecordVOS = chatRecords.stream().map(chatRecord -> {
            ChatRecordVO chatRecordVO = ChatRecordVO.builder()
                    .status(chatRecord.getStatus())
                    .fromUid(chatRecord.getFromUid())
                    .toUid(chatRecord.getToUid())
                    .jobUid(chatRecord.getJobUid())
                    .createTime(chatRecord.getCreateTime())
                    .context(chatRecord.getContext())
                    .build();
            return chatRecordVO;
        }).toList();

        return new PageInfo<>(chatRecordVOS);
    }

    @Override
    public void saveChatRecord(Long fromUid, Long toUid, String message, Integer role) throws JsonProcessingException {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUid(fromUid);
        chatMessage.setToUid(toUid);
        chatMessage.setMessage(message);

        kafkaTemplate.send(chatRecordTopic, objectMapper.writeValueAsString(chatMessage));
        log.info("聊天消息已发送到Kafka: fromUid={}, toUid={}, message={}", fromUid, toUid, message);
    }

    public void saveChatRecordToDatabase(Long fromUid, Long toUid, String message, Integer role) {
        ChatRecord chatRecord = ChatRecord.builder()
                .fromUid(fromUid)
                .toUid(toUid)
                .context(message)
                .status(role)
                .createTime(LocalDateTime.now())
                .deleted(0)
                .build();
        conversationMapper.insertChatRecord(chatRecord);
        log.info("聊天记录已保存到数据库: fromUid={}, toUid={}, message={}", fromUid, toUid, message);
    }

    @Override
    public PageInfo<ChatRecordVO> getAiChatRecord(Long uid, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<ChatRecord> chatRecords = conversationMapper.getChatBetweenUserAndAI(uid, AI_UID);

        List<ChatRecordVO> chatRecordVOS = chatRecords.stream()
                .map(chatRecord -> ChatRecordVO.builder()
                        .status(chatRecord.getStatus())
                        .fromUid(chatRecord.getFromUid())
                        .toUid(chatRecord.getToUid())
                        .jobUid(chatRecord.getJobUid())
                        .createTime(chatRecord.getCreateTime())
                        .context(chatRecord.getContext())
                        .build())
                .toList();
        
        return new PageInfo<>(chatRecordVOS);
    }

    @Override
    public List<ChatMessageElasticsearchDTO> queryForElasticsearch() {
        List<ChatRecord> chatRecords = conversationMapper.queryAll();
        List<ChatMessageElasticsearchDTO> results = new ArrayList<>();
        for(ChatRecord chatRecord : chatRecords) {
            ChatMessageElasticsearchDTO chatMessageElasticsearchDTO = ChatMessageElasticsearchDTO.builder()
                    .messageId(chatRecord.getId())
                    .fromUid(chatRecord.getFromUid())
                    .toUid(chatRecord.getToUid())
                    .jobUid(chatRecord.getJobUid())
                    .context(chatRecord.getContext())
                    .createTime(chatRecord.getCreateTime())
                    .build();
            results.add(chatMessageElasticsearchDTO);
        }
        return results;
    }
}