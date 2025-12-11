package com.boss.bossuserservice;

import com.boss.bosscommon.clients.JobsClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = JobsClient.class)
public class BossUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossUserServiceApplication.class, args);
    }

}
