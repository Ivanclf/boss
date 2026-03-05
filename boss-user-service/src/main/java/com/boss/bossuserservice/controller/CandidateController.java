package com.boss.bossuserservice.controller;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;
import com.boss.bosscommon.result.Result;
import com.boss.bossuserservice.service.CandidateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidate")
public class CandidateController {

    @Resource
    private CandidateService candidateService;

    @PostMapping("/jobs")
    public Result apply(@RequestHeader("Authorization") String token, @RequestBody UserJobApplyDTO userJobApplyDTO) {
        try {
            candidateService.apply(token, userJobApplyDTO);
        } catch (clientException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }
}
