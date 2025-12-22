package com.wuxiansheng.shieldarch.marsdata.scheduler.repository;

import lombok.Data;

/**
 * 完整性校验分组结果
 */
@Data
public class IntegrityCheckGroupResult {

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 城市 ID
     */
    private Integer cityId;

    /**
     * 里程段
     */
    private String disRange;

    /**
     * 统计数量
     */
    private Integer count;
}


