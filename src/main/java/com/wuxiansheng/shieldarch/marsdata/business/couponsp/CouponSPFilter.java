package com.wuxiansheng.shieldarch.marsdata.business.couponsp;

import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 券包人群标签识别过滤器
 */
@Slf4j
@Component
public class CouponSPFilter implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof CouponSpecialPopulationBusiness)) {
            return business;
        }
        
        CouponSpecialPopulationBusiness cs = (CouponSpecialPopulationBusiness) business;
        
        List<CouponReasonResult> filteredResults = new ArrayList<>();
        for (CouponReasonResult reasonResult : cs.getReasonResults()) {
            // 只保留券包详情页
            if (!"券包详情页".equals(reasonResult.getPageCategory())) {
                continue;
            }
            // 券名称不能为空
            if (reasonResult.getCouponName() == null || reasonResult.getCouponName().isEmpty()) {
                continue;
            }
            filteredResults.add(reasonResult);
        }
        
        cs.setReasonResults(filteredResults);
        return cs;
    }
}

