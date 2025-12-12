package com.boss.bosschatservice.mapper;

import com.boss.bosscommon.pojo.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper {
    List<ChatRecord> getAllRelatedChat(Long userUid);

    List<ChatRecord> getChatByUids(@Param("fromUid") Long fromUid, @Param("toUid") Long toUid);

    int insertChatRecord(ChatRecord chatRecord);
}
