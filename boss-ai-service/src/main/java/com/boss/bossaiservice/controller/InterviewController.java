package com.boss.bossaiservice.controller;

import com.boss.bossaiservice.service.InterviewService;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai/interview")
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    @GetMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> start(@RequestHeader("Authorization") String token) {
        return interviewService.start(token);
    }

    @PostMapping(value = "/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> question(@RequestBody ChatMessage chatMessage, @RequestHeader("Authorization") String token) {
        return interviewService.question(chatMessage, token);
    }

    @GetMapping("/history")
    public PageInfo<ChatRecordVO> getHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return interviewService.getHistory(token, pageNum, pageSize);
    }

}