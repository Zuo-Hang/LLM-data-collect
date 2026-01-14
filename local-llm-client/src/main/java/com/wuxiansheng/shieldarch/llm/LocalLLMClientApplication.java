package com.wuxiansheng.shieldarch.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 本地大模型客户端应用
 * 
 * 独立模块，用于调用本地部署的大模型（如Ollama）
 * 最小依赖，可以独立启动
 */
@SpringBootApplication
public class LocalLLMClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalLLMClientApplication.class, args);
    }
}

