package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 业绩交易
 */
@Data
public class BSaasPerformanceTransaction {
    
    @JsonProperty("start_date")
    private String startDate;
    
    @JsonProperty("end_date")
    private String endDate;
    
    @JsonProperty("serve_duration")
    private Double serveDuration;
    
    @JsonProperty("performance_order_count")
    private Integer performanceOrderCount;
    
    @JsonProperty("summary_order_count")
    private Integer summaryOrderCount;
    
    @JsonProperty("online_duration")
    private Double onlineDuration;
    
    @JsonProperty("income_amount")
    private Double incomeAmount;
    
    @JsonProperty("reward_amount")
    private Double rewardAmount;
    
    @JsonProperty("completion_rate")
    private Double completionRate;
    
    private Double rating;
    
    @JsonProperty("source_type")
    private String sourceType;
    
    @JsonProperty("image_url")
    private String imageURL;
    
    @JsonProperty("image_index")
    private Integer imageIndex;
}

