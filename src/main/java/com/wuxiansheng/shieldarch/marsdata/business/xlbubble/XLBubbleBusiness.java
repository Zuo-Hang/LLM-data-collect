package com.wuxiansheng.shieldarch.marsdata.business.xlbubble;

import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonRequest;
import com.wuxiansheng.shieldarch.marsdata.llm.ReasonResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 小拉冒泡业务
 */
@Data
public class XLBubbleBusiness implements Business {
    
    /**
     * 输入数据
     */
    private XLBubbleInput input;
    
    /**
     * 推理结果
     */
    private XLBubbleReasonResult reasonResult;
    
    /**
     * 城市检查结果
     */
    private String cityCheck;
    
    @Override
    public String getName() {
        return "xl_bubble";
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
        return input.getReasonRequests(getName());
    }
    
    @Override
    public void merge(List<ReasonResponse> results) throws Exception {
        // 转化到内部结构体
        List<XLBubbleReasonResultRaw> raws = newReasonResultRaws(results);
        List<XLBubbleReasonResult> bubbleResults = toBubbleResults(raws);
        
        // 执行 Merge
        this.reasonResult = combine(bubbleResults);
    }
    
    /**
     * 创建推理结果原始数据列表
     */
    private List<XLBubbleReasonResultRaw> newReasonResultRaws(List<ReasonResponse> reasonResps) throws Exception {
        List<XLBubbleReasonResultRaw> results = new ArrayList<>();
        
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
                
                XLBubbleReasonResultRaw raw = XLBubbleReasonResultRaw.fromJson(content);
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
    private List<XLBubbleReasonResult> toBubbleResults(List<XLBubbleReasonResultRaw> raws) {
        List<XLBubbleReasonResult> results = new ArrayList<>();
        for (XLBubbleReasonResultRaw raw : raws) {
            results.add(raw.toModel());
        }
        return results;
    }
    
    /**
     * 合并多个推理结果
     */
    private XLBubbleReasonResult combine(List<XLBubbleReasonResult> results) {
        if (results.isEmpty()) {
            return new XLBubbleReasonResult();
        }
        
        XLBubbleReasonResult res = results.get(0);
        for (int i = 1; i < results.size(); i++) {
            res = res.merge(results.get(i));
        }
        return res;
    }
}

