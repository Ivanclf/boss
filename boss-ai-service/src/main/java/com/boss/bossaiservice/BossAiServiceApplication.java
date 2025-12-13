package com.boss.bossaiservice;

import com.boss.bosscommon.clients.ChatsClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = ChatsClient.class)
public class BossAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossAiServiceApplication.class, args);
    }

}
