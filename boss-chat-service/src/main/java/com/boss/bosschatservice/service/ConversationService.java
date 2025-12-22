package com.boss.bosschatservice.service;

import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ConversationService {
    PageInfo<ChatLatestListVO> getConversationList(String token, int pageNum, int pageSize);

    PageInfo<ChatRecordVO> getChatRecord(String token, int pageNum, int pageSize, Long uid);

    void saveChatRecord(Long fromUid, Long toUid, String message, Integer role) throws JsonProcessingException;

    void saveChatRecordToDatabase(Long fromUid, Long toUid, String message, Integer role);

    PageInfo<ChatRecordVO> getAiChatRecord(Long uid, int pageNum, int pageSize);

    List<ChatMessageElasticsearchDTO> queryForElasticsearch();
}
