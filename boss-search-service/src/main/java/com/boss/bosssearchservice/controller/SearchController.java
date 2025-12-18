package com.boss.bosssearchservice.controller;

import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
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
    public List<JobElasticsearchDTO> searchJob(
            @RequestParam String keyword,
            @RequestParam String city,
            @RequestParam Integer salaryMin,
            @RequestParam Integer salaryMax,
            @RequestParam @DefaultValue("1") @Min(0) Integer pageNum,
            @RequestParam @DefaultValue("10") @Min(0) Integer pageSize
    ) throws IOException {
        return searchService.searchJob(keyword, city, salaryMin, salaryMax, pageNum, pageSize);
    }

    @GetMapping("/jobApply")
    public List<JobApplyElasticsearchDTO> searchJobApply(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam String jobCity,
            @RequestParam Integer salaryMin,
            @RequestParam Integer salaryMax,
            @RequestParam Integer status,
            @RequestParam LocalDateTime date,
            @RequestParam @DefaultValue("1") @Min(0) Integer pageNum,
            @RequestParam @DefaultValue("10") @Min(0) Integer pageSize
    ) throws IOException {
        return searchService.searchJobApply(token, keyword, jobCity, salaryMin, salaryMax, status, date, pageNum, pageSize);
    }

    @GetMapping("/chatMessage")
    public List<ChatMessageElasticsearchDTO> searchChatMessage(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam LocalDateTime date,
            @RequestParam @DefaultValue("1") @Min(0) Integer pageNum,
            @RequestParam @DefaultValue("10") @Min(0) Integer pageSize
    ) throws IOException {
        return searchService.searchChatMessage(token, keyword, date, pageNum, pageSize);
    }
}
