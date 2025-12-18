package com.boss.bosscommon.clients;

import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "boss-chat-service", path = "/chat/conversation")
public interface ChatsClient {
    @PostMapping("/save/{role}")
    void save(@RequestBody ChatMessage chatMessage, @PathVariable Integer role);

    @GetMapping("/ai/{uid}")
    PageInfo<ChatRecordVO> getAiChatRecord(
            @PathVariable Long uid,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize);

    @GetMapping("/es/all")
    List<ChatMessageElasticsearchDTO> initElasticsearch();
}
