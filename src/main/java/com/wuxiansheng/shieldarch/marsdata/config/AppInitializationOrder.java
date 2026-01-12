package com.wuxiansheng.shieldarch.marsdata.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 应用初始化顺序说明
 * 
 * 使用Spring Boot的自动配置和@PostConstruct，初始化顺序如下：
 * 
 * 1. Spring Boot自动配置阶段
 *    - 加载application.yml配置
 *    - 初始化Spring容器
 *    - 初始化数据源（DataSource）
 *    - 初始化Redis客户端（RedissonClient，如果配置了）
 * 
 * 2. @PostConstruct阶段（按依赖顺序）
 *    - NacosConfigService.init() - Nacos配置中心初始化
 *    - NacosServiceRegistry.init() - Nacos服务注册组件初始化
 *    - BusinessRegistrationConfig.registerDependencies() - 业务注册
 *    - RedisWrapper.initRedisClient() - Redis客户端初始化（如果需要）
 *    - MysqlWrapper.initMysql() - MySQL连接初始化
 *    - ServiceDiscovery初始化（NacosServiceDiscovery）
 *    - SupplierResponseRateService.init() - 供应商应答概率服务初始化（从 MySQL 查询）
 * 
 * 3. @Scheduled任务注册
 *    - PriceFittingTask - 价格拟合任务
 *    - IntegrityCheckTask - 完整性检查任务
 * 
 * 4. 应用启动完成后的操作（CommandLineRunner或ApplicationReadyEvent）
 *    - HTTP服务器启动（Spring Boot自动启动）
 *    - NacosServiceRegistry.registerService() - 服务注册到Nacos（监听WebServerInitializedEvent）
 *    - MQ Producer初始化（Producer.initProducer()）
 *    - MQ Consumer启动（Consumer.startConsumer()）
 *    - 定时任务启动（Spring Boot @EnableScheduling自动启动）
 * 
 * 5. 应用关闭时（@PreDestroy）
 *    - NacosServiceRegistry.deregisterService() - 服务从Nacos注销
 * 
 * 注意：
 * - 服务发现已使用 Nacos 替换 DiSF
 * - 监控已使用 Prometheus + Spring Boot Actuator（替代 Odin）
 */
@Slf4j
@Component
public class AppInitializationOrder {
    
    /**
     * 初始化顺序说明（供参考）
     * 
 * 1. initLogger - 日志初始化
 * 2. initNacos - Nacos配置中心初始化
 * 3. InitConfig - 配置文件加载
     * 4. LoadBaseConfigWithEnv - 基础配置加载
     * 5. InitRedisClient - Redis初始化
     * 6. ServiceDiscovery初始化 - 服务发现初始化（Nacos）
     * 7. SupplierResponseRateService.init() - 供应商应答概率服务初始化
     * 9. InitMysql - MySQL初始化
     * 10. InitProducer - MQ Producer初始化
     * 11. RegisterDependancy - 业务注册
     * 12. scheduler.NewScheduler - 定时任务调度器初始化
     * 13. registerTask - 注册定时任务
     * 
     * 启动顺序：
     * 1. pprof监控启动（Spring Boot Actuator）
     * 2. HTTP服务器启动
     * 3. Prometheus 指标暴露（/actuator/prometheus）
     * 4. MQ Consumer启动
     * 5. 定时任务调度器启动
     */
    public void logInitializationOrder() {
        log.info("应用初始化顺序说明已加载，请参考类注释");
    }
}

