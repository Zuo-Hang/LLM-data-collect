package com.wuxiansheng.shieldarch.marsdata.business.gdbubble.poster;

import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.GDBubbleBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.gdbubble.ReasonSupplierResult;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成原有协议的输出（价格适配）
 */
@Slf4j
@Component
public class GDAdapterPrice implements Poster {
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof GDBubbleBusiness)) {
            return business;
        }
        
        GDBubbleBusiness gb = (GDBubbleBusiness) business;
        
        if (gb.getReasonResult() == null || gb.getReasonResult().getSuppliersInfo() == null) {
            return gb;
        }
        
        List<ReasonSupplierResult> adaptedSuppliers = new ArrayList<>();
        for (ReasonSupplierResult supplierInfo : gb.getReasonResult().getSuppliersInfo()) {
            adaptedSuppliers.add(adapterOneSupplierPrice(supplierInfo));
        }
        
        gb.getReasonResult().setSuppliersInfo(adaptedSuppliers);
        return gb;
    }
    
    /**
     * 适配单个供应商的价格
     */
    private ReasonSupplierResult adapterOneSupplierPrice(ReasonSupplierResult supplierInfo) {
        ReasonSupplierResult res = new ReasonSupplierResult();
        res.setSupplier(supplierInfo.getSupplier());
        res.setDiscountType(supplierInfo.getDiscountType());
        res.setDiscountAmount(supplierInfo.getDiscountAmount());
        res.setPriceType(supplierInfo.getPriceType());
        res.setCarType(supplierInfo.getCarType());
        
        List<Double> priceRange = supplierInfo.getPriceRange();
        
        // 所有价格区间的右边界，都放到 other_price 上
        if (priceRange != null && priceRange.size() == 2) {
            res.setOtherPrice(priceRange.get(1));
        }
        
        // 特价车集合
        if (slicesContains(new String[]{"特惠快车", "一口价", "平价车", "特价车"}, supplierInfo.getSupplier())) {
            if (priceRange != null && !priceRange.isEmpty()) {
                res.setCapPrice(priceRange.get(0));
                res.setOtherPrice(priceRange.size() > 1 ? priceRange.get(1) : 0.0);
                res.setEstPrice(0.0);
            } else {
                res.setCapPrice(supplierInfo.price()); // 特价车品类固定放到一口价里
                res.setEstPrice(0.0);
                res.setOtherPrice(0.0);
            }
            return res;
        }
        
        // 极速拼车
        if (slicesContains(new String[]{"极速拼车", "远途极速拼"}, supplierInfo.getSupplier())) {
            if (priceRange != null && priceRange.size() == 2 && 
                priceRange.get(0).equals(priceRange.get(1))) { // A/D 样式，未拼成时一个价格
                res.setEstPrice(priceRange.get(0));
                res.setOtherPrice(supplierInfo.price());
                res.setCapPrice(0.0);
            } else if (priceRange != null && priceRange.size() == 2) {
                res.setEstPrice(priceRange.get(0));
                res.setCapPrice(priceRange.get(1));
                res.setOtherPrice(supplierInfo.price());
            } else {
                res.setEstPrice(supplierInfo.price()); // 极速拼车未拼成一个价格情况下，固定放在预估价里
                res.setCapPrice(0.0);
                res.setOtherPrice(0.0);
            }
            return res;
        }
        
        // 轻快联盟
        if ("轻快联盟".equals(supplierInfo.getSupplier())) {
            if (priceRange != null && priceRange.size() == 2) {
                res.setOtherPrice(priceRange.get(1));
                if (ReasonSupplierResult.isEstPrice(supplierInfo.getPriceType())) {
                    res.setEstPrice(priceRange.get(0));
                    res.setCapPrice(0.0);
                }
                if (ReasonSupplierResult.isCapPrice(supplierInfo.getPriceType())) {
                    res.setCapPrice(priceRange.get(0));
                    res.setEstPrice(0.0);
                }
            }
            return res;
        }
        
        // 默认情况：保持原有价格
        res.setEstPrice(supplierInfo.getEstPrice());
        res.setCapPrice(supplierInfo.getCapPrice());
        res.setPriceRange(supplierInfo.getPriceRange());
        
        return res;
    }
    
    /**
     * 检查字符串数组是否包含目标字符串
     */
    private boolean slicesContains(String[] strs, String str) {
        for (String s : strs) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }
}

