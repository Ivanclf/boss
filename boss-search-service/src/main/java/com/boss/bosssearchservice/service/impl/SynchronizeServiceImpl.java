package com.boss.bosssearchservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.Job;
import com.boss.bosscommon.pojo.entity.JobTag;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bosssearchservice.service.SynchronizeService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_INDEX;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_INDEX;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_INDEX;
import static com.boss.bosssearchservice.util.CanalColumnUtil.*;

@Service
@Slf4j
public class SynchronizeServiceImpl implements SynchronizeService {

    @Resource
    private UserClient userClient;
    @Resource
    private JobsClient jobsClient;
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void sync(
            String schema,
            String table,
            EventType eventType,
            List<Column> columns) {

        switch (schema) {
            case "job_db" -> {
                if("job".equals(table) || "job_tag".equals(table)) {
                    jobIndexSync(eventType, columns);
                }
            }
            case "user_db" -> {
                if("user_job_apply".equals(table)) {
                    jobApplyIndexSync(eventType, columns);
                }
            }
            case "chat_db" -> {
                if("chat_record".equals(table)) {
                    chatMessageIndexSync(eventType, columns);
                }
            }
            default -> {

            }
        }
    }

    /**
     * 工作数据同步
     * @param eventType
     * @param columns
     */
    private void jobIndexSync(EventType eventType, List<Column> columns) {
        Long uid = getLong(columns, "uid");
        if (uid == null) {
            log.warn("JobIndexSync: uid为空，忽略");
            return;
        }

        if (eventType == EventType.DELETE) {
            try {
                DeleteRequest request = new DeleteRequest(JOB_INDEX, uid.toString());
                client.delete(request, RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("JobIndexSync：删除 uid={} 异常", uid, e);
            }
            return;
        }

        Job job = jobsClient.queryForElasticsearch(uid);
        if (job == null) {
            log.warn("JobIndexSync: 未找到uid={}的职位信息", uid);
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
            IndexRequest request = new IndexRequest(JOB_INDEX)
                    .id(dto.getUid().toString())
                    .source(objectMapper.writeValueAsString(dto), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("JobIndexSync 保存 uid={} 异常", uid, e);
        }
    }

    /**、
     * 工作投递信息同步
     * @param eventType
     * @param columns
     */
    private void jobApplyIndexSync(EventType eventType, List<Column> columns) {
        Long id = getLong(columns, "id");
        if(id == null) {
            log.warn("JobApplyIndexSync: id为空，忽略");
            return;
        }

        if(eventType == EventType.DELETE) {
            try {
                DeleteRequest request = new DeleteRequest(JOB_APPLY_INDEX, id.toString());
                client.delete(request, RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("JobApplyIndexSync 删除 applyId={} 异常", id, e);
            }
            return;
        }
        UserJobApply userJobApply = userClient.queryJobApplyForElasticsearch(id);
        if (userJobApply == null) {
            log.warn("JobApplyIndexSync: 未找到applyId={}的申请信息", id);
            return;
        }
        UserBasicVO candidate = userClient.getUserInfo(userJobApply.getCandidateUid()).getData();
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
            IndexRequest request = new IndexRequest(JOB_APPLY_INDEX)
                    .id(dto.getApplyId().toString())
                    .source(objectMapper.writeValueAsString(dto), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("JobApplyIndexSync 保存 applyId={} 异常", id, e);
        }
    }

    /**
     * 聊天记录信息同步
     * @param eventType
     * @param columns
     */
    public void chatMessageIndexSync(EventType eventType, List<Column> columns) {

        Long messageId = getLong(columns, "id");

        if (messageId != null && eventType == EventType.DELETE) {
            try {
                DeleteRequest request = new DeleteRequest(CHAT_MESSAGE_INDEX, messageId.toString());
                client.delete(request, RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("删除 chat_message 失败", e);
            }
            return;
        }

        ChatMessageElasticsearchDTO chatMessageElasticsearchDTO = ChatMessageElasticsearchDTO.builder()
                .messageId(messageId)
                .fromUid(getLong(columns, "from_uid"))
                .toUid(getLong(columns, "to_uid"))
                .jobUid(getLong(columns, "job_uid"))
                .context(getString(columns, "context"))
                .createTime(getTime(columns, "create_time"))
                .build();

        try {
            IndexRequest request = new IndexRequest(CHAT_MESSAGE_INDEX)
                    .id(chatMessageElasticsearchDTO.getMessageId().toString())
                    .source(JSON.toJSONString(chatMessageElasticsearchDTO), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("同步 chat_message 失败", e);
        }
    }
}
