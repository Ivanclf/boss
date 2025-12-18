package com.boss.bosscommon.clients;

import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;
import jakarta.annotation.Nonnull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "boss-job-service", path = "/jobs")
public interface JobsClient {

    @GetMapping("/{uid}")
    JobBasicInfoVO getJobBasicInfo(@Nonnull @PathVariable Long uid);

    @GetMapping("/es/all")
    List<JobElasticsearchDTO> updateElasticsearch();
}
