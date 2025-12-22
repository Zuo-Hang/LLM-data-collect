package com.wuxiansheng.shieldarch.marsdata.llm;

/**
 * Sinker接口
 * 用于将处理后的业务数据下沉到存储系统（Hive、MySQL等）
 */
public interface Sinker {
    /**
     * 下沉业务数据
     * 
     * @param bctx 业务上下文
     * @param business 业务对象
     * @throws Exception 下沉失败时抛出异常
     */
    void sink(BusinessContext bctx, Business business) throws Exception;
}

