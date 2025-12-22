package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.sinker;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.GDSpecialPriceBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.GDSpecialPriceInput;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.GDSpecialPriceReasonResult;
import com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.ReasonSupplierResult;
import com.wuxiansheng.shieldarch.marsdata.config.GlobalConfig;
import com.wuxiansheng.shieldarch.marsdata.io.MysqlWrapper;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Sinker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 高德特价车MySQL Sinker
 * 将高德特价车数据批量写入MySQL
 * 唯一键：estimate_id + partner_name
 */
@Slf4j
@Component
public class GDSpecialPriceMysqlSinker implements Sinker {
    
    private static final String MOCK_PARTNER = "MockPartner";
    
    @Autowired(required = false)
    private MysqlWrapper mysqlWrapper;
    
    @Autowired(required = false)
    private GlobalConfig globalConfig;
    
    @Value("${mysql.switch:true}")
    private boolean mysqlSwitch;
    
    @Autowired(required = false)
    private GDSpecialPriceMysqlMapper mysqlMapper;
    
    @Override
    public void sink(BusinessContext bctx, Business business) {
        if (!mysqlSwitch) {
            log.info("mysql switch is false, stop writing mysql");
            return;
        }
        
        // 生产环境下的测试业务不写入MySQL
        if (globalConfig != null && "prod".equals(globalConfig.getEnv()) 
            && bctx != null && bctx.getSourceConf() != null && bctx.getSourceConf().isTest()) {
            log.info("online test business not use mysql, business_name: {}", business.getName());
            return;
        }
        
        if (!(business instanceof GDSpecialPriceBusiness)) {
            return;
        }
        
        GDSpecialPriceBusiness gb = (GDSpecialPriceBusiness) business;
        
        // 检查 ReasonResult 是否为 nil
        if (gb.getReasonResult() == null) {
            gb.setReasonResult(new GDSpecialPriceReasonResult());
        }
        
        try {
            List<GDSpecialPriceMysqlRow> rows = newMysqlRows(gb);
            if (rows.isEmpty()) {
                return;
            }
            
            batchUpsert(rows);
        } catch (Exception e) {
            log.warn("batchUpsert err: {}, business: {}", e.getMessage(), business.getName(), e);
        }
    }
    
    /**
     * 批量Upsert
     * 唯一键：estimate_id + partner_name
     */
    private void batchUpsert(List<GDSpecialPriceMysqlRow> rows) {
        if (mysqlMapper == null) {
            log.warn("mysqlMapper is null, cannot write to mysql");
            return;
        }
        
        // 使用MyBatis Plus，根据唯一键 estimate_id + partner_name 来判断是否存在
        for (GDSpecialPriceMysqlRow row : rows) {
            // 先查询是否存在（根据唯一键）
            LambdaQueryWrapper<GDSpecialPriceMysqlRow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GDSpecialPriceMysqlRow::getEstimateId, row.getEstimateId())
                       .eq(GDSpecialPriceMysqlRow::getPartnerName, row.getPartnerName());
            
            GDSpecialPriceMysqlRow existing = mysqlMapper.selectOne(queryWrapper);
            if (existing != null) {
                // 更新
                row.setId(existing.getId());
                mysqlMapper.updateById(row);
            } else {
                // 插入
                mysqlMapper.insert(row);
            }
        }
    }
    
    /**
     * 创建MySQL行数据列表
     */
    private List<GDSpecialPriceMysqlRow> newMysqlRows(GDSpecialPriceBusiness gb) {
        List<GDSpecialPriceMysqlRow> res = new ArrayList<>();
        
        GDSpecialPriceInput input = gb.getInput();
        GDSpecialPriceReasonResult reasonResult = gb.getReasonResult();
        
        if (input == null || reasonResult == null) {
            return res;
        }
        
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 如果有识别到特价车结果，插入正常数据
        if (reasonResult.getSuppliersInfo() != null && !reasonResult.getSuppliersInfo().isEmpty()) {
            for (ReasonSupplierResult supplierInfo : reasonResult.getSuppliersInfo()) {
                GDSpecialPriceMysqlRow row = new GDSpecialPriceMysqlRow();
                row.setEstimateId(input.getEstimateId());
                
                // 拼接图片URL
                StringJoiner joiner = new StringJoiner(",");
                if (input.getBubbleImageUrls() != null) {
                    for (String url : input.getBubbleImageUrls()) {
                        joiner.add(url);
                    }
                }
                row.setBubbleImageUrl(joiner.toString());
                
                row.setPartnerName(supplierInfo.getSupplier() != null ? supplierInfo.getSupplier() : "");
                row.setCapPrice(supplierInfo.getCapPrice() != null ? supplierInfo.getCapPrice() : 0.0);
                row.setReducePrice(supplierInfo.getReducePrice() != null ? supplierInfo.getReducePrice() : 0.0);
                row.setCarType("特惠快车");
                row.setCityId(input.getCityId() != null ? input.getCityId() : 0);
                row.setCityName(input.getCityName() != null ? input.getCityName() : "");
                row.setCreateTime(now);
                row.setUpdateTime(now);
                row.setType(0); // 0: 原始数据
                
                res.add(row);
            }
        } else {
            // 如果没有识别到特价车结果，插入一条 Mock 数据标识该问卷没有特价车
            GDSpecialPriceMysqlRow row = new GDSpecialPriceMysqlRow();
            row.setEstimateId(input.getEstimateId());
            
            // 拼接图片URL
            StringJoiner joiner = new StringJoiner(",");
            if (input.getBubbleImageUrls() != null) {
                for (String url : input.getBubbleImageUrls()) {
                    joiner.add(url);
                }
            }
            row.setBubbleImageUrl(joiner.toString());
            
            row.setPartnerName(MOCK_PARTNER);
            row.setCapPrice(0.0); // 默认值
            row.setReducePrice(0.0); // 默认值
            row.setCarType("特惠快车");
            row.setCityId(input.getCityId() != null ? input.getCityId() : 0);
            row.setCityName(input.getCityName() != null ? input.getCityName() : "");
            row.setCreateTime(now);
            row.setUpdateTime(now);
            row.setType(1); // 1: 拟合结果（用于标识没有特价车的情况）
            
            res.add(row);
        }
        
        return res;
    }
}

