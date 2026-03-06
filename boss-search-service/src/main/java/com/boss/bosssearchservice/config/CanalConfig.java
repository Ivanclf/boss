package com.boss.bosssearchservice.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@Slf4j
public class CanalConfig {

    @Value("${canal.host}")
    private String host;
    @Value("${canal.port}")
    private Integer port;
    @Value("${canal.destination}")
    private String destination;

    /**
     *  获取 Canal 连接器
     */
    @Getter
    private CanalConnector connector;

    @PostConstruct
    public void init() {
        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port), destination, "", ""
        );
        connector.connect();
        connector.subscribe(".*\\..*");
        connector.rollback();
        log.info("Canal 连接成功");
    }
    
    @PreDestroy
    public void destroy() {
        if (connector != null) {
            connector.disconnect();
            log.info("Canal 连接已关闭");
        }
    }
}