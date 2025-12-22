package com.wuxiansheng.shieldarch.marsdata.llm.classify;

import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import lombok.Data;

import java.util.*;

/**
 * 简单分类器
 */
@Data
public class SimpleClassifier implements BusinessContext.Classifier {
    
    /**
     * 每个key可能有多个分类
     */
    private Map<String, List<String>> keyClassifiers = new HashMap<>();
    
    public SimpleClassifier() {
        this.keyClassifiers = new HashMap<>();
    }
    
    @Override
    public String[] get(String key) {
        List<String> res = keyClassifiers.get(key);
        if (res != null) {
            return res.toArray(new String[0]);
        }
        return new String[0];
    }
    
    public void set(String key, String category) {
        keyClassifiers.put(key, Collections.singletonList(category));
    }
    
    public void append(String key, String... categories) {
        if (categories == null || categories.length == 0) {
            return;
        }
        List<String> existing = keyClassifiers.getOrDefault(key, new ArrayList<>());
        existing.addAll(Arrays.asList(categories));
        keyClassifiers.put(key, existing);
    }
}

