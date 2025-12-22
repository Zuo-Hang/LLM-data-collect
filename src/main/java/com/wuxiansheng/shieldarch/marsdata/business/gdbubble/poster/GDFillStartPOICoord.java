package com.wuxiansheng.shieldarch.marsdata.business.gdbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.GDBubbleBusiness;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 填充起点POI坐标
 * 
 * TODO: 需要实现POI服务后，才能完整实现此功能
 */
@Slf4j
@Component
public class GDFillStartPOICoord implements Poster {
    
    // TODO: 注入POI服务
    // @Autowired
    // private POIService poiService;
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof GDBubbleBusiness)) {
            return business;
        }
        
        GDBubbleBusiness gb = (GDBubbleBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getInput() == null) {
            return gb;
        }
        
        // TODO: 实现POI坐标查询
        // String startLNG, String startLAT, err = poiService.getPOICoordinate(
        //     gb.getInput().getCityId(), 
        //     gb.getReasonResult().getStartPoint(), 
        //     gb.getName()
        // );
        // if (err == null) {
        //     gb.setStartLNG(startLNG);
        //     gb.setStartLAT(startLAT);
        // }
        
        // 临时实现：设置为空
        gb.setStartLNG("");
        gb.setStartLAT("");
        
        return gb;
    }
}

