package com.wuxiansheng.shieldarch.marsdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MySQL配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mysql")
public class MysqlConfig {
    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 数据库地址
     */
    private String addr;

    /**
     * DiSF服务名
     */
    private String disfName;

    /**
     * 最大打开连接数
     */
    private Integer maxOpenConn = 100;

    /**
     * 最大空闲连接数
     */
    private Integer maxIdleConn = 10;

    /**
     * 连接最大生存时间（秒）
     */
    private Integer maxLifeTime = 3600;
}
