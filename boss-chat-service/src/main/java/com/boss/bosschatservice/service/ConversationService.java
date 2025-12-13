package com.boss.bosschatservice.service;

import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageInfo;

public interface ConversationService {
    PageInfo<ChatLatestListVO> getConversationList(String token, int pageNum, int pageSize);

    PageInfo<ChatRecordVO> getChatRecord(String token, int pageNum, int pageSize, Long uid);

    void saveChatRecord(Long fromUid, Long toUid, String message, Integer role);

    void saveChatRecordToDatabase(Long fromUid, Long toUid, String message, Integer role);

    PageInfo<ChatRecordVO> getAiChatRecord(Long uid, int pageNum, int pageSize);
}
