package com.wuxiansheng.shieldarch.marsdata.llm;

import com.wuxiansheng.shieldarch.marsdata.config.BusinessConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务注册表
 */
@Slf4j
@Component
public class BusinessRegistry {
    
    @Autowired
    private BusinessConfigService businessConfigService;
    
    /**
     * 业务工厂映射表
     */
    private final Map<String, BusinessFactory> businessFactoryMap = new ConcurrentHashMap<>();
    
    /**
     * 业务Poster映射表
     */
    private final Map<String, List<Poster>> businessPostersMap = new ConcurrentHashMap<>();
    
    /**
     * 业务Sinker映射表
     */
    private final Map<String, List<Sinker>> businessSinkersMap = new ConcurrentHashMap<>();
    
    /**
     * 注册业务工厂
     */
    public void registerBusinessFactory(String businessName, BusinessFactory factory) {
        businessFactoryMap.put(businessName, factory);
        log.info("注册业务工厂: {}", businessName);
    }
    
    /**
     * 注册Posters
     */
    public void registerPosters(String businessName, List<Poster> posters) {
        businessPostersMap.put(businessName, new ArrayList<>(posters));
        // 同时注册测试版本的Posters
        businessPostersMap.put(businessName + "_test", new ArrayList<>(posters));
        log.info("注册Posters: {}, count: {}", businessName, posters.size());
    }
    
    /**
     * 注册Sinkers
     */
    public void registerSinkers(String businessName, List<Sinker> sinkers) {
        businessSinkersMap.put(businessName, new ArrayList<>(sinkers));
        // 同时注册测试版本的Sinkers
        businessSinkersMap.put(businessName + "_test", new ArrayList<>(sinkers));
        log.info("注册Sinkers: {}, count: {}", businessName, sinkers.size());
    }
    
    /**
     * 创建业务对象列表
     */
    public List<Business> createBusinesses(String sourceUniqueId, String msg) {
        // 从配置中获取sourceUniqueId对应的业务名称列表
        List<String> businessNames = getBusinessNamesBySource(sourceUniqueId);
        
        List<Business> businesses = new ArrayList<>();
        for (String businessName : businessNames) {
            BusinessFactory factory = businessFactoryMap.get(businessName);
            if (factory == null) {
                log.error("未找到业务工厂配置: {}", businessName);
                continue;
            }
            
            Business business = factory.createBusiness(msg);
            if (business != null) {
                businesses.add(business);
            }
        }
        
        return businesses;
    }
    
    /**
     * 获取Posters
     */
    public List<Poster> getPosters(String businessName) {
        return businessPostersMap.getOrDefault(businessName, Collections.emptyList());
    }
    
    /**
     * 获取Sinkers
     */
    public List<Sinker> getSinkers(String businessName) {
        return businessSinkersMap.getOrDefault(businessName, Collections.emptyList());
    }
    
    /**
     * 获取所有业务名称
     */
    public List<String> getAllBusinessNames() {
        return new ArrayList<>(businessFactoryMap.keySet());
    }
    
    /**
     * 根据sourceUniqueId获取业务名称列表
     */
    private List<String> getBusinessNamesBySource(String sourceUniqueId) {
        return businessConfigService.getSourceBusinessNames(sourceUniqueId);
    }
}

