package com.wuxiansheng.shieldarch.marsdata.business.couponsp;

import lombok.Data;

/**
 * 券包推理结果
 */
@Data
public class CouponReasonResult {
    
    private String pageCategory;  // 列表页
    private String couponName;    // 打车券名称
    private String deadline;       // 有效期截止日期
    private String couponType;    // 券类型
    private Double discount;      // 折扣信息
    private Double cap;           // 封顶金额
    private Double threshold;     // 使用门槛
    private String validDays;     // 有效天数
    private String validPeriod;   // 有效时段
    private String supplierRule;  // 供应商规则
    private String validChannel;  // 有效渠道
    private String validCity;     // 有效城市
    private String validCarType;  // 有效车型
    private String validOrder;    // 有效订单类型
    private String validRoute;    // 有效路线
}

