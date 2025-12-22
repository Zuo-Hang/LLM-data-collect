package com.wuxiansheng.shieldarch.marsdata.business.xlprice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessFactory;
import com.wuxiansheng.shieldarch.marsdata.utils.QuestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 小拉计价业务工厂
 */
@Slf4j
@Component
public class XLPriceRuleBusinessFactory implements BusinessFactory {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private QuestUtils questUtils;
    
    @Override
    public Business createBusiness(String msg) {
        try {
            JsonNode parser = objectMapper.readTree(msg);
            
            // 获取日期
            JsonNode clientTimeNode = parser.path("clientTime");
            long clientTime = clientTimeNode.isNumber() ? clientTimeNode.asLong() : 0;
            String date = questUtils.msToDatetime(clientTime);
            
            // 获取小拉消息（JSON字符串）
            String xlMsg = questUtils.questTrim(getStringValue(parser, "data.data458"));
            
            return newXLPriceBusiness(date, xlMsg);
            
        } catch (Exception e) {
            log.error("创建小拉计价业务失败: msg={}, error={}", msg, e.getMessage(), e);
            throw new RuntimeException("创建业务失败", e);
        }
    }
    
    /**
     * 创建小拉计价业务
     */
    private Business newXLPriceBusiness(String date, String xlMsg) {
        try {
            XLRuleResponse xlPriceRule = objectMapper.readValue(xlMsg, XLRuleResponse.class);
            
            if (xlPriceRule.getRet() == null || xlPriceRule.getRet() != 0) {
                log.warn("NewXLPriceBusiness resp not 0, msg: {}", xlMsg);
                return null;
            }
            
            XLPriceRuleBusiness business = new XLPriceRuleBusiness();
            business.setDate(date);
            business.setData(xlPriceRule.getData());
            
            return business;
            
        } catch (Exception e) {
            log.warn("json Unmarshal resp err: {}, msg: {}", e.getMessage(), xlMsg);
            return null;
        }
    }
    
    /**
     * 从JSON节点获取字符串值
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

