package com.wuxiansheng.shieldarch.marsdata.utils;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;

/**
 * StatsD工具类
 */
@Slf4j
@Component
public class StatsDUtils {
    
    @Value("${statsd.host:localhost}")
    private String statsdHost;
    
    @Value("${statsd.port:8125}")
    private int statsdPort;
    
    @Value("${statsd.prefix:llm-data-collect}")
    private String statsdPrefix;
    
    private StatsDClient statsDClient;
    
    @PostConstruct
    public void init() {
        try {
            statsDClient = new NonBlockingStatsDClient(statsdPrefix, statsdHost, statsdPort);
            log.info("StatsD客户端初始化成功: host={}, port={}, prefix={}", statsdHost, statsdPort, statsdPrefix);
        } catch (Exception e) {
            log.error("StatsD客户端初始化失败", e);
        }
    }
    
    /**
     * 上报Counter指标
     * 
     * @param metric 指标名称
     * @param tags 标签
     */
    public void counter(String metric, Map<String, String> tags) {
        if (statsDClient == null) {
            return;
        }
        
        try {
            String tagString = buildTagString(tags);
            String fullMetric = tagString.isEmpty() ? metric : metric + tagString;
            statsDClient.incrementCounter(fullMetric);
        } catch (Exception e) {
            log.warn("上报StatsD指标失败: metric={}, tags={}", metric, tags, e);
        }
    }
    
    /**
     * 上报Counter指标（带数量）
     * 
     * @param metric 指标名称
     * @param count 数量
     * @param tags 标签
     */
    public void counterN(String metric, long count, Map<String, String> tags) {
        if (statsDClient == null) {
            return;
        }
        
        try {
            String tagString = buildTagString(tags);
            String fullMetric = tagString.isEmpty() ? metric : metric + tagString;
            statsDClient.count(fullMetric, count);
        } catch (Exception e) {
            log.warn("上报StatsD指标失败: metric={}, count={}, tags={}", metric, count, tags, e);
        }
    }
    
    /**
     * 构建标签字符串
     */
    private String buildTagString(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return "|#" + sb.toString();
    }
    
    @PreDestroy
    public void shutdown() {
        if (statsDClient != null) {
            statsDClient.stop();
            log.info("StatsD客户端已关闭");
        }
    }
}


