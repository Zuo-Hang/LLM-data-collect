package com.wuxiansheng.shieldarch.marsdata.business.gdbubble;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 高德冒泡推理结果原始数据（从LLM返回的JSON解析）
 */
@Slf4j
@Data
public class GDBubbleReasonResultRaw {
    
    @JsonProperty("order_estimated_distance")
    private String estimatedDistance;
    
    @JsonProperty("order_estimated_time")
    private String estimatedTime;
    
    @JsonProperty("start_point")
    private String startPoint;
    
    @JsonProperty("end_point")
    private String endPoint;
    
    @JsonProperty("creation_time")
    private String creationTime;
    
    @JsonProperty("vehicles")
    private List<ReasonSupplierResultRaw> suppliersInfo = new ArrayList<>();
    
    /**
     * 从JSON字符串创建对象
     */
    public static GDBubbleReasonResultRaw fromJson(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, GDBubbleReasonResultRaw.class);
    }
    
    /**
     * 转换为业务模型
     */
    public GDBubbleReasonResult toModel() {
        QuestUtils questUtils = new QuestUtils();
        
        GDBubbleReasonResult res = new GDBubbleReasonResult();
        res.setEstimatedDistance(questUtils.extractFloatPrefix(this.estimatedDistance));
        res.setEstimatedTime(questUtils.parseMinutesFromString(this.estimatedTime));
        res.setStartPoint(this.startPoint);
        res.setEndPoint(this.endPoint);
        res.setCreationTime(this.creationTime);
        
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
        
        @JsonProperty("discount_type")
        private String discountType;
        
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
            res.setDiscountType(this.discountType);
            res.setDiscountAmount(questUtils.extractFloatPrefix(this.discountAmount));
            res.setPriceType(this.priceType);
            
            // 解析价格信息
            setPriceInfo(res, this.price, this.priceType);
            
            return res;
        }
        
        /**
         * 设置价格信息
         */
        private void setPriceInfo(ReasonSupplierResult res, String priceStr, String priceType) {
            PriceInfo priceInfo = parsePriceInfo(priceStr);
            if (!priceInfo.hasPrice) {
                return;
            }
            
            if (ReasonSupplierResult.isCapPrice(priceType)) {
                res.setCapPrice(priceInfo.price);
            } else if (ReasonSupplierResult.isEstPrice(priceType)) {
                res.setEstPrice(priceInfo.price);
            }
            
            if (priceInfo.priceRange != null && !priceInfo.priceRange.isEmpty()) {
                res.setPriceRange(priceInfo.priceRange);
            }
        }
        
        /**
         * 解析价格信息
         */
        private PriceInfo parsePriceInfo(String priceStr) {
            if (priceStr == null || priceStr.isEmpty()) {
                return new PriceInfo(false, 0.0, new ArrayList<>());
            }
            
            // 匹配 A/B-C 格式
            Pattern fullPattern = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)/([0-9]+(?:\\.[0-9]+)?)-([0-9]+(?:\\.[0-9]+)?)$");
            // 匹配 A/D 格式，等价于 A/D-D 格式
            Pattern simplifyPattern = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)/([0-9]+(?:\\.[0-9]+)?)$");
            // 匹配 A 格式
            Pattern singlePattern = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)$");
            // 匹配 B-C 格式
            Pattern rangePattern = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)-([0-9]+(?:\\.[0-9]+)?)$");
            
            Matcher matcher;
            
            // 尝试匹配 A/B-C 格式
            matcher = fullPattern.matcher(priceStr);
            if (matcher.matches()) {
                double aVal = Double.parseDouble(matcher.group(1));
                double bVal = Double.parseDouble(matcher.group(2));
                double cVal = Double.parseDouble(matcher.group(3));
                return new PriceInfo(true, aVal, List.of(bVal, cVal));
            }
            
            // 尝试匹配 A/D 格式
            matcher = simplifyPattern.matcher(priceStr);
            if (matcher.matches()) {
                double aVal = Double.parseDouble(matcher.group(1));
                double dVal = Double.parseDouble(matcher.group(2));
                return new PriceInfo(true, aVal, List.of(dVal, dVal));
            }
            
            // 尝试匹配 A 格式
            matcher = singlePattern.matcher(priceStr);
            if (matcher.matches()) {
                double aVal = Double.parseDouble(matcher.group(1));
                return new PriceInfo(true, aVal, new ArrayList<>());
            }
            
            // 尝试匹配 B-C 格式
            matcher = rangePattern.matcher(priceStr);
            if (matcher.matches()) {
                double bVal = Double.parseDouble(matcher.group(1));
                double cVal = Double.parseDouble(matcher.group(2));
                return new PriceInfo(true, 0.0, List.of(bVal, cVal));
            }
            
            return new PriceInfo(false, 0.0, new ArrayList<>());
        }
        
        /**
         * 价格信息
         */
        private static class PriceInfo {
            boolean hasPrice;
            double price;
            List<Double> priceRange;
            
            PriceInfo(boolean hasPrice, double price, List<Double> priceRange) {
                this.hasPrice = hasPrice;
                this.price = price;
                this.priceRange = priceRange;
            }
        }
    }
}

