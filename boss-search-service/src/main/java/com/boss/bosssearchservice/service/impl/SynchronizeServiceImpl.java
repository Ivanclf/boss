package com.boss.bosssearchservice.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.boss.bosssearchservice.service.ChatMessageIndexSyncService;
import com.boss.bosssearchservice.service.JobApplyIndexSyncService;
import com.boss.bosssearchservice.service.JobIndexSyncService;
import com.boss.bosssearchservice.service.SynchronizeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SynchronizeServiceImpl implements SynchronizeService {

    @Resource
    private JobIndexSyncService jobIndexSyncService;
    @Resource
    private JobApplyIndexSyncService jobApplyIndexSyncService;
    @Resource
    private ChatMessageIndexSyncService chatMessageIndexSyncService;

    @Override
    public void sync(
            String schema,
            String table,
            EventType eventType,
            List<Column> columns) {

        switch (schema) {
            case "job_db" -> {
                if("job".equals(table) || "job_tag".equals(table)) {
                    jobIndexSyncService.sync(eventType, columns);
                }
            }
            case "user_db" -> {
                if("user_job_apply".equals(table)) {
                    jobApplyIndexSyncService.sync(eventType, columns);
                }
            }
            case "chat_db" -> {
                if("chat_record".equals(table)) {
                    chatMessageIndexSyncService.sync(eventType, columns);
                }
            }
        }
    }
}
