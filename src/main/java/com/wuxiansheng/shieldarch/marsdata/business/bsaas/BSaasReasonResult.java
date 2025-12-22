package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * B SaaS推理结果
 */
@Data
public class BSaasReasonResult {
    
    @JsonProperty("order_list")
    private List<BSaasOrderListItem> orderList = new ArrayList<>();
    
    @JsonProperty("passenger_details")
    private List<BSaasPassengerDetail> passengerDetails = new ArrayList<>();
    
    @JsonProperty("driver_details")
    private List<BSaasDriverDetail> driverDetails = new ArrayList<>();
    
    @JsonProperty("historical_statistics")
    private List<BSaasHistoricalStatistic> historicalStatistics = new ArrayList<>();
    
    @JsonProperty("performance_transactions")
    private List<BSaasPerformanceTransaction> performanceTransactions = new ArrayList<>();
    
    @JsonProperty("personal_homepage")
    private List<BSaasPersonalHomepage> personalHomepage = new ArrayList<>();
    
    @JsonProperty("verify_records")
    private List<BSaasVerifyRecord> verifyRecords = new ArrayList<>();
    
    @JsonProperty("filtered_orders")
    private List<BSaasOrderListItem> filteredOrders = new ArrayList<>();
    
    @JsonProperty("merged_stats")
    private List<BSaasMergedStatistic> mergedStats = new ArrayList<>();
}

