package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 高德特价车推理结果
 */
@Data
public class GDSpecialPriceReasonResult {
    
    /**
     * 供应商信息列表，包含所有识别到的特价车供应商
     */
    private List<ReasonSupplierResult> suppliersInfo = new ArrayList<>();
    
    /**
     * 合并两个推理结果
     */
    public GDSpecialPriceReasonResult merge(GDSpecialPriceReasonResult other) {
        if (other == null || other == this) {
            return this;
        }
        
        GDSpecialPriceReasonResult res = new GDSpecialPriceReasonResult();
        List<ReasonSupplierResult> mergedSuppliers = new ArrayList<>();
        
        if (this.suppliersInfo != null) {
            mergedSuppliers.addAll(this.suppliersInfo);
        }
        if (other.suppliersInfo != null) {
            mergedSuppliers.addAll(other.suppliersInfo);
        }
        
        res.setSuppliersInfo(mergedSuppliers);
        return res;
    }
}

