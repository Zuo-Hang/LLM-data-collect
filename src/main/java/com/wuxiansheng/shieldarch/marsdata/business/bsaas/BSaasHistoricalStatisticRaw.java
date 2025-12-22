package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 历史数据统计原始数据
 */
@Data
public class BSaasHistoricalStatisticRaw {
    
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
    
    public BSaasHistoricalStatistic toModel(String imageURL, int imageIndex, String category) {
        BSaasHistoricalStatistic statistic = new BSaasHistoricalStatistic();
        statistic.setStartDate(this.startDate);
        statistic.setEndDate(this.endDate);
        statistic.setSummaryOrderCount(this.summaryOrderCount);
        statistic.setOnlineDuration(this.onlineDuration);
        statistic.setPeakOnlineDuration(this.peakOnlineDuration);
        statistic.setServeDuration(this.serveDuration);
        statistic.setDriverCancelCount(this.driverCancelCount);
        statistic.setIncomeAmount(this.incomeAmount);
        statistic.setRewardAmount(this.rewardAmount);
        statistic.setCompletionRate(this.completionRate);
        statistic.setSourceType(this.sourceType);
        statistic.setImageURL(imageURL);
        statistic.setImageIndex(imageIndex);
        return statistic;
    }
}

