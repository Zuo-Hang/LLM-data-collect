package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 小拉规则响应
 */
@Data
public class XLRuleResponse {
    
    @JsonProperty("ret")
    private Integer ret;
    
    @JsonProperty("msg")
    private String msg;
    
    @JsonProperty("data")
    private XLRuleData data;
}

