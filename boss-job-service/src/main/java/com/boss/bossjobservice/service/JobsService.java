package com.boss.bossjobservice.service;

import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobInsertDTO;
import com.boss.bosscommon.pojo.dto.JobUpdateDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;

import java.util.List;

public interface JobsService {
    void insert(String token, JobInsertDTO jobInsertDTO);

    JobBasicInfoVO getJobBasicInfo(Long uid);

    void update(JobUpdateDTO jobUpdateDTO);

    List<JobElasticsearchDTO> queryForElasticsearch();

    Job queryJobForElasticsearch(Long uid);

    List<JobTag> queryTagsForElasticsearch(Long uid);
}
