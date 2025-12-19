package com.boss.bosssearchservice.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bosssearchservice.service.JobApplyIndexSyncService;
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

import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_INDEX;
import static com.boss.bosssearchservice.util.CanalColumnUtil.getLong;

@Service
@Slf4j
public class JobApplyIndexSyncServiceImpl implements JobApplyIndexSyncService {

    @Resource
    private UserClient userClient;
    @Resource
    private JobsClient jobsClient;
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ObjectMapper objectMapper;
    
    @Override
    public void sync(EventType eventType, List<Column> columns) {
        Long id = getLong(columns, "id");
        if(id == null) {
            log.warn("JobApplyIndexSyncServiceImpl: id为空，忽略");
            return;
        }

        if(eventType == EventType.DELETE) {
            delete(id);
            return;
        }
        UserJobApply userJobApply = userClient.queryJobApplyForElasticsearch(id);
        if (userJobApply == null) {
            log.warn("JobApplyIndexSyncServiceImpl: 未找到applyId={}的申请信息", id);
            return;
        }
        UserBasicVO candidate = userClient.getUserInfo(userJobApply.getCandidateUid());
        Job job = jobsClient.queryForElasticsearch(userJobApply.getJobUid());
        List<String> tags = jobsClient.queryTagsForElasticsearch(userJobApply.getJobUid())
                .stream().map(JobTag::getTag).collect(Collectors.toList());

        JobApplyElasticsearchDTO dto = JobApplyElasticsearchDTO.builder()
                .applyId(userJobApply.getId())
                .candidateUid(candidate.getUid())
                .candidateName(candidate.getName())
                .candidatePhone(candidate.getPhone())
                .hrUid(job.getHrUid())
                .jobUid(job.getUid())
                .jobTitle(job.getTitle())
                .jobCity(job.getCity())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .status(userJobApply.getStatus())
                .applyTime(userJobApply.getCreateTime())
                .tags(tags)
                .build();
                
        try {
            save(dto);
        } catch (IOException e) {
            log.error("JobApplyIndexSyncServiceImpl 保存 applyId={} 异常", id, e);
        }
    }

    private void save(JobApplyElasticsearchDTO dto) throws IOException {
        IndexRequest request = new IndexRequest(JOB_APPLY_INDEX)
                .id(dto.getApplyId().toString())
                .source(objectMapper.writeValueAsString(dto), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    private void delete(Long id) {
        try {
            DeleteRequest request = new DeleteRequest(JOB_APPLY_INDEX, id.toString());
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("JobApplyIndexSyncServiceImpl 删除 applyId={} 异常", id, e);
        }
    }
}