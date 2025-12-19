package com.boss.bosscommon.clients;

import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "boss-job-service", path = "/jobs")
public interface JobsClient {

    @GetMapping("/{uid}")
    JobBasicInfoVO getJobBasicInfo(@PathVariable Long uid);

    @GetMapping("/es/all")
    List<JobElasticsearchDTO> initElasticsearch();

    @GetMapping("/es/job/{uid}")
    Job queryForElasticsearch(@PathVariable Long uid);

    @GetMapping("/es/jobtag/{uid}")
    List<JobTag> queryTagsForElasticsearch(@PathVariable Long uid);
}
