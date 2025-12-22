package com.wuxiansheng.shieldarch.marsdata.service;

import com.wuxiansheng.shieldarch.marsdata.config.BusinessConfigService;
import com.wuxiansheng.shieldarch.marsdata.io.QuestService;
import com.wuxiansheng.shieldarch.marsdata.mq.Producer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 回溯服务
 */
@Slf4j
@Service
public class BackstraceService {
    
    @Autowired(required = false)
    private QuestService questService;
    
    @Autowired(required = false)
    private Producer producer;
    
    @Autowired(required = false)
    private BusinessConfigService businessConfigService;
    
    @Value("${mq.producer.quest-backstrace-topic:quest_backstrace}")
    private String questBackstraceTopic;
    
    /**
     * 业务名称到问卷活动名称的映射
     */
    private static final Map<String, List<String>> BUSINESS_TO_QUEST_ACTIVITY_NAME_MAP = new HashMap<>();
    
    static {
        BUSINESS_TO_QUEST_ACTIVITY_NAME_MAP.put("gd_bubble", Arrays.asList("ddpage_0I5ObjQ8"));
        BUSINESS_TO_QUEST_ACTIVITY_NAME_MAP.put("xl_bubble", Arrays.asList("ddpage_0OosRo48"));
        BUSINESS_TO_QUEST_ACTIVITY_NAME_MAP.put("coupon_sp", Arrays.asList("ddpage_0Oo5sN8t"));
        BUSINESS_TO_QUEST_ACTIVITY_NAME_MAP.put("price_rule_xl", Arrays.asList("ddpage_0O8YPI7s"));
    }
    
    /**
     * 回溯请求
     */
    @Data
    public static class BackstraceReq {
        private String businessName;
        private String activityName;
        private String from;
        private String to;
        
        /**
         * 验证请求参数
         */
        public void validate() throws Exception {
            if ((businessName == null || businessName.isEmpty()) && 
                (activityName == null || activityName.isEmpty())) {
                throw new Exception(String.format("invalid business_name and activity_name: %s", businessName));
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime fromTime;
            LocalDateTime toTime;
            
            try {
                fromTime = LocalDateTime.parse(from, formatter);
            } catch (Exception e) {
                throw new Exception(String.format("invalid from: %s", from), e);
            }
            
            try {
                toTime = LocalDateTime.parse(to, formatter);
            } catch (Exception e) {
                throw new Exception(String.format("invalid to: %s", to), e);
            }
            
            long daysDiff = java.time.Duration.between(fromTime, toTime).toDays();
            if (daysDiff < 0 || daysDiff > 30) {
                throw new Exception(String.format("invalid range, day_diff: %d", daysDiff));
            }
        }
        
        /**
         * 按天拆分时间范围
         */
        public List<TimeRange> splitTimeRangeByDays() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime fromTime = LocalDateTime.parse(from, formatter);
            LocalDateTime toTime = LocalDateTime.parse(to, formatter);
            
            List<TimeRange> res = new ArrayList<>();
            LocalDateTime current = fromTime;
            
            while (current.isBefore(toTime)) {
                LocalDateTime nextDay = current.plusDays(1);
                LocalDateTime nextDayStart = LocalDateTime.of(
                    nextDay.getYear(), nextDay.getMonth(), nextDay.getDayOfMonth(), 0, 0, 0);
                
                if (nextDayStart.isBefore(toTime) || nextDayStart.isEqual(toTime)) {
                    res.add(new TimeRange(current, nextDayStart));
                    current = nextDayStart;
                } else {
                    res.add(new TimeRange(current, toTime));
                    break;
                }
            }
            
            return res;
        }
    }
    
    /**
     * 时间范围
     */
    @Data
    public static class TimeRange {
        private LocalDateTime from;
        private LocalDateTime to;
        
        public TimeRange(LocalDateTime from, LocalDateTime to) {
            this.from = from;
            this.to = to;
        }
        
        public String getFromString() {
            return from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        public String getToString() {
            return to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    /**
     * 执行回溯
     */
    public void backstrace(BackstraceReq req) throws Exception {
        req.validate();
        
        List<Backstracer> backstracers = businessBackstracerMap(req.getBusinessName());
        
        // 优先使用 activity_name 入参
        if (req.getActivityName() != null && !req.getActivityName().isEmpty()) {
            backstracers = newQuestBackstracers(Arrays.asList(req.getActivityName()));
        }
        
        for (Backstracer backstracer : backstracers) {
            // 按天回溯
            for (TimeRange timeRange : req.splitTimeRangeByDays()) {
                backstracer.handle(timeRange.getFromString(), timeRange.getToString());
            }
        }
    }
    
    /**
     * 获取业务对应的回溯器列表
     */
    private List<Backstracer> businessBackstracerMap(String businessName) {
        List<String> activityNames = BUSINESS_TO_QUEST_ACTIVITY_NAME_MAP.get(businessName);
        if (activityNames == null || activityNames.isEmpty()) {
            return new ArrayList<>();
        }
        return newQuestBackstracers(activityNames);
    }
    
    /**
     * 创建问卷回溯器列表
     */
    private List<Backstracer> newQuestBackstracers(List<String> activityNames) {
        List<Backstracer> res = new ArrayList<>();
        for (String activityName : activityNames) {
            res.add(new QuestBackstracer(activityName));
        }
        return res;
    }
    
    /**
     * 回溯器接口
     */
    public interface Backstracer {
        void handle(String from, String to) throws Exception;
    }
    
    /**
     * 问卷回溯器
     */
    private class QuestBackstracer implements Backstracer {
        private final String activityName;
        
        public QuestBackstracer(String activityName) {
            this.activityName = activityName;
        }
        
        @Override
        public void handle(String from, String to) throws Exception {
            if (questService == null) {
                throw new Exception("QuestService未初始化");
            }
            
            List<String> quests = questService.queryQuestByCreateAt(activityName, from, to);
            if (quests == null) {
                quests = new ArrayList<>();
            }
            
            log.info("QuestBackstracer succ get msgs: {}", quests.size());
            
            if (producer == null) {
                throw new Exception("Producer未初始化");
            }
            
            for (String quest : quests) {
                try {
                    producer.send(questBackstraceTopic, quest);
                } catch (Exception e) {
                    log.warn("QuestBackstracer send mq err: {}", e.getMessage());
                    throw e;
                }
            }
        }
    }
}

