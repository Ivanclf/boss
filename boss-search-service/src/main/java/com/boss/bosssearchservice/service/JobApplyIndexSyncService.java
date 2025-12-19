package com.boss.bosssearchservice.service;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;

import java.util.List;

public interface JobApplyIndexSyncService {
    void sync(EventType eventType, List<Column> columns);
}
