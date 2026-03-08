package com.boss.bossaiservice.controller;

import com.boss.bossaiservice.service.InterviewService;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.boss.bosscommon.pojo.vo.ChatRecordVO;
import com.boss.bosscommon.result.Result;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * ai 服务相关接口
 */
@RestController
@RequestMapping("/ai/interview")
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    /**
     * 开始一通对话。开始时固定以“可以开始了”为提示词，让 ai 先主动生成问题。
     * @param token
     * @return
     */
    @GetMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> start(@RequestHeader("Authorization") String token) {
        return interviewService.start(token);
    }

    /**
     * 向 ai 问问题。
     * @param chatMessage
     * @param token
     * @return
     */
    @PostMapping(value = "/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> question(@RequestBody ChatMessage chatMessage, @RequestHeader("Authorization") String token) {
        return interviewService.question(chatMessage, token);
    }

    /**
     * 获取历史记录
     * @param token
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/history")
    public Result<PageInfo<ChatRecordVO>> getHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(interviewService.getHistory(token, pageNum, pageSize));
    }

}