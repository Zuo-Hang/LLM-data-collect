package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 历史数据统计
 */
@Data
public class BSaasHistoricalStatistic {
    
    @JsonProperty("start_date")
    private String startDate;
    
    @JsonProperty("end_date")
    private String endDate;
    
    @JsonProperty("summary_order_count")
    private Integer summaryOrderCount;
    
    @JsonProperty("online_duration")
    private Double onlineDuration;
    
    @JsonProperty("peak_online_duration")
    private Double peakOnlineDuration;
    
    @JsonProperty("serve_duration")
    private Double serveDuration;
    
    @JsonProperty("driver_cancel_count")
    private Integer driverCancelCount;
    
    @JsonProperty("income_amount")
    private Double incomeAmount;
    
    @JsonProperty("reward_amount")
    private Double rewardAmount;
    
    @JsonProperty("completion_rate")
    private Double completionRate;
    
    @JsonProperty("source_type")
    private String sourceType;
    
    @JsonProperty("image_url")
    private String imageURL;
    
    @JsonProperty("image_index")
    private Integer imageIndex;
}

