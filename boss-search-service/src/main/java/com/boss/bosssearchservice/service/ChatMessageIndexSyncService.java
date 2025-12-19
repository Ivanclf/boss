package com.boss.bosssearchservice.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface ChatMessageIndexSyncService {
    void sync(CanalEntry.EventType eventType, List<CanalEntry.Column> columns);
}
