package com.wuxiansheng.shieldarch.marsdata.llm;

import java.util.List;

/**
 * 业务接口
 */
public interface Business {
    /**
     * 链路名称
     */
    String getName();
    
    /**
     * 消息时间戳，单位：秒
     * 不要使用识别出来的时间戳
     */
    long getMsgTimestamp();
    
    /**
     * 推理请求列表
     */
    List<ReasonRequest> getReasonRequests();
    
    /**
     * 合并 LLM 返回结果
     */
    void merge(List<ReasonResponse> results) throws Exception;
}

