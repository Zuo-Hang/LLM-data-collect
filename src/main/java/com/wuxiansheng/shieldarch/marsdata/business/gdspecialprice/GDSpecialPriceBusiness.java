package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice;

import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonRequest;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 高德特价车业务
 */
@Data
public class GDSpecialPriceBusiness implements Business {
    
    /**
     * 输入数据
     */
    private GDSpecialPriceInput input;
    
    /**
     * 推理结果
     */
    private GDSpecialPriceReasonResult reasonResult;
    
    @Override
    public String getName() {
        return "gd_special_price";
    }
    
    @Override
    public long getMsgTimestamp() {
        return input != null ? input.getClientTime() / 1000 : 0;
    }
    
    @Override
    public List<ReasonRequest> getReasonRequests() {
        if (input == null) {
            return new ArrayList<>();
        }
        return input.getReasonRequests(getName());
    }
    
    @Override
    public void merge(List<ReasonResponse> results) throws Exception {
        // 转化到内部结构体
        List<GDSpecialPriceReasonResultRaw> raws = newReasonResultRaws(results);
        List<GDSpecialPriceReasonResult> bubbleResults = toGDSpecialPriceResults(raws);
        
        // 执行 Merge
        this.reasonResult = combine(bubbleResults);
    }
    
    /**
     * 创建推理结果原始数据列表
     */
    private List<GDSpecialPriceReasonResultRaw> newReasonResultRaws(List<ReasonResponse> reasonResps) throws Exception {
        List<GDSpecialPriceReasonResultRaw> results = new ArrayList<>();
        
        for (ReasonResponse resp : reasonResps) {
            if (resp.hasError()) {
                throw new Exception("推理结果包含错误: " + resp.getError().getMessage());
            }
            
            try {
                // 清理可能的JSON代码块标记
                String content = resp.getContent();
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3);
                }
                content = content.trim();
                
                GDSpecialPriceReasonResultRaw raw = GDSpecialPriceReasonResultRaw.fromJson(content);
                results.add(raw);
            } catch (Exception e) {
                throw new Exception("解析推理结果JSON失败: content=" + resp.getContent() + ", error=" + e.getMessage(), e);
            }
        }
        
        return results;
    }
    
    /**
     * 转换为业务结果列表
     */
    private List<GDSpecialPriceReasonResult> toGDSpecialPriceResults(List<GDSpecialPriceReasonResultRaw> raws) {
        List<GDSpecialPriceReasonResult> results = new ArrayList<>();
        for (GDSpecialPriceReasonResultRaw raw : raws) {
            results.add(raw.toModel());
        }
        return results;
    }
    
    /**
     * 合并多个推理结果
     */
    private GDSpecialPriceReasonResult combine(List<GDSpecialPriceReasonResult> results) {
        if (results.isEmpty()) {
            return new GDSpecialPriceReasonResult();
        }
        
        GDSpecialPriceReasonResult res = results.get(0);
        for (int i = 1; i < results.size(); i++) {
            res = res.merge(results.get(i));
        }
        return res;
    }
}

