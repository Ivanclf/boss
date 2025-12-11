package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;
import com.boss.bossuserservice.service.CandidateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;

import static com.boss.bosscommon.util.TokenUtil.getToken;

@RestController
@RequestMapping("/candidate")
public class CandidateController {

    @Resource
    private CandidateService candidateService;

    @PostMapping("/jobs")
    public void apply(HttpRequest httpRequest, @RequestBody UserJobApplyDTO userJobApplyDTO) {
        String token = getToken(httpRequest);
        candidateService.apply(token, userJobApplyDTO);
    }
}
