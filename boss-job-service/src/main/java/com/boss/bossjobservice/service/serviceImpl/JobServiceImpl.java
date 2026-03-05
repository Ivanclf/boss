package com.boss.bossjobservice.service.serviceImpl;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobInsertDTO;
import com.boss.bosscommon.pojo.dto.JobUpdateDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;
import com.boss.bossjobservice.mapper.JobTagMapper;
import com.boss.bossjobservice.mapper.JobsMapper;
import com.boss.bossjobservice.service.JobsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.boss.bosscommon.constant.JobPublishConstant.UNPUBLISHED;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;

@Service
@Slf4j
public class JobServiceImpl implements JobsService {

    @Resource
    private JobsMapper jobsMapper;
    @Resource
    private JobTagMapper jobTagMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void insert(String token, JobInsertDTO jobInsertDTO) throws clientException {
        Object uid = stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");
        Long hrUid = Long.valueOf(uid instanceof String ? (String) uid : "0");
        if(hrUid.equals(0L)) {
            throw new clientException("用户未登录");
        }

        List<String> tags = jobInsertDTO.getTags();
        LocalDateTime now = LocalDateTime.now();
        Job job = Job.builder()
                .hrUid(jobInsertDTO.getHrUid())
                .title(jobInsertDTO.getTitle())
                .description(jobInsertDTO.getDescription())
                .requirement(jobInsertDTO.getRequirement())
                .city(jobInsertDTO.getCity())
                .salaryMax(jobInsertDTO.getSalaryMax())
                .salaryMin(jobInsertDTO.getSalaryMin())
                .status(UNPUBLISHED)
                .publishTime(now)
                .updateTime(now)
                .build();
        Long jobUid = jobsMapper.insert(job);
        if(jobUid == null) {
            throw new clientException("插入失败");
        }

        List<JobTag> jobTags = new ArrayList<>();
        for(String tag: tags) {
            jobTags.add(new JobTag(null, jobUid, tag));
        }
        jobTagMapper.insertBatch(jobTags);
    }

    @Override
    public JobBasicInfoVO getJobBasicInfo(Long uid) {
        Job job = jobsMapper.getJobByUid(uid);
        List<JobTag> jobTags = jobTagMapper.getTagsByUid(uid);
        return JobBasicInfoVO.builder()
                .uid(job.getUid())
                .hrUid(job.getHrUid())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirement(job.getRequirement())
                .city(job.getCity())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .status(job.getStatus())
                .jobTags(jobTags.stream().map(JobTag::getTag).toList())
                .build();
    }

    @Override
    @Transactional
    public void update(JobUpdateDTO jobUpdateDTO) {
        Job job = Job.builder()
                .uid(jobUpdateDTO.getUid())
                .hrUid(jobUpdateDTO.getHrUid())
                .title(jobUpdateDTO.getTitle())
                .description(jobUpdateDTO.getDescription())
                .requirement(jobUpdateDTO.getRequirement())
                .city(jobUpdateDTO.getCity())
                .salaryMin(jobUpdateDTO.getSalaryMin())
                .salaryMax(jobUpdateDTO.getSalaryMax())
                .status(jobUpdateDTO.getStatus())
                .build();

        Long uid = jobUpdateDTO.getUid();
        List<String> tags = jobUpdateDTO.getTags();
        List<JobTag> jobTags = new ArrayList<>();
        for(String tag : tags) {
            jobTags.add(new JobTag(null, uid, tag));
        }

        job.setUpdateTime(LocalDateTime.now());

        jobsMapper.update(job);
        jobTagMapper.deleteByJobUid(uid);
        jobTagMapper.insertBatch(jobTags);
    }

    @Override
    public List<JobElasticsearchDTO> queryForElasticsearch() {
        List<Job> jobs = jobsMapper.queryAll();
        List<JobElasticsearchDTO> results = new ArrayList<>();
        for(Job job : jobs) {
            results.add(JobElasticsearchDTO.builder()
                    .uid(job.getUid())
                    .hrUid(job.getHrUid())
                    .title(job.getTitle())
                    .description(job.getDescription())
                    .requirement(job.getRequirement())
                    .city(job.getCity())
                    .salaryMin(job.getSalaryMin())
                    .salaryMax(job.getSalaryMax())
                    .status(job.getStatus())
                    .publishTime(job.getPublishTime())
                    .updateTime(job.getUpdateTime())
                    .tags(jobTagMapper.getTagsByUid(job.getUid()).stream()
                            .map(JobTag::getTag)
                            .toList())
                    .build());
        }
        return results;
    }

    @Override
    public Job queryJobForElasticsearch(Long uid) {
        return jobsMapper.getJobByUid(uid);
    }

    @Override
    public List<JobTag> queryTagsForElasticsearch(Long uid) {
        return jobTagMapper.getTagsByUid(uid);
    }
}
