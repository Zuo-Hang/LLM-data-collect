package com.wuxiansheng.shieldarch.marsdata.business.xlbubble;

import lombok.Data;

/**
 * 供应商推理结果
 */
@Data
public class ReasonSupplierResult {
    
    private String supplier;
    private Double discountAmount;
    private String priceType;
    private Double price;
}

