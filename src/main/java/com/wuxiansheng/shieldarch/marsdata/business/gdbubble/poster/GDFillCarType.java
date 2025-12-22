package com.wuxiansheng.shieldarch.marsdata.business.gdbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.GDBubbleBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.ReasonSupplierResult;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 填充车型
 */
@Slf4j
@Component
public class GDFillCarType implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof GDBubbleBusiness)) {
            return business;
        }
        
        GDBubbleBusiness gb = (GDBubbleBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getReasonResult().getSuppliersInfo() == null) {
            return gb;
        }
        
        for (ReasonSupplierResult supplierInfo : gb.getReasonResult().getSuppliersInfo()) {
            if ("更快应答车".equals(supplierInfo.getSupplier())) {
                supplierInfo.setCarType("虚拟品类");
            } else {
                supplierInfo.setCarType("经济型");
            }
        }
        
        return gb;
    }
}

