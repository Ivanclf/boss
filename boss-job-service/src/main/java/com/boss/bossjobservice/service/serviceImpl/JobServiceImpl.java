package com.boss.bossjobservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import com.boss.bosscommon.exception.clientException;
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
        Job job = BeanUtil.copyProperties(jobInsertDTO, Job.class);
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
        JobBasicInfoVO jobBasicInfoVO = BeanUtil.copyProperties(job, JobBasicInfoVO.class);
        jobBasicInfoVO.setJobTags(
                jobTags.stream().map(JobTag::getTag).toList()
        );
        return jobBasicInfoVO;
    }

    @Override
    public void update(JobUpdateDTO jobUpdateDTO) {
        // todo 实现更新逻辑
        return;
    }
}
