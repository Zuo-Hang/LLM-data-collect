package com.wuxiansheng.shieldarch.marsdata.business.xlbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.xlbubble.ReasonSupplierResult;
import com.wuxiansheng.shieldarch.marsdata.business.xlbubble.XLBubbleBusiness;
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
 * 按价格合并供应商
 */
@Slf4j
@Component
public class XLMergeSupplier implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof XLBubbleBusiness)) {
            return business;
        }
        
        XLBubbleBusiness gb = (XLBubbleBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getReasonResult().getSuppliersInfo() == null) {
            return gb;
        }
        
        // 使用Map按供应商名称去重和合并
        Map<String, ReasonSupplierResult> finalSuppliersMap = new HashMap<>();
        
        for (ReasonSupplierResult supplierInfo : gb.getReasonResult().getSuppliersInfo()) {
            ReasonSupplierResult finalSupplier = finalSuppliersMap.get(supplierInfo.getSupplier());
            
            if (finalSupplier == null) {
                finalSuppliersMap.put(supplierInfo.getSupplier(), supplierInfo);
            } else {
                // 优先使用价格不为空的
                if (supplierInfo.getPrice() != null && supplierInfo.getPrice() > 0.0 && 
                    (finalSupplier.getPrice() == null || finalSupplier.getPrice() == 0.0)) {
                    finalSuppliersMap.put(supplierInfo.getSupplier(), supplierInfo);
                    continue;
                }
                // 优先使用价格更低的
                if (supplierInfo.getPrice() != null && supplierInfo.getPrice() > 0.0 && 
                    finalSupplier.getPrice() != null && finalSupplier.getPrice() > 0.0 &&
                    supplierInfo.getPrice() < finalSupplier.getPrice()) {
                    finalSuppliersMap.put(supplierInfo.getSupplier(), supplierInfo);
                    continue;
                }
                // 价格相同，优先使用优惠更高的
                if (supplierInfo.getPrice() != null && finalSupplier.getPrice() != null &&
                    supplierInfo.getPrice().equals(finalSupplier.getPrice()) &&
                    supplierInfo.getDiscountAmount() != null && finalSupplier.getDiscountAmount() != null &&
                    supplierInfo.getDiscountAmount() > finalSupplier.getDiscountAmount()) {
                    finalSuppliersMap.put(supplierInfo.getSupplier(), supplierInfo);
                    continue;
                }
            }
        }
        
        List<ReasonSupplierResult> finalSuppliers = new ArrayList<>(finalSuppliersMap.values());
        gb.getReasonResult().setSuppliersInfo(finalSuppliers);
        
        return gb;
    }
}

