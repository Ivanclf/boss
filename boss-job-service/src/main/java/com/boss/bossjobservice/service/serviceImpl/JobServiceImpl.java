package com.boss.bossjobservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
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
    public void insert(String token, JobInsertDTO jobInsertDTO) {
        Long hrUid = (Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid");
        if(hrUid == null) {
            throw new clientException("用户未登录");
        }

        List<String> tags = jobInsertDTO.getTags();
        LocalDateTime now = LocalDateTime.now();
        Job job = new Job();
        job.setHrUid(jobInsertDTO.getHrUid());
        job.setTitle(jobInsertDTO.getTitle());
        job.setDescription(jobInsertDTO.getDescription());
        job.setRequirement(jobInsertDTO.getRequirement());
        job.setCity(jobInsertDTO.getCity());
        job.setSalaryMin(jobInsertDTO.getSalaryMin());
        job.setSalaryMax(jobInsertDTO.getSalaryMax());
        job.setHrUid(hrUid);
        job.setStatus(UNPUBLISHED);
        job.setPublishTime(now);
        job.setUpdateTime(now);
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
        JobBasicInfoVO jobBasicInfoVO = new JobBasicInfoVO();
        jobBasicInfoVO.setUid(job.getUid());
        jobBasicInfoVO.setHrUid(job.getHrUid());
        jobBasicInfoVO.setTitle(job.getTitle());
        jobBasicInfoVO.setDescription(job.getDescription());
        jobBasicInfoVO.setRequirement(job.getRequirement());
        jobBasicInfoVO.setCity(job.getCity());
        jobBasicInfoVO.setSalaryMin(job.getSalaryMin());
        jobBasicInfoVO.setSalaryMax(job.getSalaryMax());
        jobBasicInfoVO.setStatus(job.getStatus());
        jobBasicInfoVO.setJobTags(
                jobTags.stream().map(JobTag::getTag).toList()
        );
        return jobBasicInfoVO;
    }

    @Override
    @Transactional
    public void update(JobUpdateDTO jobUpdateDTO) {
        Job job = new Job();
        job.setUid(jobUpdateDTO.getUid());
        job.setHrUid(jobUpdateDTO.getHrUid());
        job.setTitle(jobUpdateDTO.getTitle());
        job.setDescription(jobUpdateDTO.getDescription());
        job.setRequirement(jobUpdateDTO.getRequirement());
        job.setCity(jobUpdateDTO.getCity());
        job.setSalaryMin(jobUpdateDTO.getSalaryMin());
        job.setSalaryMax(jobUpdateDTO.getSalaryMax());
        job.setStatus(jobUpdateDTO.getStatus());

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
            JobElasticsearchDTO jobElasticsearchDTO = new JobElasticsearchDTO();
            jobElasticsearchDTO.setUid(job.getUid());
            jobElasticsearchDTO.setHrUid(job.getHrUid());
            jobElasticsearchDTO.setTitle(job.getTitle());
            jobElasticsearchDTO.setDescription(job.getDescription());
            jobElasticsearchDTO.setRequirement(job.getRequirement());
            jobElasticsearchDTO.setCity(job.getCity());
            jobElasticsearchDTO.setSalaryMin(job.getSalaryMin());
            jobElasticsearchDTO.setSalaryMax(job.getSalaryMax());
            jobElasticsearchDTO.setStatus(job.getStatus());
            jobElasticsearchDTO.setPublishTime(job.getPublishTime());
            jobElasticsearchDTO.setUpdateTime(job.getUpdateTime());
            jobElasticsearchDTO.setTags(
                    jobTagMapper.getTagsByUid(jobElasticsearchDTO.getUid()).stream()
                            .map(JobTag::getTag).toList()
            );
            results.add(jobElasticsearchDTO);
        }
        return results;
    }
}
