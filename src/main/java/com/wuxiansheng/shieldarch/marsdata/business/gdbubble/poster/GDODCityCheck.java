package com.wuxiansheng.shieldarch.marsdata.business.gdbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.GDBubbleBusiness;
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
public class GDODCityCheck implements Poster {
    
    @Autowired
    private ODCityCheck odCityCheck;
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof GDBubbleBusiness)) {
            return business;
        }
        
        GDBubbleBusiness gb = (GDBubbleBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getInput() == null) {
            return gb;
        }
        
        // 去除"进站口"（出站口不参与计算，避免干扰相似度计算）
        String startPOI = gb.getReasonResult().getStartPoint();
        if (startPOI != null) {
            startPOI = startPOI.replace("进站口", "");
        }
        
        // 计算相似度阈值（万达广场使用0.3，其他使用0.5）
        double threshold = (startPOI != null && startPOI.contains("万达广场")) ? 0.3 : 0.5;
        
        String cityCheck = odCityCheck.checkODCity(
            startPOI,
            gb.getInput().getCityName(),
            gb.getName(),
            threshold
        );
        
        gb.setCityCheck(cityCheck);
        return gb;
    }
}

