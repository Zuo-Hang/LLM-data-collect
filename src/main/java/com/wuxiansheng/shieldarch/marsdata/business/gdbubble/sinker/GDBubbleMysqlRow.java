package com.wuxiansheng.shieldarch.marsdata.business.gdbubble.sinker;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 高德冒泡MySQL行数据
 * 注意：表名根据环境动态设置（ocr_bubble_data_v2 或 ocr_bubble_data_v2_pre）
 */
@Data
public class GDBubbleMysqlRow {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String estimateBubbleId;
    private String estimateId;
    private Integer cityId;
    private String cityName;
    private String createTime;
    private String bubbleImageUrl;
    private String bubbleAggregation;
    private String estArriveTime;
    private Double estDistance;
    private Integer estDuration;
    private Double estPay;
    private Double reducePrice;
    private Double dynamicPrice;
    private String partnerName;
    private String carType;
    private String phone;
    private String submitTime;
    private String capPrice;
    private String startDistrict;
    private String startPoi;
    private String endPoi;
    private String partyOffer;
    private String otherPrice;
    private String time;
    private String ridePlatformId;
    private String ridePlatformName;
    private String expenseTime;
    private String userType;
    private String responseDurationWj;
    private String estArriveTimeWj;
    private String estArriveDistanceWj;
    private String tCoinReducePrice;
    private String isFromOcrResult;
    private String submitName;
    private String timeRange;
    private String disRange;
    private String isStation;
    private String stationName;
    private String systemMsg;
    private String reduceType;
    private String extraPrice;
    private String dataCheckStatus;
    private String startLng;
    private String startLat;
    private String routeType;
    private String route;
    private String districtType;
    private String districtName;
}

