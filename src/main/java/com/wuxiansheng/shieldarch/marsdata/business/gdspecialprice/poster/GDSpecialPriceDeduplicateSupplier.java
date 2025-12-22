package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.poster;

import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.GDSpecialPriceBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.ReasonSupplierResult;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商去重，避免多个图片识别到相同供应商导致重复
 */
@Slf4j
@Component
public class GDSpecialPriceDeduplicateSupplier implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof GDSpecialPriceBusiness)) {
            log.warn("DeduplicateSupplier: invalid business type: {}", business != null ? business.getClass() : "null");
            return business;
        }
        
        GDSpecialPriceBusiness gb = (GDSpecialPriceBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getReasonResult().getSuppliersInfo() == null) {
            return gb;
        }
        
        if (gb.getReasonResult().getSuppliersInfo().isEmpty()) {
            return gb;
        }
        
        int originalCount = gb.getReasonResult().getSuppliersInfo().size();
        
        // 使用map去重，保留价格更低的供应商
        Map<String, ReasonSupplierResult> supplierMap = new HashMap<>();
        for (ReasonSupplierResult supplierInfo : gb.getReasonResult().getSuppliersInfo()) {
            String supplierName = supplierInfo.getSupplier();
            ReasonSupplierResult existingSupplier = supplierMap.get(supplierName);
            
            if (existingSupplier == null || 
                (supplierInfo.getCapPrice() != null && existingSupplier.getCapPrice() != null &&
                 supplierInfo.getCapPrice() < existingSupplier.getCapPrice())) {
                // 首次出现或价格更低时更新
                supplierMap.put(supplierName, supplierInfo);
            }
        }
        
        // 重新构建供应商列表
        List<ReasonSupplierResult> deduplicatedSuppliers = new ArrayList<>(supplierMap.values());
        
        // 记录去重结果
        if (deduplicatedSuppliers.size() < originalCount) {
            log.info("供应商去重: 业务={}, 问卷ID={}, 原始={}, 去重后={}",
                business.getName(), gb.getInput() != null ? gb.getInput().getEstimateId() : "unknown",
                originalCount, deduplicatedSuppliers.size());
        }
        
        gb.getReasonResult().setSuppliersInfo(deduplicatedSuppliers);
        return gb;
    }
}

