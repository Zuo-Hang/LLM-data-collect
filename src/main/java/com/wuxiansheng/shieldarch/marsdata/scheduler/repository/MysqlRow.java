package com.wuxiansheng.shieldarch.marsdata.scheduler.repository;

import lombok.Data;

/**
 * 特价车数据表行结构
 */
@Data
public class MysqlRow {
    private Long id;
    private String estimateId;
    private String bubbleImageUrl;
    private String partnerName;
    private Double capPrice;
    private Double reducePrice;
    private String carType;
    private Integer cityId;
    private String cityName;
    private String createTime;
    private String updateTime;
    /**
     * 0: 原始数据, 1: 拟合结果
     */
    private Integer type;
}

