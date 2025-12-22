package com.wuxiansheng.shieldarch.marsdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * StatsD 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "statsd")
public class StatsdConfig {

    /**
     * 是否启用 StatsD 上报
     */
    private boolean enabled = false;

    /**
     * StatsD 服务端 host
     */
    private String host = "127.0.0.1";

    /**
     * StatsD 服务端端口
     */
    private int port = 8125;

    /**
     * 指标前缀
     */
    private String prefix = "llm_data_collect";
}


