package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 距离价格分段
 */
@Data
public class DistancePriceSegment {
    
    @JsonProperty("begin")
    private Double begin;
    
    @JsonProperty("end")
    private Double end;
    
    @JsonProperty("price")
    private Double price;
}

