package com.wuxiansheng.shieldarch.marsdata.business.xlbubble;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 小拉冒泡推理结果原始数据（从LLM返回的JSON解析）
 */
@Slf4j
@Data
public class XLBubbleReasonResultRaw {
    
    @JsonProperty("estimated_distance")
    private String estimatedDistance;
    
    @JsonProperty("estimated_time")
    private String estimatedTime;
    
    @JsonProperty("start_point")
    private String startPoint;
    
    @JsonProperty("end_point")
    private String endPoint;
    
    @JsonProperty("bubble_time")
    private String bubbleTime;
    
    @JsonProperty("vehicles")
    private List<ReasonSupplierResultRaw> suppliersInfo = new ArrayList<>();
    
    /**
     * 从JSON字符串创建对象
     */
    public static XLBubbleReasonResultRaw fromJson(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, XLBubbleReasonResultRaw.class);
    }
    
    /**
     * 转换为业务模型
     */
    public XLBubbleReasonResult toModel() {
        QuestUtils questUtils = new QuestUtils();
        
        XLBubbleReasonResult res = new XLBubbleReasonResult();
        res.setEstimatedDistance(questUtils.extractFloatPrefix(this.estimatedDistance));
        res.setEstimatedTime(questUtils.parseMinutesFromString(this.estimatedTime));
        res.setStartPoint(this.startPoint);
        res.setEndPoint(this.endPoint);
        res.setBubbleTime(this.bubbleTime);
        
        List<ReasonSupplierResult> suppliers = new ArrayList<>();
        if (this.suppliersInfo != null) {
            for (ReasonSupplierResultRaw raw : this.suppliersInfo) {
                suppliers.add(raw.toModel());
            }
        }
        res.setSuppliersInfo(suppliers);
        
        return res;
    }
    
    /**
     * 供应商原始数据
     */
    @Data
    public static class ReasonSupplierResultRaw {
        private String supplier;
        
        @JsonProperty("discount_amount")
        private String discountAmount;
        
        @JsonProperty("price_type")
        private String priceType;
        
        private String price;
        
        /**
         * 转换为业务模型
         */
        public ReasonSupplierResult toModel() {
            QuestUtils questUtils = new QuestUtils();
            
            ReasonSupplierResult res = new ReasonSupplierResult();
            res.setSupplier(this.supplier);
            res.setDiscountAmount(questUtils.extractFloatPrefix(this.discountAmount));
            res.setPriceType(this.priceType);
            res.setPrice(questUtils.extractFloatPrefix(this.price));
            
            return res;
        }
    }
}

