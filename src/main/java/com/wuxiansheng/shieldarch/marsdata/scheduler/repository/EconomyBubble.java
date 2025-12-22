package com.wuxiansheng.shieldarch.marsdata.scheduler.repository;

import lombok.Data;

/**
 * 经济型冒泡数据
 */
@Data
public class EconomyBubble {
    private String estimateId;
    private String partnerName;
    /**
     * 经济型实付
     */
    private Double estPay;
    private Integer cityId;
    private String cityName;
}

