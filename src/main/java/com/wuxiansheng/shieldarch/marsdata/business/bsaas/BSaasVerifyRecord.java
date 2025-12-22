package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 校验记录
 */
@Data
public class BSaasVerifyRecord {
    
    private Integer flag;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("image_url")
    private String imageURL;
    
    @JsonProperty("order_id")
    private String orderID;
    
    @JsonProperty("page_type")
    private String pageType;
}

