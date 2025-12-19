package com.boss.bosssearchservice.service.impl;

import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bosssearchservice.service.GetInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GetInfoServiceImpl implements GetInfoService {

    @Resource
    private JobsClient jobsClient;
    @Resource
    private UserClient userClient;

    @Override
    public List<JobApplyElasticsearchDTO> queryForElasticsearch() {
        List<JobElasticsearchDTO> jobs = jobsClient.initElasticsearch();
        List<UserJobApply> users = userClient.initElasticsearch();

        Map<Long, JobElasticsearchDTO> jobMap = jobs.stream()
                .collect(Collectors.toMap(JobElasticsearchDTO::getUid, job -> job));

        List<JobApplyElasticsearchDTO> results = new ArrayList<>();
        for (UserJobApply user : users) {
            JobElasticsearchDTO job = jobMap.get(user.getJobUid());
            if (job != null) {
                Long candidateUid = user.getCandidateUid();
                UserBasicVO candidateBasicInfo = userClient.getUserInfo(candidateUid);
                JobApplyElasticsearchDTO result = JobApplyElasticsearchDTO.builder()
                        .applyId(user.getId())
                        .candidateUid(candidateUid)
                        .candidateName(candidateBasicInfo.getName())
                        .candidatePhone(candidateBasicInfo.getPhone())
                        .hrUid(user.getHrUid())
                        .jobUid(user.getJobUid())
                        .jobTitle(job.getTitle())
                        .jobCity(job.getCity())
                        .salaryMin(job.getSalaryMin())
                        .salaryMax(job.getSalaryMax())
                        .status(user.getStatus())
                        .applyTime(user.getCreateTime())
                        .tags(job.getTags())
                        .build();
                results.add(result);
            }
        }
        
        return results;
    }
}