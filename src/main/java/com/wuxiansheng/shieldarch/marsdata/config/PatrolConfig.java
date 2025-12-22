package com.wuxiansheng.shieldarch.marsdata.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 城市巡检配置
 *
 * 仅保留完整性校验任务所需字段：
 * - cityList: 城市名称列表
 * - patrolDict: 时间范围 -> (里程段 -> 期望数量)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatrolConfig {

    /**
     * 城市列表，对应 JSON 字段 city_list
     */
    @JsonProperty("city_list")
    private List<String> cityList = new ArrayList<>();

    /**
     * 巡检配置字典：
     * key: 时间范围（如 "6:30-7:00"）
     * value: 里程段 -> 期望数量
     * 对应 JSON 字段 patrol_dict
     */
    @JsonProperty("patrol_dict")
    private Map<String, Map<String, Integer>> patrolDict = new HashMap<>();
}



