package com.wuxiansheng.shieldarch.marsdata.business.bsaas;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单列表原始数据
 */
@Data
public class BSaasOrderListRaw {
    @JsonProperty("year_month")
    private String yearMonth;
    
    @JsonProperty("month_day")
    private String monthDay;
    
    private List<String> tags = new ArrayList<>();
    private String time;
    
    @JsonProperty("order_time")
    private String orderTime;
    
    private String status;
    private FlexibleNumber amount;
    
    public BSaasOrderListItem toModel(String imageURL, int imageIndex) {
        BSaasOrderListItem item = new BSaasOrderListItem();
        item.setYearMonth(this.yearMonth);
        item.setMonthDay(this.monthDay);
        item.setTags(new ArrayList<>(this.tags));
        item.setTime(this.time);
        item.setOrderTime(this.orderTime);
        item.setStatus(this.status);
        item.setAmount(this.amount != null ? this.amount.toFloat() : 0.0);
        item.setAmountText(this.amount != null ? this.amount.string() : "");
        item.setImageURL(imageURL);
        item.setImageIndex(imageIndex);
        return item;
    }
}

