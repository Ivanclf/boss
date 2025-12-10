package com.boss.bossuserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BossUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BossUserServiceApplication.class, args);
    }

}
