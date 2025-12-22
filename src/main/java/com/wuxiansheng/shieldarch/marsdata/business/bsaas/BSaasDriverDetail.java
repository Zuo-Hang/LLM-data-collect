package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 司机收入详情
 */
@Data
public class BSaasDriverDetail {
    
    private String time;
    
    @JsonProperty("driver_income")
    private Double driverIncome;
    
    @JsonProperty("passenger_pay")
    private Double passengerPay;
    
    @JsonProperty("passenger_pay_fee")
    private Double passengerPayFee;
    
    @JsonProperty("passenger_pre_discount_pay")
    private Double passengerPreDiscountPay;
    
    @JsonProperty("passenger_discount")
    private Double passengerDiscount;
    
    @JsonProperty("driver_remuneration")
    private Double driverRemuneration;
    
    @JsonProperty("driver_base_income")
    private Double driverBaseIncome;
    
    @JsonProperty("info_fee")
    private Double infoFee;
    
    @JsonProperty("info_fee_before_subsidy")
    private Double infoFeeBeforeSubsidy;
    
    @JsonProperty("info_fee_after_subsidy")
    private Double infoFeeAfterSubsidy;
    
    @JsonProperty("take_rate")
    private Double takeRate;
    
    @JsonProperty("driver_reward")
    private Double driverReward;
    
    @JsonProperty("order_dispatch_service_fee_after_subsidy")
    private Double orderDispatchServiceFeeAfterSubsidy;
    
    @JsonProperty("order_dispatch_service_fee_before_subsidy")
    private Double orderDispatchServiceFeeBeforeSubsidy;
    
    @JsonProperty("security_service_fee")
    private Double securityServiceFee;
    
    @JsonProperty("other_fee")
    private String otherFee;
    
    @JsonProperty("image_url")
    private String imageURL;
    
    @JsonProperty("image_index")
    private Integer imageIndex;
    
    // 不序列化的字段
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String takeRateRaw;
}

