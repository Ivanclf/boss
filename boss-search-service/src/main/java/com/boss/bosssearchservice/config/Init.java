package com.boss.bosssearchservice.config;

import com.alibaba.fastjson.JSON;
import com.boss.bosscommon.clients.ChatsClient;
import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobApplyElasticsearchDTO;
import com.boss.bosscommon.pojo.dto.JobElasticsearchDTO;
import com.boss.bosssearchservice.service.GetInfoService;
import jakarta.annotation.PostConstruct;
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_INDEX;
import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_SCRIPT;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_INDEX;
import static com.boss.bosssearchservice.constants.JobApplyIndexConstant.JOB_APPLY_SCRIPT;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_INDEX;
import static com.boss.bosssearchservice.constants.JobIndexConstant.JOB_SCRIPT;

@Component
@Slf4j
public class Init {

    @Resource
    private RestHighLevelClient client;
    @Resource
    private GetInfoService getInfoService;
    @Resource
    private JobsClient jobsClient;
    @Resource
    private ChatsClient chatsClient;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @PostConstruct
    public void createIndex() throws IOException {

        if(initialized.get()) {
            return;
        }

        synchronized (this) {
            if(initialized.get()) {
                return;
            }

            ensureIndex(JOB_INDEX, JOB_SCRIPT);
            ensureIndex(JOB_APPLY_INDEX, JOB_APPLY_SCRIPT);
            ensureIndex(CHAT_MESSAGE_INDEX, CHAT_MESSAGE_SCRIPT);

            if (isIndexEmpty(JOB_INDEX)) {
                log.info("job_index 为空，开始初始化数据");
                initJobIndex();
            }
            if (isIndexEmpty(JOB_APPLY_INDEX)) {
                log.info("job_apply_index 为空，开始初始化数据");
                initJobApplyIndex();
            }
            if (isIndexEmpty(CHAT_MESSAGE_INDEX)) {
                log.info("chat_message_index 为空，开始初始化数据");
                initChatMessageIndex();
            }


            initialized.set(true);
            log.info("Elastic Search 索引表初始化成功");
        }


    }

    private void ensureIndex(String index, String mappingScript) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        if (!client.indices().exists(request, RequestOptions.DEFAULT)) {
            create(index, mappingScript);
            log.info("创建 ES 索引：{}", index);
        }
    }

    private boolean isIndexEmpty(String index) throws IOException {
        CountRequest request = new CountRequest(index);
        CountResponse response = client.count(request, RequestOptions.DEFAULT);
        return response.getCount() == 0;
    }

    private void create(String chatMessageIndex, String chatMessageScript) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(chatMessageIndex);
        createIndexRequest.settings(Settings.builder()
                .put("analysis.analyzer.ik_max.type", "custom")
                .put("analysis.analyzer.ik_max.tokenizer", "ik_max_word"));
        createIndexRequest.mapping(chatMessageScript, XContentType.JSON);

        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    private <T> void bulkSave(String index, Function<T, String> idGetter, List<T> docs) throws IOException {
        if (docs == null || docs.isEmpty()) {
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

    private void initJobIndex() throws IOException {
        List<JobElasticsearchDTO> docs =
                jobsClient.initElasticsearch();
        bulkSave(
                JOB_INDEX,
                dto -> dto.getUid().toString(),
                docs
        );

        log.info("job_index 初始化完成，文档数：{}", docs.size());
    }

    private void initJobApplyIndex() throws IOException {
        List<JobApplyElasticsearchDTO> docs =
                getInfoService.queryForElasticsearch();
        bulkSave(
                JOB_APPLY_INDEX,
                dto -> dto.getApplyId().toString(),
                docs
        );

        log.info("job_apply_index 初始化完成，文档数：{}", docs.size());
    }

    private void initChatMessageIndex() throws IOException {
        List<ChatMessageElasticsearchDTO> docs =
                chatsClient.initElasticsearch();
        bulkSave(
                JOB_INDEX,
                dto -> dto.getMessageId().toString(),
                docs
        );

        log.info("chat_message_index 初始化完成，文档数：{}", docs.size());
    }

}
