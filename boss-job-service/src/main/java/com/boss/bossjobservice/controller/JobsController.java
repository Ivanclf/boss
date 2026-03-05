package com.boss.bossjobservice.controller;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobInsertDTO;
import com.boss.bosscommon.pojo.dto.JobUpdateDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;
import com.boss.bosscommon.result.Result;
import com.boss.bossjobservice.service.JobsService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobsController {

    @Resource
    private JobsService jobsService;

    @PostMapping
    public Result insertJobs(@RequestHeader("authorization") String token, @RequestBody JobInsertDTO jobInsertDTO) {
        if(jobInsertDTO.getSalaryMax() * jobInsertDTO.getSalaryMin() < 0) {
            return Result.error("输入的薪资金额不合理");
        }
        if(StringUtils.hasText(jobInsertDTO.getTitle())) {
            return Result.error("请输入标题");
        }
        if(CollectionUtils.isEmpty(jobInsertDTO.getTags())) {
            return Result.error("请输入标签");
        }
        try {
            jobsService.insert(token, jobInsertDTO);
            return Result.success();
        } catch (clientException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{uid}")
    public JobBasicInfoVO getJobBasicInfo(@Nonnull @Min(0) @PathVariable Long uid) {
        return jobsService.getJobBasicInfo(uid);
    }

    @PutMapping
    public Result updateJobs(@RequestBody JobUpdateDTO jobUpdateDTO) {
        if(jobUpdateDTO.getUid() == null) {
            return Result.error("请输入正确的 UID");
        }
        if(jobUpdateDTO.getSalaryMax() * jobUpdateDTO.getSalaryMin() < 0) {
            return Result.error("输入的薪资金额不合理");
        }
        if(StringUtils.hasText(jobUpdateDTO.getTitle())) {
            return Result.error("请输入标题");
        }
        if(CollectionUtils.isEmpty(jobUpdateDTO.getTags())) {
            return Result.error("请输入标签");
        }
        jobsService.update(jobUpdateDTO);
        return Result.success();
    }

    @GetMapping("/es/all")
    public List<JobElasticsearchDTO> initElasticsearch() {
        return jobsService.queryForElasticsearch();
    }

    @GetMapping("/es/job/{uid}")
    public Job queryForElasticsearch(@PathVariable Long uid) {
        return jobsService.queryJobForElasticsearch(uid);
    }

    @GetMapping("/es/jobtag/{uid}")
    public List<JobTag> queryTagsForElasticsearch(@PathVariable Long uid) {
        return jobsService.queryTagsForElasticsearch(uid);
    }
}
