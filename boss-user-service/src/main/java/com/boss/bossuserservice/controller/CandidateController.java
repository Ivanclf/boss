package com.boss.bossuserservice.controller;

import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;
import com.boss.bosscommon.result.Result;
import com.boss.bossuserservice.service.CandidateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 处理求职者求职服务的接口
 */
@RestController
@RequestMapping("/candidate")
public class CandidateController {

    @Resource
    private CandidateService candidateService;

    /**
     * 求职者投递求职消息
     * @param token
     * @param userJobApplyDTO
     * @return
     */
    @PostMapping("/jobs")
    public Result apply(@RequestHeader("Authorization") String token, @RequestBody UserJobApplyDTO userJobApplyDTO) {
        try {
            candidateService.apply(token, userJobApplyDTO);
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }
}
