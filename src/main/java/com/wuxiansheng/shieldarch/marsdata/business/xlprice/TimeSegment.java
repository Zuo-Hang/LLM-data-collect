package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 时段分段
 */
@Data
public class TimeSegment {
    
    @JsonProperty("startTime")
    private String startTime;
    
    @JsonProperty("endTime")
    private String endTime;
    
    @JsonProperty("segmentPrice")
    private List<DistancePriceSegment> segmentPrice = new ArrayList<>();
}

