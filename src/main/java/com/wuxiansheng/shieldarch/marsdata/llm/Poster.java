package com.wuxiansheng.shieldarch.marsdata.llm;

/**
 * Poster接口
 * 用于对业务数据进行后处理（过滤、转换等）
 */
@FunctionalInterface
public interface Poster {
    /**
     * 处理业务数据
     * 
     * @param bctx 业务上下文
     * @param business 业务对象
     * @return 处理后的业务对象
     */
    Business apply(BusinessContext bctx, Business business);
}

