package com.wuxiansheng.shieldarch.marsdata.business.xlbubble;

import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 小拉冒泡推理结果
 */
@Data
public class XLBubbleReasonResult {
    
    private Double estimatedDistance;
    private Double estimatedTime;
    private String startPoint;
    private String endPoint;
    private String bubbleTime;
    private List<ReasonSupplierResult> suppliersInfo = new ArrayList<>();
    
    /**
     * 合并两个推理结果
     */
    public XLBubbleReasonResult merge(XLBubbleReasonResult other) {
        if (other == null || other == this) {
            return this;
        }
        
        QuestUtils questUtils = new QuestUtils();
        
        XLBubbleReasonResult res = new XLBubbleReasonResult();
        res.setEstimatedDistance(questUtils.mergeFloat64(
            this.estimatedDistance != null ? this.estimatedDistance : 0.0,
            other.estimatedDistance != null ? other.estimatedDistance : 0.0));
        res.setEstimatedTime(questUtils.mergeFloat64(
            this.estimatedTime != null ? this.estimatedTime : 0.0,
            other.estimatedTime != null ? other.estimatedTime : 0.0));
        res.setStartPoint(questUtils.mergeString(this.startPoint, other.startPoint));
        res.setEndPoint(questUtils.mergeString(this.endPoint, other.endPoint));
        res.setBubbleTime(questUtils.mergeString(this.bubbleTime, other.bubbleTime));
        
        // 合并供应商列表
        List<ReasonSupplierResult> mergedSuppliers = new ArrayList<>();
        if (this.suppliersInfo != null) {
            mergedSuppliers.addAll(this.suppliersInfo);
        }
        if (other.suppliersInfo != null) {
            mergedSuppliers.addAll(other.suppliersInfo);
        }
        res.setSuppliersInfo(mergedSuppliers);
        
        return res;
    }
}

