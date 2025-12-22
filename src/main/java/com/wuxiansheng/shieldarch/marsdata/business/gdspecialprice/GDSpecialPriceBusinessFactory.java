package com.wuxiansheng.shieldarch.marsdata.business.gdspecialprice;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 高德特价车业务工厂
 */
@Slf4j
@Component
public class GDSpecialPriceBusinessFactory implements BusinessFactory {
    
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
            
            GDSpecialPriceInput input = new GDSpecialPriceInput();
            input.setEstimateId(getStringValue(parser, "objId"));
            
            // 合并多个字段的图片URL
            List<String> imageUrls = new ArrayList<>();
            imageUrls.addAll(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data565.#.key")));
            imageUrls.addAll(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data529.#.key")));
            imageUrls.addAll(questUtils.questImageUrlsV2(
                gjsonUtils.gjsonExtractStringSlice(msg, "data.data552.#.key")));
            input.setBubbleImageUrls(imageUrls);
            
            JsonNode clientTimeNode = parser.path("clientTime");
            input.setClientTime(clientTimeNode.isNumber() ? clientTimeNode.asLong() : 0);
            
            String cityNameRaw = questUtils.questTrim(getStringValue(parser, "data.data86"));
            input.setCityName(questUtils.questFormatCityName(cityNameRaw));
            input.setCityId(CityMap.getCityMap().get(input.getCityName()));
            
            GDSpecialPriceBusiness business = new GDSpecialPriceBusiness();
            business.setInput(input);
            
            return business;
            
        } catch (Exception e) {
            log.error("创建高德特价车业务失败: msg={}, error={}", msg, e.getMessage(), e);
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

