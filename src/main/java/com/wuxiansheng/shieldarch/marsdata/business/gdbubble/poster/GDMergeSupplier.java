package com.wuxiansheng.shieldarch.marsdata.business.gdbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.GDBubbleBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.ReasonSupplierResult;
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
public class GDMergeSupplier implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof GDBubbleBusiness)) {
            return business;
        }
        
        GDBubbleBusiness gb = (GDBubbleBusiness) business;
        
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
                // 优先使用预估价更低的
                if (supplierInfo.getEstPrice() != null && supplierInfo.getEstPrice() > 0.0 &&
                    (finalSupplier.getEstPrice() == null || finalSupplier.getEstPrice() == 0.0 ||
                     supplierInfo.getEstPrice() < finalSupplier.getEstPrice())) {
                    finalSuppliersMap.put(supplierInfo.getSupplier(), supplierInfo);
                    continue;
                }
                // 预估价相同，优先使用一口价更低的
                if (supplierInfo.getCapPrice() != null && supplierInfo.getCapPrice() > 0.0 &&
                    finalSupplier.getEstPrice() != null && supplierInfo.getEstPrice().equals(finalSupplier.getEstPrice()) &&
                    (finalSupplier.getCapPrice() == null || finalSupplier.getCapPrice() == 0.0 ||
                     supplierInfo.getCapPrice() < finalSupplier.getCapPrice())) {
                    finalSuppliersMap.put(supplierInfo.getSupplier(), supplierInfo);
                    continue;
                }
                // 价格相同，优先使用优惠更高的
                if (supplierInfo.getCapPrice() != null && finalSupplier.getCapPrice() != null &&
                    supplierInfo.getCapPrice().equals(finalSupplier.getCapPrice()) &&
                    supplierInfo.getEstPrice() != null && finalSupplier.getEstPrice() != null &&
                    supplierInfo.getEstPrice().equals(finalSupplier.getEstPrice()) &&
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

