package com.boss.bosssearchservice;

import com.boss.bosscommon.clients.ChatsClient;
import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.clients.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {UserClient.class, JobsClient.class, ChatsClient.class})
public class BossSearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossSearchServiceApplication.class, args);
    }

}
