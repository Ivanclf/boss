package com.boss.bossjobservice.controller;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.JobInsertDTO;
import com.boss.bosscommon.pojo.dto.JobUpdateDTO;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;
import com.boss.bossjobservice.service.JobsService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.net.http.HttpRequest;

import static com.boss.bosscommon.util.TokenUtil.getToken;

@RestController
@RequestMapping("/jobs")
public class JobsController {

    @Resource
    private JobsService jobsService;

    @PostMapping
    public void insertJobs(HttpRequest httpRequest, @RequestBody JobInsertDTO jobInsertDTO) {
        if(jobInsertDTO.getSalaryMax() * jobInsertDTO.getSalaryMin() < 0) {
            throw new clientException("输入的薪资金额不合理");
        }
        if(jobInsertDTO.getTitle().isBlank()) {
            throw new clientException("请输入标题");
        }
        if(jobInsertDTO.getTags() == null || jobInsertDTO.getTags().isEmpty()) {
            throw new clientException("请输入标签");
        }
        String token = getToken(httpRequest);
        jobsService.insert(token, jobInsertDTO);
    }

    @GetMapping("/{uid}")
    public JobBasicInfoVO getJobBasicInfo(@Nonnull @Min(0) @PathVariable Long uid) {
        return jobsService.getJobBasicInfo(uid);
    }

    @PutMapping
    public void updateJobs(@RequestBody JobUpdateDTO jobUpdateDTO) {
        if(jobUpdateDTO.getUid() == null) {
            throw new clientException("请输入正确的 UID");
        }
        if(jobUpdateDTO.getSalaryMax() * jobUpdateDTO.getSalaryMin() < 0) {
            throw new clientException("输入的薪资金额不合理");
        }
        if(jobUpdateDTO.getTitle().isBlank()) {
            throw new clientException("请输入标题");
        }
        if(jobUpdateDTO.getTags() == null || jobUpdateDTO.getTags().isEmpty()) {
            throw new clientException("请输入标签");
        }
        jobsService.update(jobUpdateDTO);
    }
}
