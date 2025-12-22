package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice;

import lombok.Data;

/**
 * 供应商特价车信息
 */
@Data
public class ReasonSupplierResult {
    
    /**
     * 供应商名称，如"曹操出行"、"星徽出行"等
     */
    private String supplier;
    
    /**
     * 一口价，特价车的实际价格
     */
    private Double capPrice;
    
    /**
     * 优惠金额，特价车相比原价的优惠数额
     */
    private Double reducePrice;
}

