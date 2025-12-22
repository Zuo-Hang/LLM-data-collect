package com.wuxiansheng.shieldarch.marsdata.llm;

import lombok.Data;
import java.util.Map;

/**
 * 推理请求
 */
@Data
public class ReasonRequest {
    /**
     * 推理上下文
     */
    private ReasonContext context;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 图片URL，可以为空
     */
    private String picUrl;
    
    /**
     * 推理上下文
     */
    @Data
    public static class ReasonContext {
        /**
         * 自定义Map
         */
        private Map<String, Object> customMap;
    }
}

