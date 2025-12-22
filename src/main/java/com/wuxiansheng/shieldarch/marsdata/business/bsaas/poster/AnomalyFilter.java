package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasOrderListItem;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasVerifyRecord;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 异常过滤Poster
 * 过滤明显异常的订单数据
 */
@Slf4j
@Component
public class AnomalyFilter implements Poster {
    
    private static final int VERIFY_FLAG_ANOMALY = 3;
    private static final double MAX_TAKE_RATE = 0.5; // 50%
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null || bs.getReasonResult().getOrderList() == null) {
            return business;
        }
        
        List<BSaasOrderListItem> filtered = new ArrayList<>();
        
        for (BSaasOrderListItem order : bs.getReasonResult().getOrderList()) {
            // 检查订单金额异常
            if (order.getAmount() == null || order.getAmount() <= 0) {
                if (bs.getReasonResult().getFilteredOrders() == null) {
                    bs.getReasonResult().setFilteredOrders(new ArrayList<>());
                }
                bs.getReasonResult().getFilteredOrders().add(order);
                
                BSaasVerifyRecord record = new BSaasVerifyRecord();
                record.setFlag(VERIFY_FLAG_ANOMALY);
                record.setErrorMessage("订单金额异常");
                record.setOrderID(order.getOrderID());
                record.setImageURL(order.getImageURL());
                record.setPageType("订单列表页");
                
                if (bs.getReasonResult().getVerifyRecords() == null) {
                    bs.getReasonResult().setVerifyRecords(new ArrayList<>());
                }
                bs.getReasonResult().getVerifyRecords().add(record);
                continue;
            }
            
            // 检查司机明细相关异常
            if (order.getDriverDetailRef() != null) {
                // 检查抽成比例过高
                Double takeRate = order.getDriverDetailRef().getTakeRate();
                if (takeRate != null && takeRate > 0 && takeRate < 1 && takeRate * 100 > MAX_TAKE_RATE * 100) {
                    BSaasVerifyRecord record = new BSaasVerifyRecord();
                    record.setFlag(VERIFY_FLAG_ANOMALY);
                    record.setErrorMessage("抽成比例过高");
                    record.setOrderID(order.getOrderID());
                    record.setImageURL(order.getDriverDetailRef().getImageURL());
                    record.setPageType("司机收入详情");
                    
                    if (bs.getReasonResult().getVerifyRecords() == null) {
                        bs.getReasonResult().setVerifyRecords(new ArrayList<>());
                    }
                    bs.getReasonResult().getVerifyRecords().add(record);
                }
                
                // 检查司机与乘客金额差异过大
                Double passengerPay = order.getDriverDetailRef().getPassengerPay();
                if (passengerPay != null) {
                    Double driverIncome = order.getDriverDetailRef().getDriverIncome();
                    if (driverIncome != null) {
                        double delta = Math.abs(passengerPay - driverIncome);
                        if (delta > order.getAmount()) {
                            BSaasVerifyRecord record = new BSaasVerifyRecord();
                            record.setFlag(VERIFY_FLAG_ANOMALY);
                            record.setErrorMessage("司机与乘客金额差异过大");
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
            }
            
            filtered.add(order);
        }
        
        bs.getReasonResult().setOrderList(filtered);
        return business;
    }
}

