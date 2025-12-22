package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 时段分段
 */
@Data
public class Segment {
    
    @JsonProperty("begin")
    private String begin;
    
    @JsonProperty("end")
    private String end;
    
    @JsonProperty("price")
    private Double price;
}

