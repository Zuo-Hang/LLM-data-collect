package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单列表项
 */
@Data
public class BSaasOrderListItem {
    
    @JsonProperty("order_id")
    private String orderID;
    
    @JsonProperty("year_month")
    private String yearMonth;
    
    @JsonProperty("month_day")
    private String monthDay;
    
    private List<String> tags = new ArrayList<>();
    
    private String time;
    
    @JsonProperty("order_time")
    private String orderTime;
    
    private String status;
    
    private Double amount;
    
    @JsonProperty("amount_text")
    private String amountText;
    
    @JsonProperty("image_url")
    private String imageURL;
    
    @JsonProperty("image_index")
    private Integer imageIndex;
    
    @JsonProperty("order_date")
    private String orderDate;
    
    @JsonProperty("charge_type")
    private String chargeType;
    
    @JsonProperty("order_type")
    private String orderType;
    
    @JsonProperty("order_customer")
    private String orderCustomer;
    
    @JsonProperty("take_fee_type")
    private String takeFeeType;
    
    @JsonProperty("order_channel")
    private String orderChannel;
    
    // 引用字段（不序列化）
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private BSaasDriverDetail driverDetailRef;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private BSaasPassengerDetail passengerDetailRef;
}

