package com.boss.bosssearchservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.boss.bosscommon.pojo.dto.ChatMessageElasticsearchDTO;
import com.boss.bosssearchservice.service.ChatMessageIndexSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.boss.bosssearchservice.constants.ChatMessageIndexConstant.CHAT_MESSAGE_INDEX;
import static com.boss.bosssearchservice.util.CanalColumnUtil.*;

@Service
@Slf4j
public class ChatMessageIndexSyncServiceImpl implements ChatMessageIndexSyncService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public void sync(EventType eventType, List<Column> columns) {

        Long messageId = getLong(columns, "id");

        if (eventType == EventType.DELETE) {
            delete(messageId);
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

        save(chatMessageElasticsearchDTO);
    }

    private void save(ChatMessageElasticsearchDTO doc) {
        try {
            IndexRequest request = new IndexRequest(CHAT_MESSAGE_INDEX)
                    .id(doc.getMessageId().toString())
                    .source(JSON.toJSONString(doc), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("同步 chat_message 失败", e);
        }
    }

    private void delete(Long id) {
        try {
            DeleteRequest request = new DeleteRequest(CHAT_MESSAGE_INDEX, id.toString());
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("删除 chat_message 失败", e);
        }
    }
}
