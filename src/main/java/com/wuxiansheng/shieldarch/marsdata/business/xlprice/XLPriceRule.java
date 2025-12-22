package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 小拉价格规则
 */
@Data
public class XLPriceRule {
    
    @JsonProperty("defaultPrice")
    private Double defaultPrice;
    
    @JsonProperty("segmentList")
    private List<Segment> segmentList = new ArrayList<>();
    
    /**
     * 距离单位：m -> km, 价格单位：分 -> 元
     */
    public void refactorUnit() {
        if (defaultPrice != null) {
            defaultPrice = defaultPrice / 100;
        }
        if (segmentList != null) {
            for (Segment seg : segmentList) {
                if (seg.getPrice() != null) {
                    seg.setPrice(seg.getPrice() / 100);
                }
            }
        }
    }
    
    /**
     * 获取时段价格
     * 
     * @param hour 小时（0-23）
     * @return 价格
     */
    public double periodPrice(int hour) {
        String hourStr = reFormatHour(hour);
        
        if (segmentList != null) {
            for (Segment seg : segmentList) {
                if (seg.getBegin() != null && seg.getEnd() != null &&
                    seg.getBegin().compareTo(hourStr) <= 0 && hourStr.compareTo(seg.getEnd()) < 0) {
                    return seg.getPrice() != null ? seg.getPrice() : 0.0;
                }
            }
        }
        
        return defaultPrice != null ? defaultPrice : 0.0;
    }
    
    /**
     * 格式化小时为字符串
     * 1 -> 01:00, 10 -> 10:00
     */
    private String reFormatHour(int hour) {
        if (hour < 0 || hour > 23) {
            hour = 0;
        }
        
        String hourStr = String.valueOf(hour);
        if (hourStr.length() == 1) {
            hourStr = "0" + hourStr;
        }
        return hourStr + ":00";
    }
}

