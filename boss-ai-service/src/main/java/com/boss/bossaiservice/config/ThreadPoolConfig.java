package com.boss.bossaiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    
    @Bean("interviewTaskExecutor")
    public ThreadPoolExecutor interviewThreadPoolExecutor() {
        return new ThreadPoolExecutor(5, 10, 1L, TimeUnit.HOURS, new ArrayBlockingQueue<>(20));
    }

    @Bean("chatSaveTaskExecutor")
    public ThreadPoolExecutor chatSaveThreadPoolExecutor() {
        return new ThreadPoolExecutor(5, 20, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(40));
    }
}