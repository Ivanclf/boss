package com.boss.bosschatservice.service;

import com.boss.bosscommon.pojo.dto.ChatRecordDTO;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.github.pagehelper.PageInfo;

public interface ConversationService {
    PageInfo<ChatLatestListVO> getConversationList(String token, int pageNum, int pageSize);

    PageInfo<ChatRecordDTO> getChatRecord(String token, int pageNum, int pageSize, Long uid);

    /**
     * 保存聊天记录
     */
    void saveChatRecord(Long fromUid, Long toUid, String message);
}
