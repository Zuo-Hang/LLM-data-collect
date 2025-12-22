package com.wuxiansheng.shieldarch.marsdata.llm;

import lombok.Data;

/**
 * 推理响应
 */
@Data
public class ReasonResponse {
    /**
     * 推理上下文
     */
    private ReasonRequest.ReasonContext context;
    
    /**
     * 用户在提示词中定义的JSON格式内容
     */
    private String content;
    
    /**
     * 错误信息
     */
    private Exception error;
    
    /**
     * 检查是否有错误
     */
    public boolean hasError() {
        return error != null;
    }
}

