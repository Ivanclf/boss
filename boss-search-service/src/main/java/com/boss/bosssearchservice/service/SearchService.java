package com.boss.bosssearchservice.service;

import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface SearchService {
    List<JobElasticsearchDTO> searchJob(
            String keyword,
            String city,
            Integer salaryMin,
            Integer salaryMax,
            Integer pageNum,
            Integer pageSize
    ) throws IOException;

    List<JobApplyElasticsearchDTO> searchJobApply(String keyword, String jobCity, Integer salaryMin, Integer salaryMax, Integer status, LocalDateTime date, Integer pageNum, Integer pageSize) throws IOException;

    List<ChatMessageElasticsearchDTO> searchChatMessage(String keyword, LocalDateTime date, Integer pageNum, Integer pageSize) throws IOException;
}
