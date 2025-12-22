package com.wuxiansheng.shieldarch.marsdata.business.couponsp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 券包推理结果原始数据（从LLM返回的JSON解析）
 */
@Slf4j
@Data
public class CouponReasonResultRaw {
    
    @JsonProperty("page_catagory")
    private String pageCategory;
    
    @JsonProperty("coupon_name")
    private String couponName;
    
    @JsonProperty("deadline")
    private String deadline;
    
    @JsonProperty("coupon_type")
    private String couponType;
    
    @JsonProperty("discount")
    private String discount;
    
    @JsonProperty("cap")
    private String cap;
    
    @JsonProperty("threshold")
    private String threshold;
    
    @JsonProperty("valid_days")
    private String validDays;
    
    @JsonProperty("valid_period")
    private String validPeriod;
    
    @JsonProperty("supplier_rule")
    private String supplierRule;
    
    @JsonProperty("valid_channel")
    private String validChannel;
    
    @JsonProperty("valid_city")
    private String validCity;
    
    @JsonProperty("valid_car_type")
    private String validCarType;
    
    @JsonProperty("valid_order")
    private String validOrder;
    
    @JsonProperty("valid_route")
    private String validRoute;
    
    /**
     * 从JSON字符串创建对象
     */
    public static CouponReasonResultRaw fromJson(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, CouponReasonResultRaw.class);
    }
    
    /**
     * 转换为业务模型
     */
    public CouponReasonResult toModel() {
        QuestUtils questUtils = new QuestUtils();
        
        CouponReasonResult result = new CouponReasonResult();
        result.setPageCategory(this.pageCategory);
        result.setCouponName(this.couponName);
        result.setDeadline(this.deadline);
        result.setCouponType(this.couponType);
        result.setDiscount(questUtils.extractFloatPrefix(this.discount));
        result.setCap(questUtils.extractFloatPrefix(this.cap));
        result.setThreshold(questUtils.extractFloatPrefix(this.threshold));
        result.setValidDays(this.validDays);
        result.setValidPeriod(this.validPeriod);
        result.setSupplierRule(this.supplierRule);
        result.setValidChannel(this.validChannel);
        result.setValidCity(this.validCity);
        result.setValidCarType(this.validCarType);
        result.setValidOrder(this.validOrder);
        result.setValidRoute(this.validRoute);
        
        return result;
    }
}

