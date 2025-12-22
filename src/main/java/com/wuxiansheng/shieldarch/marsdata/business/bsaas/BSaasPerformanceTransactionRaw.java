package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 业绩交易原始数据
 */
@Data
public class BSaasPerformanceTransactionRaw {
    
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
    
    public BSaasPerformanceTransaction toModel(String imageURL, int imageIndex, String category) {
        BSaasPerformanceTransaction transaction = new BSaasPerformanceTransaction();
        transaction.setStartDate(this.startDate);
        transaction.setEndDate(this.endDate);
        transaction.setServeDuration(this.serveDuration);
        transaction.setPerformanceOrderCount(this.performanceOrderCount);
        transaction.setSummaryOrderCount(this.summaryOrderCount);
        transaction.setOnlineDuration(this.onlineDuration);
        transaction.setIncomeAmount(this.incomeAmount);
        transaction.setRewardAmount(this.rewardAmount);
        transaction.setCompletionRate(this.completionRate);
        transaction.setRating(this.rating);
        transaction.setSourceType(this.sourceType);
        transaction.setImageURL(imageURL);
        transaction.setImageIndex(imageIndex);
        return transaction;
    }
}

