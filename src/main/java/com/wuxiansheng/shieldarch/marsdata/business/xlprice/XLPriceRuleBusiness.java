package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonRequest;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonResponse;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 小拉计价业务
 */
@Slf4j
@Data
public class XLPriceRuleBusiness implements Business {
    
    /**
     * 日期
     */
    private String date;
    
    /**
     * 小拉规则数据
     */
    private XLRuleData data;
    
    /**
     * 城市ID
     */
    private Integer cityId;
    
    /**
     * 城市名称
     */
    private String cityName;
    
    @Override
    public String getName() {
        return "price_rule_xl";
    }
    
    @Override
    public long getMsgTimestamp() {
        if (date == null || date.isEmpty()) {
            return 0;
        }
        
        try {
            QuestUtils questUtils = new QuestUtils();
            return questUtils.dateTimeToTimestamp(date);
        } catch (Exception e) {
            log.warn("{}.getMsgTimestamp err: {}, date: {}", getName(), e.getMessage(), date);
            return 0;
        }
    }
    
    @Override
    public List<ReasonRequest> getReasonRequests() {
        // 不需要LLM推理
        return new ArrayList<>();
    }
    
    @Override
    public void merge(List<ReasonResponse> results) throws Exception {
        // do nothing
    }
}

