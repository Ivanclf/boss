package com.boss.bosssearchservice.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosssearchservice.service.JobIndexSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_INDEX;
import static com.boss.bosssearchservice.util.CanalColumnUtil.getLong;

@Service
@Slf4j
public class JobIndexSyncServiceImpl implements JobIndexSyncService {

    @Resource
    private JobsClient jobsClient;
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void sync(CanalEntry.EventType eventType, List<CanalEntry.Column> columns) {
        Long uid = getLong(columns, "uid");
        if (uid == null) {
            log.warn("JobIndexSyncServiceImpl: uid为空，忽略");
            return;
        }

        if (eventType == CanalEntry.EventType.DELETE) {
            delete(uid);
            return;
        }

        Job job = jobsClient.queryForElasticsearch(uid);
        if (job == null) {
            log.warn("JobIndexSyncServiceImpl: 未找到uid={}的职位信息", uid);
            return;
        }
        List<String> tags = jobsClient.queryTagsForElasticsearch(uid)
                .stream().map(JobTag::getTag).collect(Collectors.toList());

        JobElasticsearchDTO dto = JobElasticsearchDTO.builder()
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
                .tags(tags)
                .build();

        try {
            save(dto);
        } catch (IOException e) {
            log.error("JobIndexSyncServiceImpl 保存 uid={} 异常", uid, e);
        }
    }

    private void save(JobElasticsearchDTO dto) throws IOException {
        IndexRequest request = new IndexRequest(JOB_INDEX)
                .id(dto.getUid().toString())
                .source(objectMapper.writeValueAsString(dto), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    private void delete(Long uid) {
        try {
            DeleteRequest request = new DeleteRequest(JOB_INDEX, uid.toString());
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("JobIndexSyncServiceImpl 删除 uid={} 异常", uid, e);
        }
    }
}