package com.boss.bosssearchservice.controller;

import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.result.Result;
import com.boss.bosssearchservice.service.SearchService;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping("/job")
    public Result<List<JobElasticsearchDTO>> searchJob(
            @RequestParam String keyword,
            @RequestParam String city,
            @RequestParam Integer salaryMin,
            @RequestParam Integer salaryMax,
            @RequestParam @DefaultValue("1") @Min(0) Integer pageNum,
            @RequestParam @DefaultValue("10") @Min(0) Integer pageSize
    ) {
        try {
            List<JobElasticsearchDTO> jobElasticsearchDTOS = searchService.searchJob(keyword, city, salaryMin, salaryMax, pageNum, pageSize);
            return Result.success(jobElasticsearchDTOS);
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/jobApply")
    public Result<List<JobApplyElasticsearchDTO>> searchJobApply(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam String jobCity,
            @RequestParam Integer salaryMin,
            @RequestParam Integer salaryMax,
            @RequestParam Integer status,
            @RequestParam LocalDateTime date,
            @RequestParam @DefaultValue("1") @Min(0) Integer pageNum,
            @RequestParam @DefaultValue("10") @Min(0) Integer pageSize
    ) {
        try {
            List<JobApplyElasticsearchDTO> jobApplyElasticsearchDTOS = searchService.searchJobApply(token, keyword, jobCity, salaryMin, salaryMax, status, date, pageNum, pageSize);
            return Result.success(jobApplyElasticsearchDTOS);
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/chatMessage")
    public Result<List<ChatMessageElasticsearchDTO>> searchChatMessage(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam LocalDateTime date,
            @RequestParam @DefaultValue("1") @Min(0) Integer pageNum,
            @RequestParam @DefaultValue("10") @Min(0) Integer pageSize
    ) {
        try {
            List<ChatMessageElasticsearchDTO> chatMessageElasticsearchDTOS = searchService.searchChatMessage(token, keyword, date, pageNum, pageSize);
            return Result.success(chatMessageElasticsearchDTOS);
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }
}
