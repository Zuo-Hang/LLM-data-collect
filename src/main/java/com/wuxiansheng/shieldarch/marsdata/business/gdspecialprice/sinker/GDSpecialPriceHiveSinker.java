package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.sinker;

import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.GDSpecialPriceBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.GDSpecialPriceInput;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.ReasonSupplierResult;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.sinker.HiveSinker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 特价车Hive存储
 */
@Slf4j
@Component
public class GDSpecialPriceHiveSinker extends HiveSinker {
    
    @Override
    public void sink(BusinessContext bctx, Business business) {
        if (!(business instanceof GDSpecialPriceBusiness)) {
            return;
        }
        
        GDSpecialPriceBusiness gdBusiness = (GDSpecialPriceBusiness) business;
        
        if (gdBusiness.getReasonResult() == null || gdBusiness.getReasonResult().getSuppliersInfo() == null) {
            return;
        }
        
        // 为每个供应商生成一条Hive记录
        List<SpecialPriceHiveRaw> hiveRaws = buildHiveRaws(gdBusiness);
        for (SpecialPriceHiveRaw hiveRaw : hiveRaws) {
            printToHive(hiveRaw, business.getName(), gdBusiness.getMsgTimestamp());
        }
    }
    
    /**
     * 构建多个Hive原始数据记录
     */
    private List<SpecialPriceHiveRaw> buildHiveRaws(GDSpecialPriceBusiness gdBusiness) {
        // 检查是否有供应商信息
        if (gdBusiness.getReasonResult().getSuppliersInfo().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SpecialPriceHiveRaw> hiveRaws = new ArrayList<>();
        GDSpecialPriceInput input = gdBusiness.getInput();
        
        // 为每个供应商生成一条记录
        for (ReasonSupplierResult supplierInfo : gdBusiness.getReasonResult().getSuppliersInfo()) {
            SpecialPriceHiveRaw hiveRaw = new SpecialPriceHiveRaw();
            hiveRaw.setEstimateId(input.getEstimateId());
            
            // 将图片URL列表转换为逗号分隔的字符串
            StringJoiner sj = new StringJoiner(",");
            if (input.getBubbleImageUrls() != null) {
                input.getBubbleImageUrls().forEach(sj::add);
            }
            hiveRaw.setBubbleImageUrl(sj.toString());
            
            hiveRaw.setPartnerName(supplierInfo.getSupplier());
            hiveRaw.setCapPrice(supplierInfo.getCapPrice());
            hiveRaw.setReducePrice(supplierInfo.getReducePrice());
            hiveRaw.setCarType("特惠快车"); // 固定值
            
            hiveRaws.add(hiveRaw);
        }
        
        return hiveRaws;
    }
    
    /**
     * 特价车Hive原始数据
     */
    @Data
    private static class SpecialPriceHiveRaw {
        // 问卷ID，唯一标识一次调研
        private String estimateId;
        // 冒泡图片URL，用于识别的原始图片地址
        private String bubbleImageUrl;
        // 供应商名称，如"曹操出行"、"星徽出行"等
        private String partnerName;
        // 一口价，特价车的实际价格
        private Double capPrice;
        // 优惠金额，特价车相比原价的优惠数额
        private Double reducePrice;
        // 车型，固定为"特惠快车"
        private String carType;
    }
}

