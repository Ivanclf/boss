package com.boss.bosssearchservice.util;

import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.boss.bosssearchservice.service.SynchronizeService;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CanalUtil {

    @Resource
    private SynchronizeService synchronizeService;

    public void handleEntries(List<Entry> entries) throws InvalidProtocolBufferException {
        for(Entry entry: entries) {
            if(entry.getEntryType() != EntryType.ROWDATA) {
                continue;
            }

            RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
            String schema = entry.getHeader().getSchemaName();
            String table = entry.getHeader().getTableName();
            EventType eventType = rowChange.getEventType();

            for(RowData rowData  : rowChange.getRowDatasList()) {
                List<Column> columns = eventType == EventType.DELETE
                        ? rowData.getBeforeColumnsList()
                        : rowData.getAfterColumnsList();

                synchronizeService.sync(schema, table, eventType, columns);
            }
        }
    }
}
