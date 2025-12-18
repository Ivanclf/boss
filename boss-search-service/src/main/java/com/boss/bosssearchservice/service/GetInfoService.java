package com.boss.bosssearchservice.service;

import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;

import java.util.List;

public interface GetInfoService {
    List<JobApplyElasticsearchDTO> queryForElasticsearch();
}
