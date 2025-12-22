package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasOrderListItem;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商名称标准化Poster
 * 统一供应商名称文本，主要进行基础清洗
 */
@Slf4j
@Component
public class SupplierNameNormalize implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null) {
            return business;
        }
        
        // 标准化供应商名称
        if (bs.getInput() != null && bs.getInput().getMeta() != null) {
            bs.getInput().getMeta().setSupplierName(normalizeSupplier(bs.getInput().getMeta().getSupplierName()));
        }
        
        // 标准化订单标签
        if (bs.getReasonResult().getOrderList() != null) {
            for (BSaasOrderListItem order : bs.getReasonResult().getOrderList()) {
                order.setTags(normalizeTags(order.getTags()));
            }
        }
        
        return business;
    }
    
    /**
     * 标准化供应商名称
     */
    private String normalizeSupplier(String name) {
        return name != null ? name.trim() : "";
    }
    
    /**
     * 标准化标签
     */
    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream()
            .map(tag -> tag != null ? tag.trim() : "")
            .collect(Collectors.toList());
    }
}

