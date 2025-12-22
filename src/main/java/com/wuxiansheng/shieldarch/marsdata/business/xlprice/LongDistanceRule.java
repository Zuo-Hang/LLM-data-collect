package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 长途规则
 */
@Data
public class LongDistanceRule {
    
    @JsonProperty("defaultConfig")
    private List<DistancePriceSegment> defaultConfig = new ArrayList<>();
    
    @JsonProperty("segmentList")
    private List<TimeSegment> segmentList = new ArrayList<>();
    
    /**
     * 获取时段详情
     * 
     * @param hour 小时（0-23）
     * @return 距离价格分段列表
     */
    public List<DistancePriceSegment> periodDetail(int hour) {
        String hourStr = reFormatHour(hour);
        
        if (segmentList != null) {
            for (TimeSegment seg : segmentList) {
                if (seg.getStartTime() != null && seg.getEndTime() != null &&
                    seg.getStartTime().compareTo(hourStr) <= 0 && hourStr.compareTo(seg.getEndTime()) < 0) {
                    return seg.getSegmentPrice() != null ? seg.getSegmentPrice() : new ArrayList<>();
                }
            }
        }
        
        return defaultConfig != null ? defaultConfig : new ArrayList<>();
    }
    
    /**
     * 距离单位：m -> km, 价格单位：分 -> 元
     */
    public void refactorDisUnit() {
        if (defaultConfig != null) {
            for (DistancePriceSegment config : defaultConfig) {
                if (config.getBegin() != null) {
                    config.setBegin(config.getBegin() / 1000);
                }
                if (config.getEnd() != null) {
                    config.setEnd(config.getEnd() / 1000);
                }
                if (config.getPrice() != null) {
                    config.setPrice(config.getPrice() / 100);
                }
            }
        }
        
        if (segmentList != null) {
            for (TimeSegment seg : segmentList) {
                if (seg.getSegmentPrice() != null) {
                    for (DistancePriceSegment innerSeg : seg.getSegmentPrice()) {
                        if (innerSeg.getBegin() != null) {
                            innerSeg.setBegin(innerSeg.getBegin() / 1000);
                        }
                        if (innerSeg.getEnd() != null) {
                            innerSeg.setEnd(innerSeg.getEnd() / 1000);
                        }
                        if (innerSeg.getPrice() != null) {
                            innerSeg.setPrice(innerSeg.getPrice() / 100);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 格式化小时为字符串
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

