package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;
import com.boss.bossuserservice.service.CandidateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidate")
public class CandidateController {

    @Resource
    private CandidateService candidateService;

    @PostMapping("/jobs")
    public void apply(@RequestHeader("authorization") String token, @RequestBody UserJobApplyDTO userJobApplyDTO) {
        candidateService.apply(token, userJobApplyDTO);
    }
}
