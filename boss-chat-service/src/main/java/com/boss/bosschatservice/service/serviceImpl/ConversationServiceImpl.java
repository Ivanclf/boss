package com.boss.bosschatservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import com.boss.bosschatservice.mapper.ConversationMapper;
import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.entity.ChatRecord;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public PageInfo<ChatLatestListVO> getConversationList(String token, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        Long userUid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");

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

        Long fromUid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");

        List<ChatRecord> chatRecords = conversationMapper.getChatByUids(fromUid, uid);

        List<ChatRecordVO> chatRecordVOS = chatRecords.stream().map(chatRecord -> {
            return BeanUtil.copyProperties(chatRecord, ChatRecordVO.class);
        }).toList();

        return new PageInfo<>(chatRecordVOS);
    }

    @Override
    public void saveChatRecord(Long fromUid, Long toUid, String message, Integer role) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUid(fromUid);
        chatMessage.setToUid(toUid);
        chatMessage.setMessage(message);

        kafkaTemplate.send("chat-record-topic", chatMessage);
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
                .map(chatRecord -> BeanUtil.copyProperties(chatRecord, ChatRecordVO.class))
                .toList();
        
        return new PageInfo<>(chatRecordVOS);
    }
}