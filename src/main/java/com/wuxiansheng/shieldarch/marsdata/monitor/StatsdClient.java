package com.wuxiansheng.shieldarch.marsdata.monitor;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * StatsD 客户端封装
 * 提供简单的 Counter / Timer 能力，并支持 Tag
 */
@Slf4j
@Component
public class StatsdClient {

    private StatsDClient client;
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public StatsdClient(com.wuxiansheng.shieldarch.marsdata.config.StatsdConfig config) {
        if (!config.isEnabled()) {
            log.info("[StatsdClient] StatsD 未启用，所有指标上报将被忽略");
            this.client = null;
            this.enabled.set(false);
            return;
        }
        try {
            this.client = new NonBlockingStatsDClient(
                    config.getPrefix(),
                    config.getHost(),
                    config.getPort()
            );
            this.enabled.set(true);
            log.info("[StatsdClient] StatsD 初始化成功, host={}, port={}, prefix={}",
                    config.getHost(), config.getPort(), config.getPrefix());
        } catch (Exception e) {
            log.warn("[StatsdClient] StatsD 初始化失败，将不进行指标上报", e);
            this.client = null;
            this.enabled.set(false);
        }
    }

    // 预留 Tag 能力：当前使用的 StatsD 客户端版本不支持 Tag，这里仅保留方法签名（未使用）
    @SuppressWarnings("unused")
    private String[] buildTags(Map<String, String> tags) {
        return new String[0];
    }

    /**
     * 计数型指标（CounterN）
     */
    public void count(String metric, long value, Map<String, String> tags) {
        if (!enabled.get() || client == null) {
            return;
        }
        try {
            client.count(metric, value);
        } catch (Exception e) {
            log.warn("[StatsdClient] 发送 count 指标失败: metric={}, value={}", metric, value, e);
        }
    }

    /**
     * 累加计数（Counter, delta=1）
     */
    public void increment(String metric, Map<String, String> tags) {
        count(metric, 1, tags);
    }

    /**
     * 记录执行时长（毫秒）
     */
    public void timing(String metric, long durationMs, Map<String, String> tags) {
        if (!enabled.get() || client == null) {
            return;
        }
        try {
            client.recordExecutionTime(metric, durationMs);
        } catch (Exception e) {
            log.warn("[StatsdClient] 发送 timing 指标失败: metric={}, durationMs={}", metric, durationMs, e);
        }
    }
    
    /**
     * 
     * @param method 方法名
     * @param sourceUniqueId 源唯一ID
     * @param businessName 业务名称
     * @param durationMs 耗时（毫秒）
     * @param responseCode 响应码（0表示成功，非0表示失败）
     */
    public void recordRpcMetric(String method, String sourceUniqueId, String businessName, long durationMs, int responseCode) {
        if (!enabled.get() || client == null) {
            return;
        }
        try {
            // 记录执行时间
            client.recordExecutionTime(method, durationMs);
            // 记录成功/失败计数
            String statusMetric = method + (responseCode == 0 ? "_success" : "_fail");
            client.incrementCounter(statusMetric);
        } catch (Exception e) {
            log.warn("[StatsdClient] 发送RPC指标失败: method={}, error={}", method, e.getMessage());
        }
    }
    
    /**
     * 
     * @param metric 指标名
     * @param value 值
     * @param tags 标签
     */
    public void recordGauge(String metric, long value, Map<String, String> tags) {
        if (!enabled.get() || client == null) {
            return;
        }
        try {
            client.gauge(metric, value);
        } catch (Exception e) {
            log.warn("[StatsdClient] 发送Gauge指标失败: metric={}, value={}", metric, value, e);
        }
    }
    
    /**
     * 
     * @param metric 指标名
     * @param tags 标签
     */
    public void incrementCounter(String metric, Map<String, String> tags) {
        increment(metric, tags);
    }
}


