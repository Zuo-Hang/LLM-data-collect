package com.wuxiansheng.shieldarch.marsdata.business.gdbubble;

import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 供应商推理结果
 */
@Data
public class ReasonSupplierResult {
    
    private String supplier;
    private Double estPrice;      // 预估价
    private Double capPrice;       // 一口价
    private List<Double> priceRange = new ArrayList<>();  // 价格区间
    private String discountType;   // 优惠描述
    private Double discountAmount; // 优惠金额
    private String priceType;      // 价格类型
    private Double otherPrice;     // 其他价格
    private String carType;        // 车型
    
    /**
     * 获取价格（优先使用预估价，其次一口价）
     */
    public Double price() {
        QuestUtils questUtils = new QuestUtils();
        return questUtils.mergeFloat64(
            estPrice != null ? estPrice : 0.0,
            capPrice != null ? capPrice : 0.0);
    }
    
    /**
     * 判断是否为预估价
     */
    public static boolean isEstPrice(String priceType) {
        return "预估".equals(priceType) || "预估价".equals(priceType);
    }
    
    /**
     * 判断是否为一口价
     */
    public static boolean isCapPrice(String priceType) {
        return "一口价".equals(priceType);
    }
}

