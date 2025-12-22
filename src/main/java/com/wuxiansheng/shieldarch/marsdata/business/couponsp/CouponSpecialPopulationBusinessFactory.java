package com.wuxiansheng.shieldarch.marsdata.business.couponsp;

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
 * 券包人群标签识别业务工厂
 */
@Slf4j
@Component
public class CouponSpecialPopulationBusinessFactory implements BusinessFactory {
    
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
            
            CouponSpecialPopulationInput input = new CouponSpecialPopulationInput();
            input.setEstimateId(getStringValue(parser, "objId"));
            input.setActivityName(getStringValue(parser, "activityName"));
            input.setDate(questUtils.questTrim(getStringValue(parser, "data.data753")));
            
            String cityNameRaw = questUtils.questTrim(getStringValue(parser, "data.data632"));
            input.setCityName(questUtils.questFormatCityName(cityNameRaw));
            input.setCityId(CityMap.getCityMap().get(input.getCityName()));
            
            input.setLabel(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data381.0.value")));
            input.setHasActInMainPage("是".equals(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data852.0.value"))));
            input.setActUrlsInMainPage(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data86.#.key")));
            
            input.setHasActInVenue("是".equals(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data150.0.value"))));
            input.setActUrlsInVenue(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data956.#.key")));
            
            input.setHasOtherAct("是".equals(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data379.0.value"))));
            input.setCouponListAndDetailUrls(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data90.#.key")));
            
            JsonNode clientTimeNode = parser.path("clientTime");
            input.setSubmitTimestampMs(clientTimeNode.isNumber() ? clientTimeNode.asLong() : 0);
            
            input.setIsCouponComplete("是".equals(questUtils.questTrim(getStringValue(parser, "data.optionsWithId.data534.0.value"))));
            
            CouponSpecialPopulationBusiness business = new CouponSpecialPopulationBusiness();
            business.setInput(input);
            
            return business;
            
        } catch (Exception e) {
            log.error("创建券包人群标签识别业务失败: msg={}, error={}", msg, e.getMessage(), e);
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

