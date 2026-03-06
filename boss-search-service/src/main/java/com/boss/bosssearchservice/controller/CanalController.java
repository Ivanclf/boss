package com.boss.bosssearchservice.controller;

import com.alibaba.fastjson.JSON;
import com.boss.bosscommon.clients.ChatsClient;
import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.clients.UserClient;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bosssearchservice.service.CanalDataSyncService;
import com.boss.bosssearchservice.service.impl.SynchronizeServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_INDEX;
import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_SCRIPT;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_INDEX;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_SCRIPT;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_INDEX;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_SCRIPT;

/**
 * 该 Controller 不对外开放，仅用于对 Canal 进行生命周期管理。
 */
@Component
@Slf4j
public class CanalController {

    @Resource
    private RestHighLevelClient client;
    @Resource
    private UserClient userClient;
    @Resource
    private JobsClient jobsClient;
    @Resource
    private ChatsClient chatsClient;
    @Resource
    private CanalDataSyncService canalDataSyncService;

    /**
     * 在开始时，如果 ES 为空，就进行全量同步，然后调用 Service，进行增量同步。
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException {
        // 初始化 ES 文档
        ensureIndex(JOB_INDEX, JOB_SCRIPT);
        ensureIndex(JOB_APPLY_INDEX, JOB_APPLY_SCRIPT);
        ensureIndex(CHAT_MESSAGE_INDEX, CHAT_MESSAGE_SCRIPT);

        // 初始化空索引
        if (isIndexEmpty(JOB_INDEX)) {
            log.info("job_index 为空，开始初始化数据");
            List<JobElasticsearchDTO> docs = jobsClient.initElasticsearch();
            if(docs != null) {
                bulkSave(JOB_INDEX, dto -> dto.getUid().toString(), docs);
                log.info("job_index 初始化完成，文档数：{}", docs.size());
            }
        }
        if (isIndexEmpty(JOB_APPLY_INDEX)) {
            log.info("job_apply_index 为空，开始初始化数据");
            List<JobApplyElasticsearchDTO> docs = queryForElasticsearch();
            if(docs != null) {
                bulkSave(JOB_APPLY_INDEX, dto -> dto.getApplyId().toString(), docs);
                log.info("job_apply_index 初始化完成，文档数：{}", docs.size());
            }
        }
        if (isIndexEmpty(CHAT_MESSAGE_INDEX)) {
            log.info("job_apply_index 为空，开始初始化数据");
            List<ChatMessageElasticsearchDTO> docs = chatsClient.initElasticsearch();
            if(docs != null) {
                bulkSave(CHAT_MESSAGE_INDEX, dto -> dto.getMessageId().toString(), docs);
                log.info("chat_message_index 初始化完成，文档数：{}", docs.size());
            }
        }
        log.info("Elastic Search 索引表初始化成功");
            
        // 启动 Canal 数据同步
        canalDataSyncService.start();
    }

    /**
     * 在结束时销毁和 Canal 的链接。
     */
    @PreDestroy
    public void destroy() {
        // 停止 Canal 数据同步线程
        canalDataSyncService.stop();
        log.info("CanalController 组件销毁完成");
    }

    /**
     * 创建 ES 索引，并指定使用 ik 分词器。其等效的 DSL 语句如下：
     * <p>
     *     <pre>
     *         {@code
     * PUT /{index}
     * {
     *   "settings": {
     *     "analysis": {
     *       "analyzer": {
     *         "ik_max": {
     *           "type": "custom",
     *           "tokenizer": "ik_max_word"
     *         }
     *       }
     *     }
     *   },
     *   "mappings": {
     *     "properties": {
     *       "id": { "type": "keyword" },
     *       "title": {
     *         "type": "text",
     *         "analyzer": "ik_max"
     *       },
     *       "content": {
     *         "type": "text",
     *         "analyzer": "ik_max"
     *       },
     *       "createdAt": { "type": "date", "format": "strict_date_optional_time||epoch_millis" }
     *     }
     *   }
     * }
     *         }
     *     </pre>
     * </p>
     * @param index
     * @param mappingScript
     * @throws IOException
     */
    private void ensureIndex(String index, String mappingScript) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        if (!client.indices().exists(request, RequestOptions.DEFAULT)) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
            createIndexRequest.settings(Settings.builder()
                    .put("analysis.analyzer.ik_max.type", "custom")
                    .put("analysis.analyzer.ik_max.tokenizer", "ik_max_word"));
            createIndexRequest.mapping(mappingScript, XContentType.JSON);
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info("创建 ES 索引：{}", index);
        }
    }

    /**
     * 查询该索引下的数据是否为空。其中的查询 DSL 语句等价为
     * <p>
     *     <pre>
     *         {@code
     * GET /{index}/_count
     * {
     *     "query":{
     *         "match_all":{}
     *     }
     * }
     *         }
     *     </pre>
     * </p>
     * @param index
     * @return
     * @throws IOException
     */
    private boolean isIndexEmpty(String index) throws IOException {
        CountRequest request = new CountRequest(index);
        CountResponse response = client.count(request, RequestOptions.DEFAULT);
        return response.getCount() == 0;
    }

    /**
     * 该方法会一次性将多个文档写入指定索引中。等效的 DSL 语句为
     * <p>
     *     <pre>
     *         {@code
     * POST /_bulk
     * { "index": { "_index": "{index}", "_id": "{id}" } }
     * { ...文档 JSON... }
     * { "index": { "_index": "{index}", "_id": "{id}" } }
     * { ...文档 JSON... }
     *         }
     *     </pre>
     * </p>
     * @param index
     * @param idGetter 从 POJO 中取出 id 的匿名函数。不写这个的话还要各种反射和猜 id 名字，很难办的。
     * @param docs 是需要存放数据的相应 POJO
     * @param <T>
     * @throws IOException
     */
    private <T> void bulkSave(String index, Function<T, String> idGetter, List<T> docs) throws IOException {
        if (CollectionUtils.isEmpty(docs)) {
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (T doc : docs) {
            IndexRequest request = new IndexRequest(index)
                    .id(idGetter.apply(doc))
                    .source(JSON.toJSONString(doc), XContentType.JSON);
            bulkRequest.add(request);
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    public List<JobApplyElasticsearchDTO> queryForElasticsearch() {
        List<JobElasticsearchDTO> jobs = jobsClient.initElasticsearch();
        List<UserJobApply> users = userClient.initElasticsearch();

        Map<Long, JobElasticsearchDTO> jobMap = jobs.stream()
                .collect(Collectors.toMap(JobElasticsearchDTO::getUid, job -> job));

        List<JobApplyElasticsearchDTO> results = new ArrayList<>();
        for (UserJobApply user : users) {
            JobElasticsearchDTO job = jobMap.get(user.getJobUid());
            if (job != null) {
                Long candidateUid = user.getCandidateUid();
                UserBasicVO candidateBasicInfo = userClient.getUserInfo(candidateUid).getData();
                // 组装投递记录
                JobApplyElasticsearchDTO result = JobApplyElasticsearchDTO.builder()
                        .applyId(user.getId())
                        .candidateUid(candidateUid)
                        .candidateName(candidateBasicInfo.getName())
                        .candidatePhone(candidateBasicInfo.getPhone())
                        .hrUid(user.getHrUid())
                        .jobUid(user.getJobUid())
                        .jobTitle(job.getTitle())
                        .jobCity(job.getCity())
                        .salaryMin(job.getSalaryMin())
                        .salaryMax(job.getSalaryMax())
                        .status(user.getStatus())
                        .applyTime(user.getCreateTime())
                        .tags(job.getTags())
                        .build();
                results.add(result);
            }
        }

        return results;
    }
}
