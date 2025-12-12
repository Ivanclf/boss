package com.boss.bosschatservice;

import com.boss.bosscommon.clients.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = UserClient.class)
public class BossChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossChatServiceApplication.class, args);
    }

}
