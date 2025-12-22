package com.wuxiansheng.shieldarch.marsdata.business.gdbubble;

import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 高德冒泡推理结果
 */
@Data
public class GDBubbleReasonResult {
    
    private Double estimatedDistance;
    private Double estimatedTime;
    private String startPoint;
    private String endPoint;
    private String creationTime;
    private List<ReasonSupplierResult> suppliersInfo = new ArrayList<>();
    
    /**
     * 合并两个推理结果
     */
    public GDBubbleReasonResult merge(GDBubbleReasonResult other) {
        if (other == null || other == this) {
            return this;
        }
        
        QuestUtils questUtils = new QuestUtils();
        
        GDBubbleReasonResult res = new GDBubbleReasonResult();
        res.setEstimatedDistance(questUtils.mergeFloat64(
            this.estimatedDistance != null ? this.estimatedDistance : 0.0,
            other.estimatedDistance != null ? other.estimatedDistance : 0.0));
        res.setEstimatedTime(questUtils.mergeFloat64(
            this.estimatedTime != null ? this.estimatedTime : 0.0,
            other.estimatedTime != null ? other.estimatedTime : 0.0));
        res.setStartPoint(questUtils.mergeString(this.startPoint, other.startPoint));
        res.setEndPoint(questUtils.mergeString(this.endPoint, other.endPoint));
        res.setCreationTime(questUtils.mergeString(this.creationTime, other.creationTime));
        
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

