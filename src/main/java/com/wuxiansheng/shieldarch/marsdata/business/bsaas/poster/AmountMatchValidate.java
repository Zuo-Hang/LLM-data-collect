package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasOrderListItem;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasVerifyRecord;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 金额匹配验证Poster
 * 校验司机收入与乘客支付之间的金额关系
 */
@Slf4j
@Component
public class AmountMatchValidate implements Poster {
    
    private static final int VERIFY_FLAG_AMOUNT = 1;
    private static final double ORDER_PASSENGER_TOLERANCE = 1.0;
    private static final double DRIVER_INCOME_TOLERANCE = 1.5;
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null || bs.getReasonResult().getOrderList() == null) {
            return business;
        }
        
        for (BSaasOrderListItem order : bs.getReasonResult().getOrderList()) {
            if (order.getDriverDetailRef() == null) {
                continue;
            }
            
            if (!checkAmountLogic(order)) {
                BSaasVerifyRecord record = new BSaasVerifyRecord();
                record.setFlag(VERIFY_FLAG_AMOUNT);
                record.setErrorMessage("金额逻辑不一致");
                record.setOrderID(order.getOrderID());
                record.setImageURL(order.getImageURL());
                record.setPageType("订单列表页");
                
                if (bs.getReasonResult().getVerifyRecords() == null) {
                    bs.getReasonResult().setVerifyRecords(new java.util.ArrayList<>());
                }
                bs.getReasonResult().getVerifyRecords().add(record);
            }
        }
        
        return business;
    }
    
    /**
     * 检查金额逻辑
     */
    private boolean checkAmountLogic(BSaasOrderListItem order) {
        if (order.getDriverDetailRef() == null) {
            return false;
        }
        
        if (order.getDriverDetailRef().getPassengerPay() == null || 
            order.getDriverDetailRef().getDriverIncome() == null) {
            return false;
        }
        
        double orderPrice = order.getAmount() != null ? order.getAmount() : 0.0;
        double passengerPay = order.getDriverDetailRef().getPassengerPay();
        double driverIncome = order.getDriverDetailRef().getDriverIncome();
        double takeRate = order.getDriverDetailRef().getTakeRate() != null ? 
            order.getDriverDetailRef().getTakeRate() : 0.0;
        
        if (orderPrice <= 0 || passengerPay <= 0 || driverIncome <= 0) {
            return false;
        }
        
        // 订单金额应该约等于乘客支付金额
        if (!approxEqual(orderPrice, passengerPay, ORDER_PASSENGER_TOLERANCE)) {
            return false;
        }
        
        // 计算期望的司机收入
        double expectedDriverIncome = passengerPay;
        if (takeRate > 0 && takeRate < 1) {
            expectedDriverIncome = passengerPay * (1 - takeRate);
        }
        
        // 司机收入应该约等于期望值
        return approxEqual(driverIncome, expectedDriverIncome, DRIVER_INCOME_TOLERANCE);
    }
    
    /**
     * 判断两个值是否在容差范围内相等
     */
    private boolean approxEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) <= tolerance;
    }
}

