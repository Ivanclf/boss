package com.boss.bosschatservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import com.boss.bosschatservice.mapper.ConversationMapper;
import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.dto.ChatRecordDTO;
import com.boss.bosscommon.pojo.entity.ChatRecord;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public PageInfo<ChatRecordDTO> getChatRecord(String token, int pageNum, int pageSize, Long uid) {
        PageHelper.startPage(pageNum, pageSize);

        Long fromUid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");

        List<ChatRecord> chatRecords = conversationMapper.getChatByUids(fromUid, uid);

        List<ChatRecordDTO> chatRecordDTOS = chatRecords.stream().map(chatRecord -> {
            return BeanUtil.copyProperties(chatRecord, ChatRecordDTO.class);
        }).toList();

        return new PageInfo<>(chatRecordDTOS);
    }
    /**
     * 保存聊天记录
     */
    @Override
    public void saveChatRecord(Long fromUid, Long toUid, String message) {
        ChatRecord chatRecord = ChatRecord.builder()
                .fromUid(fromUid)
                .toUid(toUid)
                .context(message)
                .status(1)
                .createTime(java.time.LocalDateTime.now())
                .deleted(0)
                .build();
        conversationMapper.insertChatRecord(chatRecord);
    }
}
