package com.wuxiansheng.shieldarch.marsdata.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.config.ApolloConfigService;
import com.wuxiansheng.shieldarch.marsdata.monitor.StatsdClient;
import com.wuxiansheng.shieldarch.marsdata.utils.DiSFUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM客户端
 */
@Slf4j
@Service
public class LLMClient {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private StatsdClient statsdClient;
    
    @Autowired
    private ApolloConfigService apolloConfigService;
    
    @Autowired(required = false)
    private DiSFUtils diSFUtils;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    /**
     * LLM集群配置
     */
    @Data
    public static class LLMClusterConf {
        private String disfName;
        private String appId;
        private LLMParams params;
        
        @Data
        public static class LLMParams {
            private String model;
            private int maxTokens;
            private double temperature;
            private double topK;
            private double topP;
            private double repetitionPenalty;
            private boolean stream;
        }
    }
    
    /**
     * LLM请求
     */
    @Data
    public static class RequestLLMRequest {
        private String llmDisfName;
        private String caller;
        private Map<String, String> headers;
        private String params;
        private String reqUrl;
        private String prompt;
    }
    
    /**
     * LLM响应
     */
    @Data
    public static class LLMResponse {
        private String id;
        private String object;
        private long created;
        private String model;
        private List<LLMResponseChoice> choices;
        private LLMResponseUsage usage;
        private String promptLogprobs;
        
        @Data
        public static class LLMResponseChoice {
            private int index;
            private LLMResponseMessage message;
            private String logprobs;
            private String finishReason;
            private String stopReason;
        }
        
        @Data
        public static class LLMResponseMessage {
            private String role;
            private String reasoningContent;
            private String content;
            private List<Object> toolCalls;
        }
        
        @Data
        public static class LLMResponseUsage {
            private int promptTokens;
            private int totalTokens;
            private int completionTokens;
            private String promptTokensDetails;
        }
    }
    
    /**
     * LLM请求消息
     */
    @Data
    public static class LLMRequest {
        private String model;
        private List<Object> messages;
        private int maxTokens;
        private double temperature;
        private double topK;
        private double topP;
        private double repetitionPenalty;
        private boolean stream;
    }
    
    /**
     * LLM消息
     */
    @Data
    public static class LLMMessage {
        private String role;
        private List<LLMMessageContent> content;
    }
    
    @Data
    public static class LLMMessageContent {
        private String type;
        private String text;
        private MessageImageUrl imageUrl;
    }
    
    @Data
    public static class MessageImageUrl {
        private String url;
    }
    
    /**
     * 创建LLM请求
     */
    public RequestLLMRequest newRequestLLMRequest(String businessName, String url, String prompt) {
        LLMClusterConf conf = getLLMClusterConf(businessName);
        
        RequestLLMRequest request = new RequestLLMRequest();
        request.setLlmDisfName(conf.getDisfName());
        request.setCaller(businessName);
        request.setReqUrl(url);
        request.setPrompt(prompt);
        
        // 设置Headers
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Luban-LLM-Service-APPId", conf.getAppId());
        headers.put("Content-type", "application/json");
        request.setHeaders(headers);
        
        // 设置Params
        try {
            String paramsJson = objectMapper.writeValueAsString(conf.getParams());
            request.setParams(paramsJson);
        } catch (Exception e) {
            log.error("序列化LLM参数失败: businessName={}, error={}", businessName, e.getMessage(), e);
        }
        
        return request;
    }
    
    /**
     * 请求LLM
     * 
     * @param request LLM请求
     * @return LLM响应
     */
    public LLMResponse requestLLM(RequestLLMRequest request) throws Exception {
        long beginTime = System.currentTimeMillis();
        Exception error = null;
        
        try {
            // 构建消息
            LLMMessage message = new LLMMessage();
            message.setRole("user");
            
            List<LLMMessageContent> contents = new ArrayList<>();
            // 文本内容
            LLMMessageContent textContent = new LLMMessageContent();
            textContent.setType("text");
            textContent.setText(request.getPrompt());
            contents.add(textContent);
            
            // 图片内容
            LLMMessageContent imageContent = new LLMMessageContent();
            imageContent.setType("image_url");
            MessageImageUrl imageUrl = new MessageImageUrl();
            imageUrl.setUrl(request.getReqUrl());
            imageContent.setImageUrl(imageUrl);
            contents.add(imageContent);
            
            message.setContent(contents);
            
            // 构建请求体
            LLMRequest llmReq = objectMapper.readValue(request.getParams(), LLMRequest.class);
            llmReq.setMessages(List.of(message));
            
            // 获取HTTP端点
            String endpoint = getHttpEndpoint(request.getLlmDisfName());
            if (endpoint == null || endpoint.isEmpty()) {
                throw new Exception("no valid llm endpoint: " + request.getLlmDisfName());
            }
            
            String url = "http://" + endpoint + "/v1/chat/completions";
            
            // 发送HTTP请求
            String requestBody = objectMapper.writeValueAsString(llmReq);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .headers(request.getHeaders().entrySet().stream()
                            .flatMap(e -> java.util.stream.Stream.of(e.getKey(), e.getValue()))
                            .toArray(String[]::new))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new Exception("HTTP请求失败: statusCode=" + response.statusCode() + ", body=" + response.body());
            }
            
            // 解析响应
            LLMResponse llmResponse = objectMapper.readValue(response.body(), LLMResponse.class);
            
            log.info("requestLLM pic_url: {}, cost: {}ms", request.getReqUrl(), System.currentTimeMillis() - beginTime);
            
            return llmResponse;
            
        } catch (Exception e) {
            error = e;
            log.error("requestLLM失败: pic_url={}, error={}", request.getReqUrl(), e.getMessage(), e);
            throw e;
        } finally {
            // 上报指标
            if (statsdClient != null) {
                long duration = System.currentTimeMillis() - beginTime;
                statsdClient.recordRpcMetric("llm_req", request.getCaller(), "llm", duration, 
                    error == null ? 0 : 1);
            }
        }
    }
    
    /**
     * 获取LLM集群配置
     */
    private LLMClusterConf getLLMClusterConf(String businessName) {
        LLMClusterConf defaultConf = getDefaultLLMClusterConf();
        
        Map<String, String> params = apolloConfigService.getConfig(ApolloConfigService.OCR_LLM_CONF);
        if (params.isEmpty()) {
            log.error("获取Apollo配置失败: {}, 使用默认配置", ApolloConfigService.OCR_LLM_CONF);
            return defaultConf;
        }
        
        // TODO: 实现WithEnvPrefix逻辑（根据环境添加test_前缀）
        String confKey = "llm_cluster_conf_" + businessName;
        String confStr = params.get(confKey);
        
        if (confStr == null || confStr.isEmpty()) {
            return defaultConf;
        }
        
        try {
            LLMClusterConf conf = objectMapper.readValue(confStr, LLMClusterConf.class);
            return conf;
        } catch (Exception e) {
            log.error("解析LLM集群配置失败: businessName={}, conf={}, error={}", 
                businessName, confStr, e.getMessage(), e);
            return defaultConf;
        }
    }
    
    /**
     * 获取默认LLM集群配置
     */
    private LLMClusterConf getDefaultLLMClusterConf() {
        LLMClusterConf conf = new LLMClusterConf();
        conf.setDisfName("disf!machinelearning-luban-online-online-service-biz-Nautilus-OCR_model_online");
        conf.setAppId("k8s-sv0-uozuez-1754050816242");
        
        LLMClusterConf.LLMParams params = new LLMClusterConf.LLMParams();
        params.setModel("/nfs/ofs-llm-ssd/Qwen2.5-VL-32B-Instruct/main");
        params.setMaxTokens(8192);
        params.setTemperature(0.3);
        params.setTopK(50);
        params.setTopP(0.8);
        params.setRepetitionPenalty(1.0);
        params.setStream(false);
        conf.setParams(params);
        
        return conf;
    }
    
    /**
     * 获取HTTP端点
     * 
     * @param disfName DiSF服务名称
     * @return HTTP端点（格式：ip:port），如果获取失败返回null
     */
    private String getHttpEndpoint(String disfName) {
        if (disfName == null || disfName.isEmpty()) {
            log.warn("DiSF服务名称为空");
            return null;
        }
        
        // 兼容测试环境的vip（不包含disf!前缀）
        if (!disfName.contains("disf!")) {
            log.debug("使用测试环境VIP: {}", disfName);
            return disfName;
        }
        
        // 使用DiSF工具类获取服务端点
        if (diSFUtils != null) {
            String endpoint = diSFUtils.getHttpEndpoint(disfName);
            if (endpoint != null && !endpoint.isEmpty()) {
                return endpoint;
            }
        }
        
        // 如果DiSF工具类不可用，记录警告
        log.warn("DiSF服务发现不可用，无法获取端点: {}", disfName);
        log.warn("请确保DiSF Java SDK已正确配置和初始化");
        
        return null;
    }
}

