package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 高德特价车推理结果原始数据（从LLM返回的JSON解析）
 */
@Slf4j
@Data
public class GDSpecialPriceReasonResultRaw {
    
    @JsonProperty("suppliers_info")
    private List<ReasonSupplierResultRaw> suppliersInfo = new ArrayList<>();
    
    /**
     * 从JSON字符串创建对象
     */
    public static GDSpecialPriceReasonResultRaw fromJson(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, GDSpecialPriceReasonResultRaw.class);
    }
    
    /**
     * 转换为业务模型
     */
    public GDSpecialPriceReasonResult toModel() {
        GDSpecialPriceReasonResult res = new GDSpecialPriceReasonResult();
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
        
        @JsonProperty("cap_price")
        private String capPrice;
        
        @JsonProperty("reduce_price")
        private String reducePrice;
        
        /**
         * 转换为业务模型
         */
        public ReasonSupplierResult toModel() {
            QuestUtils questUtils = new QuestUtils();
            
            ReasonSupplierResult res = new ReasonSupplierResult();
            res.setSupplier(this.supplier);
            res.setCapPrice(questUtils.extractFloatPrefix(this.capPrice));
            res.setReducePrice(questUtils.extractFloatPrefix(this.reducePrice));
            
            return res;
        }
    }
}

