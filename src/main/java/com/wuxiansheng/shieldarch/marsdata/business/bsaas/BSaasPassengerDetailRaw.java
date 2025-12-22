package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 乘客费用明细原始数据
 */
@Data
public class BSaasPassengerDetailRaw {
    
    private String time;
    
    @JsonProperty("passenger_pay")
    private Double passengerPay;
    
    @JsonProperty("passenger_order_price")
    private Double passengerOrderPrice;
    
    @JsonProperty("start_fee_sf")
    private Double startFeeSf;
    
    @JsonProperty("start_dis")
    private Double startDis;
    
    @JsonProperty("dis_charge_sf")
    private Double disChargeSf;
    
    @JsonProperty("over_distance")
    private Double overDistance;
    
    @JsonProperty("dur_charge_sf")
    private Double durChargeSf;
    
    @JsonProperty("over_duration")
    private Double overDuration;
    
    @JsonProperty("long_fee_sf")
    private Double longFeeSf;
    
    @JsonProperty("long_dis")
    private Double longDis;
    
    @JsonProperty("dynamic_fee_sf")
    private Double dynamicFeeSf;
    
    @JsonProperty("dynamic_factor")
    private Double dynamicFactor;
    
    @JsonProperty("passenger_discount")
    private Double passengerDiscount;
    
    @JsonProperty("product_type")
    private String productType;
    
    @JsonProperty("passenger_other_fee")
    private String passengerOtherFee;
    
    public BSaasPassengerDetail toModel(String imageURL, int imageIndex) {
        BSaasPassengerDetail detail = new BSaasPassengerDetail();
        detail.setTime(this.time);
        detail.setPassengerPay(this.passengerPay);
        detail.setPassengerOrderPrice(this.passengerOrderPrice);
        detail.setStartFeeSf(this.startFeeSf);
        detail.setStartDis(this.startDis);
        detail.setDisChargeSf(this.disChargeSf);
        detail.setOverDistance(this.overDistance);
        detail.setDurChargeSf(this.durChargeSf);
        detail.setOverDuration(this.overDuration);
        detail.setLongFeeSf(this.longFeeSf);
        detail.setLongDis(this.longDis);
        detail.setDynamicFeeSf(this.dynamicFeeSf);
        detail.setDynamicFactor(this.dynamicFactor);
        detail.setPassengerDiscount(this.passengerDiscount);
        detail.setProductType(this.productType);
        detail.setPassengerOtherFee(this.passengerOtherFee);
        detail.setImageURL(imageURL);
        detail.setImageIndex(imageIndex);
        return detail;
    }
}

