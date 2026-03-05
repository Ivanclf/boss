package com.boss.bosssearchservice.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.boss.bosssearchservice.util.CanalUtil;
import jakarta.annotation.Resource;
import lombok.Getter;
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

    /**
     * -- GETTER --
     *  获取 Canal 连接器
     */
    @Getter
    private CanalConnector connector;

    @Override
    public void afterPropertiesSet() {
        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port), destination, "", ""
        );
        connector.connect();
        connector.subscribe(".*\\..*");
        connector.rollback();
        log.info("canal 链接成功");

    }
    
    @Override
    public void destroy() {
        if (connector != null) {
            connector.disconnect();
            log.info("canal 连接已关闭");
        }
    }

}