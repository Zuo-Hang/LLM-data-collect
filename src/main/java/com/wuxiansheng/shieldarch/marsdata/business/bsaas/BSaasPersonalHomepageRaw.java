package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 个人主页原始数据
 */
@Data
public class BSaasPersonalHomepageRaw {
    
    @JsonProperty("driver_name")
    private String driverName;
    
    @JsonProperty("car_number")
    private String carNumber;
    
    @JsonProperty("car_type")
    private String carType;
    
    public BSaasPersonalHomepage toModel(String imageURL, int imageIndex) {
        BSaasPersonalHomepage homepage = new BSaasPersonalHomepage();
        homepage.setDriverName(this.driverName);
        homepage.setCarNumber(this.carNumber);
        homepage.setCarType(this.carType);
        homepage.setImageURL(imageURL);
        homepage.setImageIndex(imageIndex);
        return homepage;
    }
}

