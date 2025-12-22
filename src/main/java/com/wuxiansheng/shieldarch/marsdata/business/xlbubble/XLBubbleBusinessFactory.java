package com.wuxiansheng.shieldarch.marsdata.business.xlbubble;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.config.CityMap;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessFactory;
import com.wuxiansheng.shieldarch.marsdata.utils.GjsonUtils;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 小拉冒泡业务工厂
 */
@Slf4j
@Component
public class XLBubbleBusinessFactory implements BusinessFactory {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private QuestUtils questUtils;
    
    @Autowired
    private GjsonUtils gjsonUtils;
    
    @Override
    public Business createBusiness(String msg) {
        try {
            JsonNode parser = objectMapper.readTree(msg);
            
            XLBubbleInput input = new XLBubbleInput();
            input.setEstimateId(getStringValue(parser, "objId"));
            input.setActivityName(getStringValue(parser, "activityName"));
            input.setBubbleImageUrls(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data397.#.key")));
            
            String cityNameRaw = questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data552.0.value"));
            input.setCityName(questUtils.questFormatCityName(cityNameRaw));
            input.setCityId(CityMap.getCityMap().get(input.getCityName()));
            
            input.setTimeRange(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data69.0.value")));
            input.setDisRange(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data952.0.value")));
            input.setDistrictType(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data691.0.value")));
            input.setDistrictName(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data99.0.value")));
            input.setSubmitName(getStringValue(parser, "data.data130"));
            
            JsonNode clientTimeNode = parser.path("clientTime");
            long clientTime = clientTimeNode.isNumber() ? clientTimeNode.asLong() : 0;
            input.setSubmitTimestampMs(clientTime);
            input.setSubmitTime(questUtils.msToDatetime(clientTime));
            
            // 如果区县类型是"主城"且区县名为空，则设置为"主城"
            if ("主城".equals(input.getDistrictType()) && 
                (input.getDistrictName() == null || input.getDistrictName().isEmpty())) {
                input.setDistrictName(input.getDistrictType());
            }
            
            XLBubbleBusiness business = new XLBubbleBusiness();
            business.setInput(input);
            
            return business;
            
        } catch (Exception e) {
            log.error("创建小拉冒泡业务失败: msg={}, error={}", msg, e.getMessage(), e);
            throw new RuntimeException("创建业务失败", e);
        }
    }
    
    /**
     * 从JSON节点获取字符串值（支持路径，如 "data.optionsWithId.data381.0.value"）
     */
    private String getStringValue(JsonNode root, String path) {
        try {
            String[] parts = path.split("\\.");
            JsonNode node = root;
            
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    // 数组索引
                    int index = Integer.parseInt(part);
                    if (node.isArray() && index < node.size()) {
                        node = node.get(index);
                    } else {
                        return "";
                    }
                } else {
                    // 对象字段
                    if (node.has(part)) {
                        node = node.get(part);
                    } else {
                        return "";
                    }
                }
            }
            
            return node.isTextual() ? node.asText() : node.asText();
        } catch (Exception e) {
            return "";
        }
    }
}

