package com.boss.bossaiservice.service;

import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageInfo;
import reactor.core.publisher.Flux;

public interface InterviewService {
    Flux<String> start(String token);

    Flux<String> question(ChatMessage chatMessage, String token);

    PageInfo<ChatRecordVO> getHistory(String token, int pageNum, int pageSize);
}