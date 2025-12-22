package com.wuxiansheng.shieldarch.marsdata.business.xlbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.xlbubble.XLBubbleBusiness;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import com.wuxiansheng.shieldarch.marsdata.llm.poster.ODCityCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * OD城市检查
 */
@Slf4j
@Component
public class XLODCityCheck implements Poster {
    
    @Autowired
    private ODCityCheck odCityCheck;
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof XLBubbleBusiness)) {
            return business;
        }
        
        XLBubbleBusiness gb = (XLBubbleBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getInput() == null) {
            return gb;
        }
        
        String cityCheck = odCityCheck.checkODCity(
            gb.getReasonResult().getStartPoint(),
            gb.getInput().getCityName(),
            gb.getName(),
            0.5
        );
        
        gb.setCityCheck(cityCheck);
        return gb;
    }
}

