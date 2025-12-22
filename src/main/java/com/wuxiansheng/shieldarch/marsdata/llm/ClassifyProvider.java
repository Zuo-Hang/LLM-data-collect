package com.wuxiansheng.shieldarch.marsdata.llm;

/**
 * 分类提供者接口
 */
public interface ClassifyProvider {
    /**
     * 获取分类器
     */
    BusinessContext.Classifier getClassifier();
}

