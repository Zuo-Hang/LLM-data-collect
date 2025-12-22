package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 个人主页
 */
@Data
public class BSaasPersonalHomepage {
    
    @JsonProperty("driver_name")
    private String driverName;
    
    @JsonProperty("car_number")
    private String carNumber;
    
    @JsonProperty("car_type")
    private String carType;
    
    @JsonProperty("image_url")
    private String imageURL;
    
    @JsonProperty("image_index")
    private Integer imageIndex;
}

