package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasMergedStatistic;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 性能汇总合并Poster
 * 合并流水统计信息，按日期范围汇总
 */
@Slf4j
@Component
public class PerformanceSummaryMerge implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null) {
            return business;
        }
        
        if ((bs.getReasonResult().getPerformanceTransactions() == null || 
             bs.getReasonResult().getPerformanceTransactions().isEmpty()) &&
            (bs.getReasonResult().getHistoricalStatistics() == null || 
             bs.getReasonResult().getHistoricalStatistics().isEmpty())) {
            return business;
        }
        
        Map<String, BSaasMergedStatistic> table = new HashMap<>();
        
        // 处理业绩交易数据
        if (bs.getReasonResult().getPerformanceTransactions() != null) {
            for (var perf : bs.getReasonResult().getPerformanceTransactions()) {
                String key = perf.getStartDate() + "|" + perf.getEndDate();
                BSaasMergedStatistic entry = getOrCreateMerged(table, key);
                entry.setStartDate(perf.getStartDate());
                entry.setEndDate(perf.getEndDate());
                entry.setSourceType(perf.getSourceType());
                entry.setPerformanceOrderCount(
                    (entry.getPerformanceOrderCount() != null ? entry.getPerformanceOrderCount() : 0) + 
                    (perf.getPerformanceOrderCount() != null ? perf.getPerformanceOrderCount() : 0));
                entry.setOnlineDuration(
                    (entry.getOnlineDuration() != null ? entry.getOnlineDuration() : 0.0) + 
                    (perf.getOnlineDuration() != null ? perf.getOnlineDuration() : 0.0));
                entry.setServeDuration(
                    (entry.getServeDuration() != null ? entry.getServeDuration() : 0.0) + 
                    (perf.getServeDuration() != null ? perf.getServeDuration() : 0.0));
                entry.setIncomeAmount(
                    (entry.getIncomeAmount() != null ? entry.getIncomeAmount() : 0.0) + 
                    (perf.getIncomeAmount() != null ? perf.getIncomeAmount() : 0.0));
                entry.setRewardAmount(
                    (entry.getRewardAmount() != null ? entry.getRewardAmount() : 0.0) + 
                    (perf.getRewardAmount() != null ? perf.getRewardAmount() : 0.0));
                entry.setCompletionRate(perf.getCompletionRate());
                entry.setRating(perf.getRating());
            }
        }
        
        // 处理历史统计数据
        if (bs.getReasonResult().getHistoricalStatistics() != null) {
            for (var summary : bs.getReasonResult().getHistoricalStatistics()) {
                String key = summary.getStartDate() + "|" + summary.getEndDate();
                BSaasMergedStatistic entry = getOrCreateMerged(table, key);
                entry.setStartDate(summary.getStartDate());
                entry.setEndDate(summary.getEndDate());
                if ((entry.getSourceType() == null || entry.getSourceType().isEmpty()) && 
                    summary.getSourceType() != null && !summary.getSourceType().isEmpty()) {
                    entry.setSourceType(summary.getSourceType());
                }
                entry.setSummaryOrderCount(
                    (entry.getSummaryOrderCount() != null ? entry.getSummaryOrderCount() : 0) + 
                    (summary.getSummaryOrderCount() != null ? summary.getSummaryOrderCount() : 0));
            }
        }
        
        // 转换为列表并排序
        List<BSaasMergedStatistic> merged = new ArrayList<>(table.values());
        merged.sort((a, b) -> {
            int startCompare = (a.getStartDate() != null ? a.getStartDate() : "").compareTo(
                b.getStartDate() != null ? b.getStartDate() : "");
            if (startCompare != 0) {
                return startCompare;
            }
            return (a.getEndDate() != null ? a.getEndDate() : "").compareTo(
                b.getEndDate() != null ? b.getEndDate() : "");
        });
        
        bs.getReasonResult().setMergedStats(merged);
        return business;
    }
    
    /**
     * 获取或创建合并统计项
     */
    private BSaasMergedStatistic getOrCreateMerged(Map<String, BSaasMergedStatistic> table, String key) {
        return table.computeIfAbsent(key, k -> new BSaasMergedStatistic());
    }
}

