package com.boss.bosssearchservice.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface SynchronizeService {
    void sync(String schema, String table, CanalEntry.EventType eventType, List<CanalEntry.Column> columns);
}
