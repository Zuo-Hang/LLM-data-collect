package com.wuxiansheng.shieldarch.marsdata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LLM Data Collect Service - Java Version
 * 主启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.wuxiansheng.shieldarch.marsdata.business.*.sinker")
public class LLMDataCollectApplication {

    public static void main(String[] args) {
        SpringApplication.run(LLMDataCollectApplication.class, args);
    }
}

