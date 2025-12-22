package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasDriverDetail;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasOrderListItem;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasPassengerDetail;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasVerifyRecord;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据完整性验证Poster
 * 校验必填字段是否缺失
 */
@Slf4j
@Component
public class DataCompletenessValidate implements Poster {
    
    private static final int VERIFY_FLAG_MISSING = 2;
    private static final double FLOAT_EPSILON = 1e-9;
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null) {
            return business;
        }
        
        // 校验订单字段
        if (bs.getReasonResult().getOrderList() != null) {
            for (BSaasOrderListItem order : bs.getReasonResult().getOrderList()) {
                List<String> missing = checkOrderFields(order);
                if (!missing.isEmpty()) {
                    BSaasVerifyRecord record = new BSaasVerifyRecord();
                    record.setFlag(VERIFY_FLAG_MISSING);
                    record.setErrorMessage("订单字段缺失:" + String.join(",", missing));
                    record.setOrderID(order.getOrderID());
                    record.setImageURL(order.getImageURL());
                    record.setPageType("订单列表页");
                    
                    if (bs.getReasonResult().getVerifyRecords() == null) {
                        bs.getReasonResult().setVerifyRecords(new ArrayList<>());
                    }
                    bs.getReasonResult().getVerifyRecords().add(record);
                }
            }
        }
        
        // 校验司机明细字段
        if (bs.getReasonResult().getDriverDetails() != null) {
            for (BSaasDriverDetail detail : bs.getReasonResult().getDriverDetails()) {
                List<String> missing = checkDriverFields(detail);
                if (!missing.isEmpty()) {
                    BSaasVerifyRecord record = new BSaasVerifyRecord();
                    record.setFlag(VERIFY_FLAG_MISSING);
                    record.setErrorMessage("司机明细字段缺失:" + String.join(",", missing));
                    record.setImageURL(detail.getImageURL());
                    record.setPageType("司机收入详情");
                    
                    if (bs.getReasonResult().getVerifyRecords() == null) {
                        bs.getReasonResult().setVerifyRecords(new ArrayList<>());
                    }
                    bs.getReasonResult().getVerifyRecords().add(record);
                }
            }
        }
        
        // 校验乘客明细字段
        if (bs.getReasonResult().getPassengerDetails() != null) {
            for (BSaasPassengerDetail detail : bs.getReasonResult().getPassengerDetails()) {
                List<String> missing = checkPassengerFields(detail);
                if (!missing.isEmpty()) {
                    BSaasVerifyRecord record = new BSaasVerifyRecord();
                    record.setFlag(VERIFY_FLAG_MISSING);
                    record.setErrorMessage("乘客明细字段缺失:" + String.join(",", missing));
                    record.setImageURL(detail.getImageURL());
                    record.setPageType("乘客费用明细");
                    
                    if (bs.getReasonResult().getVerifyRecords() == null) {
                        bs.getReasonResult().setVerifyRecords(new ArrayList<>());
                    }
                    bs.getReasonResult().getVerifyRecords().add(record);
                }
            }
        }
        
        return business;
    }
    
    /**
     * 检查订单字段
     */
    private List<String> checkOrderFields(BSaasOrderListItem order) {
        List<String> missing = new ArrayList<>();
        if (order.getOrderID() == null || order.getOrderID().isEmpty()) {
            missing.add("order_id");
        }
        if (order.getMonthDay() == null || order.getMonthDay().isEmpty()) {
            missing.add("order_date");
        }
        if (order.getOrderTime() == null || order.getOrderTime().isEmpty()) {
            missing.add("order_time");
        }
        if (order.getAmount() == null || order.getAmount() <= 0) {
            missing.add("order_price");
        }
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            missing.add("order_status");
        }
        return missing;
    }
    
    /**
     * 检查司机字段
     */
    private List<String> checkDriverFields(BSaasDriverDetail detail) {
        List<String> missing = new ArrayList<>();
        if (isZeroFloat(detail.getDriverIncome(), 0)) {
            missing.add("driver_income");
        }
        if (isZeroFloat(detail.getPassengerPay(), 0)) {
            missing.add("passenger_pay");
        }
        if (detail.getTakeRate() == null) {
            missing.add("take_rate");
        }
        if (isZeroFloat(detail.getInfoFeeBeforeSubsidy(), 0) && 
            isZeroFloat(detail.getInfoFeeAfterSubsidy(), 0)) {
            missing.add("info_fee");
        }
        return missing;
    }
    
    /**
     * 检查乘客字段
     */
    private List<String> checkPassengerFields(BSaasPassengerDetail detail) {
        List<String> missing = new ArrayList<>();
        if (isZeroFloat(detail.getPassengerOrderPrice(), 0)) {
            missing.add("passenger_order_price");
        }
        if (detail.getProductType() == null || detail.getProductType().isEmpty()) {
            missing.add("product_type");
        }
        return missing;
    }
    
    /**
     * 判断浮点数是否为零
     */
    private boolean isZeroFloat(Double v, double epsilon) {
        if (v == null) {
            return true;
        }
        if (epsilon <= 0) {
            epsilon = FLOAT_EPSILON;
        }
        return Math.abs(v) <= epsilon;
    }
}

