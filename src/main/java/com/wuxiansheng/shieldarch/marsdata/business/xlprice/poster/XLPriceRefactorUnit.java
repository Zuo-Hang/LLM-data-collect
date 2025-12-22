package com.wuxiansheng.shieldarch.marsdata.business.xlprice.poster;

import com.wuxiansheng.shieldarch.marsdata.business.xlprice.XLPriceRuleBusiness;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 重构单位：m -> km，分 -> 元
 */
@Slf4j
@Component
public class XLPriceRefactorUnit implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof XLPriceRuleBusiness)) {
            return business;
        }
        
        XLPriceRuleBusiness gb = (XLPriceRuleBusiness) business;
        
        if (gb.getData() != null) {
            gb.getData().refactorUnit();
        }
        
        return gb;
    }
}

