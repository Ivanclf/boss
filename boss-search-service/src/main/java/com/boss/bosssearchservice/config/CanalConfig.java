package com.boss.bosssearchservice.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.boss.bosssearchservice.util.CanalUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

import static java.lang.Thread.sleep;

@Component
@Slf4j
public class CanalConfig implements InitializingBean, DisposableBean {

    @Value("${canal.host}")
    private String host;
    @Value("${canal.port}")
    private Integer port;
    @Value("${canal.destination}")
    private String destination;

    @Resource
    private CanalUtil canalUtil;

    private CanalConnector connector;

    @Override
    public void afterPropertiesSet() throws Exception {
        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port), destination, "", ""
        );
        connector.connect();
        connector.subscribe(".*\\..*");
        connector.rollback();
        log.info("canal 链接成功");

    }
    @Override
    public void destroy() throws Exception {
        connector.disconnect();
    }

    private void process() throws InterruptedException {
        while (true) {
            Message message = connector.getWithoutAck(100);
            long batchId = message.getId();

            if(batchId == -1 || message.getEntries().isEmpty()) {
                sleep(1000);
                continue;
            }

            try {
                canalUtil.handleEntries(message.getEntries());
                connector.ack(batchId);
            } catch (Exception e) {
                connector.rollback(batchId);
                log.error("canal 处理时发生了错误");
            }
        }
    }
}