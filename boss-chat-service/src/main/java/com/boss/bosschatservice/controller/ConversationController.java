package com.boss.bosschatservice.controller;

import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.pojo.dto.ChatRecordDTO;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/chat/conversation")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    @GetMapping
    public PageInfo<ChatLatestListVO> getConversationList(
            @RequestHeader("authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return conversationService.getConversationList(token, pageNum, pageSize);
    }

    @GetMapping("/{uid}")
    public PageInfo<ChatRecordDTO> getConversationRecord(
            @RequestHeader("authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @PathVariable @NotNull @Min(0L) Long uid
    ) {
        return conversationService.getChatRecord(token, pageNum, pageSize, uid);
    }
}
