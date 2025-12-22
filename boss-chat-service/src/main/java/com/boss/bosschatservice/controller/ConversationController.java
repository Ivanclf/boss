package com.boss.bosschatservice.controller;

import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.vo.ChatLatestListVO;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

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
    public PageInfo<ChatRecordVO> getConversationRecord(
            @RequestHeader("authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @PathVariable @NotNull @Min(0L) Long uid
    ) {
        return conversationService.getChatRecord(token, pageNum, pageSize, uid);
    }

    @PostMapping("/save/{role}")
    public void save(@RequestBody ChatMessage chatMessage, @PathVariable Integer role) throws Exception{
        conversationService.saveChatRecord(chatMessage.getFromUid(), chatMessage.getToUid(), chatMessage.getMessage(), role);
    }

    @GetMapping("/ai/{uid}")
    public PageInfo<ChatRecordVO> getAiChatRecord(
            @PathVariable @NotNull @Min(0L) Long uid,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return conversationService.getAiChatRecord(uid, pageNum, pageSize);
    }

    @GetMapping("/es/all")
    public List<ChatMessageElasticsearchDTO> initElasticsearch() {
        return conversationService.queryForElasticsearch();
    }
}
