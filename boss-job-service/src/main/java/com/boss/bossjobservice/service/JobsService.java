package com.boss.bossjobservice.service;

import com.boss.bosscommon.pojo.dto.JobInsertDTO;
import com.boss.bosscommon.pojo.dto.JobUpdateDTO;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;

public interface JobsService {
    void insert(String token, JobInsertDTO jobInsertDTO);

    JobBasicInfoVO getJobBasicInfo(Long uid);

    void update(JobUpdateDTO jobUpdateDTO);
}
