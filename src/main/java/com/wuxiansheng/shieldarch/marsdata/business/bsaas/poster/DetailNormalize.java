package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasDriverDetail;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasPassengerDetail;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasPersonalHomepage;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 详情标准化Poster
 */
@Slf4j
@Component
public class DetailNormalize implements Poster {
    
    private static final double EMPTY_THRESHOLD = 1e-6;
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null) {
            return business;
        }
        
        // 标准化日期
        normalizeDates(bs);
        
        // 标准化乘客明细的产品类型
        if (bs.getReasonResult().getPassengerDetails() != null) {
            for (BSaasPassengerDetail detail : bs.getReasonResult().getPassengerDetails()) {
                detail.setProductType(normalizePassengerProductType(detail.getProductType()));
            }
        }
        
        // 标准化司机明细的信息费
        if (bs.getReasonResult().getDriverDetails() != null) {
            for (BSaasDriverDetail detail : bs.getReasonResult().getDriverDetails()) {
                // 如果InfoFeeAfterSubsidy为空，尝试从InfoFee或InfoFeeBeforeSubsidy填充
                if (isZeroFloat(detail.getInfoFeeAfterSubsidy(), EMPTY_THRESHOLD)) {
                    if (!isZeroFloat(detail.getInfoFee(), EMPTY_THRESHOLD)) {
                        detail.setInfoFeeAfterSubsidy(detail.getInfoFee());
                    } else if (!isZeroFloat(detail.getInfoFeeBeforeSubsidy(), EMPTY_THRESHOLD)) {
                        detail.setInfoFeeAfterSubsidy(detail.getInfoFeeBeforeSubsidy());
                    }
                }
            }
            
            // 过滤掉TakeRate为空的司机明细
            bs.getReasonResult().setDriverDetails(filterDriverDetailsByTakeRate(bs.getReasonResult().getDriverDetails()));
        }
        
        return business;
    }
    
    /**
     * 标准化日期
     */
    private void normalizeDates(BSaasBusiness bs) {
        // 标准化订单列表的日期
        if (bs.getReasonResult().getOrderList() != null) {
            for (var order : bs.getReasonResult().getOrderList()) {
                order.setOrderDate(normalizeDateString(order.getOrderDate()));
                order.setYearMonth(normalizeYearMonth(order.getYearMonth()));
                order.setMonthDay(normalizeMonthDay(order.getMonthDay()));
                order.setChargeType(normalizeChargeType(order.getChargeType(), order.getTags()));
            }
        }
        
        // 标准化历史统计的日期
        if (bs.getReasonResult().getHistoricalStatistics() != null) {
            for (var stat : bs.getReasonResult().getHistoricalStatistics()) {
                stat.setStartDate(normalizeDateString(stat.getStartDate()));
                stat.setEndDate(normalizeDateString(stat.getEndDate()));
            }
        }
        
        // 标准化业绩交易的日期
        if (bs.getReasonResult().getPerformanceTransactions() != null) {
            for (var perf : bs.getReasonResult().getPerformanceTransactions()) {
                perf.setStartDate(normalizeDateString(perf.getStartDate()));
                perf.setEndDate(normalizeDateString(perf.getEndDate()));
            }
        }
        
        // 标准化个人主页
        if (bs.getReasonResult().getPersonalHomepage() != null) {
            for (var home : bs.getReasonResult().getPersonalHomepage()) {
                if (home.getImageURL() != null) {
                    home.setImageURL(home.getImageURL().trim());
                }
            }
            bs.getReasonResult().setPersonalHomepage(deduplicatePersonalHomepage(bs.getReasonResult().getPersonalHomepage()));
        }
    }
    
    /**
     * 标准化日期字符串
     */
    private String normalizeDateString(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = date.trim();
        String replaced = trimmed
            .replace("年", "-")
            .replace("月", "-")
            .replace("日", "")
            .replace("\u3000", "")
            .replace(".", "-")
            .replace("/", "-")
            .replace("--", "-");
        
        String[] parts = replaced.split("-");
        String year = "";
        String month = "";
        String day = "";
        
        if (parts.length >= 3) {
            year = parts[0].trim();
            month = parts[1].trim();
            day = parts[2].trim();
        } else if (parts.length == 2) {
            year = parts[0].trim();
            month = parts[1].trim();
        } else if (parts.length == 1) {
            year = parts[0].trim();
        }
        
        if (year.isEmpty()) {
            return "";
        }
        
        month = ensureTwoDigits(month);
        day = ensureTwoDigits(day);
        
        if (day.isEmpty()) {
            day = "01";
        }
        if (month.isEmpty() && year.length() > 4) {
            month = ensureTwoDigits(year.substring(4));
            year = year.substring(0, 4);
        }
        if (month.isEmpty()) {
            month = "01";
        }
        
        return String.join("/", year, month, day);
    }
    
    /**
     * 去重个人主页
     */
    private List<BSaasPersonalHomepage> deduplicatePersonalHomepage(List<BSaasPersonalHomepage> list) {
        if (list == null || list.size() <= 1) {
            return list;
        }
        
        class Key {
            String driverName;
            String carNumber;
            String carType;
            
            Key(String driverName, String carNumber, String carType) {
                this.driverName = driverName != null ? driverName.trim() : "";
                this.carNumber = carNumber != null ? carNumber.trim() : "";
                this.carType = carType != null ? carType.trim() : "";
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Key key = (Key) o;
                return Objects.equals(driverName, key.driverName) &&
                       Objects.equals(carNumber, key.carNumber) &&
                       Objects.equals(carType, key.carType);
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(driverName, carNumber, carType);
            }
        }
        
        List<BSaasPersonalHomepage> result = new ArrayList<>();
        Map<Key, Integer> seen = new HashMap<>();
        
        for (BSaasPersonalHomepage home : list) {
            Key k = new Key(
                home.getDriverName(),
                home.getCarNumber(),
                home.getCarType()
            );
            
            Integer idx = seen.get(k);
            if (idx != null) {
                BSaasPersonalHomepage existing = result.get(idx);
                if (existing.getImageIndex() == null || existing.getImageIndex() < 0 ||
                    (home.getImageIndex() != null && home.getImageIndex() >= 0 && home.getImageIndex() < existing.getImageIndex())) {
                    result.set(idx, home);
                }
                continue;
            }
            
            seen.put(k, result.size());
            result.add(home);
        }
        
        return result;
    }
    
    /**
     * 标准化年月
     */
    private String normalizeYearMonth(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = value.trim();
        String parts = trimmed
            .replace("年", "-")
            .replace("月", "")
            .replace(".", "-");
        
        String[] split = parts.split("-");
        if (split.length >= 2) {
            String year = split[0].trim();
            String month = ensureTwoDigits(split[1].trim());
            if (!year.isEmpty() && !month.isEmpty()) {
                return year + "/" + month;
            }
        }
        return trimmed;
    }
    
    /**
     * 标准化月日
     */
    private String normalizeMonthDay(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = value.trim();
        String normalized = trimmed
            .replace("月", "-")
            .replace("日", "")
            .replace("号", "");
        
        String[] split = normalized.split("-");
        if (split.length >= 2) {
            String month = ensureTwoDigits(split[0].trim());
            String day = ensureTwoDigits(split[1].trim());
            return month + "月" + day + "日";
        }
        return trimmed;
    }
    
    /**
     * 标准化乘客产品类型
     */
    private String normalizePassengerProductType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.contains("特惠")) {
            return "一口价";
        }
        return trimmed;
    }
    
    /**
     * 标准化费用类型
     */
    private String normalizeChargeType(String current, List<String> tags) {
        if (tags == null) {
            return current;
        }
        for (String tag : tags) {
            if (tag != null && tag.trim().contains("特惠")) {
                return "一口价";
            }
        }
        return current;
    }
    
    /**
     * 过滤司机明细（按TakeRate）
     */
    private List<BSaasDriverDetail> filterDriverDetailsByTakeRate(List<BSaasDriverDetail> details) {
        if (details == null) {
            return new ArrayList<>();
        }
        List<BSaasDriverDetail> result = new ArrayList<>();
        for (BSaasDriverDetail detail : details) {
            // TakeRate不为空
            if (detail.getTakeRate() != null) {
                result.add(detail);
            }
        }
        return result;
    }
    
    /**
     * 判断浮点数是否为零
     */
    private boolean isZeroFloat(Double v, double epsilon) {
        if (v == null) {
            return true;
        }
        if (epsilon <= 0) {
            epsilon = 1e-9;
        }
        return Math.abs(v) <= epsilon;
    }
    
    /**
     * 确保两位数字
     */
    private String ensureTwoDigits(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = val.trim();
        java.util.regex.Matcher matcher = DIGIT_PATTERN.matcher(trimmed);
        if (!matcher.find()) {
            return "";
        }
        
        String digits = matcher.group();
        try {
            int n = Integer.parseInt(digits);
            return String.format("%02d", n);
        } catch (NumberFormatException e) {
            if (digits.length() >= 2) {
                return digits.substring(digits.length() - 2);
            }
            return "0" + digits;
        }
    }
}

