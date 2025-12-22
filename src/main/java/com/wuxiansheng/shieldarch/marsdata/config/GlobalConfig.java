package com.wuxiansheng.shieldarch.marsdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 全局配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class GlobalConfig {
    /**
     * HTTP服务端口
     */
    private String httpPort = ":8080";

    /**
     * Pprof监控端口
     */
    private String pprofPort = ":6060";

    /**
     * 环境标识 (dev/test/prod)
     */
    private String env = "prod";
}
