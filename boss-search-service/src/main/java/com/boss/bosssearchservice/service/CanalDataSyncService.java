package com.boss.bosssearchservice.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.boss.bosssearchservice.config.CanalConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Canal 数据同步服务
 */
@Service
@Slf4j
public class CanalDataSyncService {

    @Resource
    private CanalConfig canalConfig;
    
    @Resource
    private SynchronizeService synchronizeService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread syncThread;

    /**
     * 启动数据同步
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            syncThread = new Thread(this::syncLoop, "canal-data-sync-thread");
            syncThread.start();
            log.info("Canal 数据同步线程已启动");
        }
    }

    /**
     * 停止数据同步
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (syncThread != null && syncThread.isAlive()) {
                syncThread.interrupt();
                try {
                    syncThread.join(5000);
                    log.info("Canal 数据同步线程已停止");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("等待 Canal 线程关闭时被打断", e);
                }
            }
        }
    }

    /**
     * 数据同步循环
     */
    private void syncLoop() {
        log.info("Canal 数据处理线程开始运行");
        try {
            while (running.get()) {
                try {
                    Message message = canalConfig.getConnector().getWithoutAck(100);
                    long batchId = message.getId();

                    // 没有新数据
                    if (batchId == -1 || message.getEntries().isEmpty()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    try {
                        processEntries(message.getEntries());
                        canalConfig.getConnector().ack(batchId);
                        log.debug("Canal 批次 {} 处理成功", batchId);
                    } catch (Exception e) {
                        canalConfig.getConnector().rollback(batchId);
                        log.error("Canal 处理批次 {} 时发生错误", batchId, e);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Canal 数据处理线程被中断");
                    break;
                } catch (Exception e) {
                    log.error("Canal 数据处理发生异常", e);
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Canal 数据处理线程被中断", e);
        } finally {
            log.info("Canal 数据处理线程退出");
        }
    }

    /**
     * 处理 Canal Entry 列表
     */
    private void processEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            // 只处理行数据变更事件
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                continue;
            }

            try {
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                String schema = entry.getHeader().getSchemaName();
                String table = entry.getHeader().getTableName();
                CanalEntry.EventType eventType = rowChange.getEventType();

                // 根据事件类型选择数据源（删除用 before，增改用 after）
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    List<CanalEntry.Column> columns = eventType == CanalEntry.EventType.DELETE
                            ? rowData.getBeforeColumnsList()
                            : rowData.getAfterColumnsList();
                    
                    synchronizeService.sync(schema, table, eventType, columns);
                }
            } catch (InvalidProtocolBufferException e) {
                log.error("解析 Canal Entry 时发生协议缓冲区错误", e);
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                log.error("处理 Canal Entry 时发生运行时错误", e);
                throw e;
            } catch (Exception e) {
                log.error("处理 Canal Entry 时发生错误", e);
                throw new RuntimeException(e);
            }
        }
    }
}
