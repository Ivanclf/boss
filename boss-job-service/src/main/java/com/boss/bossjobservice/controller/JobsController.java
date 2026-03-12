package com.boss.bossjobservice.controller;

import com.boss.bosscommon.exception.ClientException;
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

/**
 * 工作岗位服务相关接口
 */
@RestController
@RequestMapping("/jobs")
public class JobsController {

    @Resource
    private JobsService jobsService;

    /**
     * 新建工作信息
     * @param token 用户的 token
     * @param jobInsertDTO
     * @return
     */
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
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取指定工作的信息
     * @param uid
     * @return
     */
    @GetMapping("/{uid}")
    public JobBasicInfoVO getJobBasicInfo(@Nonnull @Min(0) @PathVariable Long uid) {
        return jobsService.getJobBasicInfo(uid);
    }

    /**
     * 更新工作相关信息
     * @param jobUpdateDTO
     * @return
     */
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

    /**
     * 仅用于 ES，实现工作全量更新
     * @return
     */
    @GetMapping("/es/all")
    public List<JobElasticsearchDTO> initElasticsearch() {
        return jobsService.queryForElasticsearch();
    }

    /**
     * 仅用于 ES，实现查找特定的工作
     * @param uid
     * @return
     */
    @GetMapping("/es/job/{uid}")
    public Job queryForElasticsearch(@PathVariable Long uid) {
        return jobsService.queryJobForElasticsearch(uid);
    }

    /**
     * 仅用于 ES，实现查找特定工作标签
     * @param uid
     * @return
     */
    @GetMapping("/es/jobtag/{uid}")
    public List<JobTag> queryTagsForElasticsearch(@PathVariable Long uid) {
        return jobsService.queryTagsForElasticsearch(uid);
    }
}
