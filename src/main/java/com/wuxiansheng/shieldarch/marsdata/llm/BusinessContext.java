package com.wuxiansheng.shieldarch.marsdata.llm;

import lombok.Data;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * 业务上下文
 */
@Data
public class BusinessContext {
    /**
     * 业务配置
     */
    private BusinessConf businessConf;
    
    /**
     * 源配置
     */
    private SourceConf sourceConf;
    
    /**
     * 分类器（可选）
     */
    private Classifier classifier;
    
    /**
     * Spring应用上下文（用于依赖注入）
     */
    private ApplicationContext applicationContext;
    
    /**
     * 业务名称包装器
     * 如果是测试环境，会在业务名称后添加"_test"
     */
    public String businessNameWrapper(String businessName) {
        if (sourceConf != null && sourceConf.isTest()) {
            return businessName + "_test";
        }
        return businessName;
    }
    
    /**
     * 业务配置
     */
    @Data
    public static class BusinessConf {
        /**
         * 业务名称
         */
        private String name;
        
        /**
         * 是否启用
         */
        private boolean enable;
        
        /**
         * 访问大模型的最大并发，0 代表全部
         */
        private int maxConcurrent;
        
        /**
         * 源配置列表
         */
        private List<SourceConf> sources;
        
        /**
         * 根据sourceUniqueId获取源配置
         */
        public SourceConf getSourceConf(String sourceUniqueId) {
            if (sources == null) {
                return null;
            }
            for (SourceConf sourceConf : sources) {
                if (sourceUniqueId.equals(sourceConf.getUniqueId())) {
                    return sourceConf;
                }
            }
            return null;
        }
    }
    
    /**
     * 源配置
     */
    @Data
    public static class SourceConf {
        /**
         * 唯一ID
         */
        private String uniqueId;
        
        /**
         * 是否测试环境
         */
        private boolean test;
        
        /**
         * 重要等级 P0, P1, P2
         */
        private int level;
    }
    
    /**
     * 分类器接口
     */
    public interface Classifier {
        /**
         * 不存在时，返回空数组
         */
        String[] get(String url);
    }
}

