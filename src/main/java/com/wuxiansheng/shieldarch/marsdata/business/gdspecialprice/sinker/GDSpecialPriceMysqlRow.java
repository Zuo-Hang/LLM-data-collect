package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice.sinker;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 高德特价车MySQL行数据
 * 唯一键：estimate_id + partner_name
 * 注意：表名根据环境动态设置（bubble_gd_special_price 或 bubble_gd_special_price_pre）
 */
@Data
public class GDSpecialPriceMysqlRow {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String estimateId;
    private String bubbleImageUrl;
    private String partnerName;
    private Double capPrice;
    private Double reducePrice;
    private String carType;
    private Integer cityId;
    private String cityName;
    private String createTime;
    private String updateTime;
    private Integer type; // 0: 原始数据, 1: 拟合结果
}

