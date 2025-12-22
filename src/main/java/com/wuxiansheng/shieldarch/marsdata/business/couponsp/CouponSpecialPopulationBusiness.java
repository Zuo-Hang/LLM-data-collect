package com.wuxiansheng.shieldarch.marsdata.business.couponsp;

import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonRequest;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 券包人群标签识别业务
 */
@Data
public class CouponSpecialPopulationBusiness implements Business {
    
    /**
     * 输入数据
     */
    private CouponSpecialPopulationInput input;
    
    /**
     * 推理结果
     */
    private List<CouponReasonResult> reasonResults = new ArrayList<>();
    
    @Override
    public String getName() {
        return "coupon_sp";
    }
    
    @Override
    public long getMsgTimestamp() {
        return input != null ? input.getSubmitTimestampMs() / 1000 : 0;
    }
    
    @Override
    public List<ReasonRequest> getReasonRequests() {
        if (input == null) {
            return new ArrayList<>();
        }
        return input.getReasonRequests();
    }
    
    @Override
    public void merge(List<ReasonResponse> results) throws Exception {
        // 转化到内部结构体
        List<CouponReasonResultRaw> raws = newReasonResultRaws(results);
        this.reasonResults = toBubbleResults(raws);
    }
    
    /**
     * 创建推理结果原始数据列表
     */
    private List<CouponReasonResultRaw> newReasonResultRaws(List<ReasonResponse> reasonResps) throws Exception {
        List<CouponReasonResultRaw> results = new ArrayList<>();
        
        for (ReasonResponse resp : reasonResps) {
            if (resp.hasError()) {
                throw new Exception("推理结果包含错误: " + resp.getError().getMessage());
            }
            
            try {
                CouponReasonResultRaw raw = CouponReasonResultRaw.fromJson(resp.getContent());
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
    private List<CouponReasonResult> toBubbleResults(List<CouponReasonResultRaw> raws) {
        List<CouponReasonResult> results = new ArrayList<>();
        for (CouponReasonResultRaw raw : raws) {
            results.add(raw.toModel());
        }
        return results;
    }
}

