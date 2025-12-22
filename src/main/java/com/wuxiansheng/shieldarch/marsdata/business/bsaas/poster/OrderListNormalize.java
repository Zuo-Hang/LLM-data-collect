package com.wuxiansheng.shieldarch.marsdata.business.bsaas.poster;

import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasBusiness;
import com.wuxiansheng.shieldarch.marsdata.business.bsaas.BSaasOrderListItem;
import com.wuxiansheng.shieldarch.marsdata.llm.Business;
import com.wuxiansheng.shieldarch.marsdata.llm.BusinessContext;
import com.wuxiansheng.shieldarch.marsdata.llm.Poster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 订单列表标准化Poster
 */
@Slf4j
@Component
public class OrderListNormalize implements Poster {
    
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月");
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM月dd日");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public Business apply(BusinessContext bctx, Business business) {
        if (!(business instanceof BSaasBusiness)) {
            return business;
        }
        
        BSaasBusiness bs = (BSaasBusiness) business;
        if (bs.getReasonResult() == null || bs.getReasonResult().getOrderList() == null) {
            return business;
        }
        
        // 获取默认值
        OrderDefaults defaults = deriveOrderDefaults(bs.getInput().getSubmitDateTime());
        
        // 标准化每个订单项
        for (BSaasOrderListItem order : bs.getReasonResult().getOrderList()) {
            normalizeOrderItem(order, defaults);
        }
        
        // 过滤不完整的订单
        bs.getReasonResult().setOrderList(filterIncompleteOrders(bs.getReasonResult().getOrderList()));
        
        return business;
    }
    
    /**
     * 订单默认值
     */
    private static class OrderDefaults {
        String yearMonth;
        String monthDay;
    }
    
    /**
     * 从提交时间推导默认值
     */
    private OrderDefaults deriveOrderDefaults(LocalDateTime submitDateTime) {
        OrderDefaults defaults = new OrderDefaults();
        if (submitDateTime == null) {
            return defaults;
        }
        defaults.yearMonth = submitDateTime.format(YEAR_MONTH_FORMATTER);
        defaults.monthDay = submitDateTime.format(MONTH_DAY_FORMATTER);
        return defaults;
    }
    
    /**
     * 标准化订单项
     */
    private void normalizeOrderItem(BSaasOrderListItem order, OrderDefaults defaults) {
        // 标准化年月
        if (order.getYearMonth() == null || order.getYearMonth().isEmpty()) {
            order.setYearMonth(normalizeChineseYearMonth(order.getMonthDay(), defaults.yearMonth));
        }
        if (order.getYearMonth() == null || order.getYearMonth().isEmpty()) {
            order.setYearMonth(defaults.yearMonth);
        }
        
        // 标准化月日
        if (order.getMonthDay() == null || order.getMonthDay().isEmpty()) {
            order.setMonthDay(defaults.monthDay);
        }
        order.setMonthDay(normalizeChineseMonthDay(order.getMonthDay()));
        order.setYearMonth(normalizeChineseYearMonth(order.getYearMonth(), defaults.yearMonth));
        
        // 标准化时间
        order.setOrderTime(normalizeTime(order.getOrderTime()));
        
        // 解析金额文本到数值
        if (order.getAmount() == null || order.getAmount() == 0) {
            if (order.getAmountText() != null && !order.getAmountText().isEmpty()) {
                Double value = parseAmount(order.getAmountText());
                if (value != null) {
                    order.setAmount(value);
                }
            }
        }
        if (order.getAmount() != null) {
            order.setAmount(roundPrice(order.getAmount()));
        }
        
        // 构建订单日期
        order.setOrderDate(buildOrderDate(order.getYearMonth(), order.getMonthDay()));
        
        // 解析订单标签
        OrderTagMeta meta = parseOrderTags(order.getTags());
        order.setTags(meta.tags);
        order.setChargeType(meta.chargeType);
        order.setOrderType(meta.orderType);
        order.setTakeFeeType(meta.takeFeeType);
        order.setOrderChannel(meta.orderChannel);
        
        // 生成订单ID
        if (order.getOrderID() == null || order.getOrderID().isEmpty()) {
            order.setOrderID(generateOrderID(order.getYearMonth(), order.getMonthDay(), order.getOrderTime()));
        }
    }
    
    /**
     * 过滤不完整的订单
     */
    private List<BSaasOrderListItem> filterIncompleteOrders(List<BSaasOrderListItem> orders) {
        List<BSaasOrderListItem> filtered = new ArrayList<>();
        for (BSaasOrderListItem order : orders) {
            if ((order.getOrderTime() == null || order.getOrderTime().trim().isEmpty()) ||
                (order.getAmount() == null || order.getAmount() == 0) ||
                (order.getOrderDate() == null || order.getOrderDate().trim().isEmpty())) {
                continue;
            }
            filtered.add(order);
        }
        return filtered;
    }
    
    /**
     * 标准化中文年月
     */
    private String normalizeChineseYearMonth(String input, String fallback) {
        if (input == null || input.trim().isEmpty()) {
            return fallback;
        }
        
        String trimmed = input.trim();
        String normalized = trimmed.replace("年", "-").replace("月", "-");
        normalized = normalized.replaceAll("^-|-$", "");
        
        // 尝试解析多种格式
        String[] layouts = {"yyyy-MM", "yyyy-M", "yyyy/MM"};
        for (String layout : layouts) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(layout);
                LocalDateTime dateTime = LocalDateTime.parse(normalized + "-01", DateTimeFormatter.ofPattern(layout + "-dd"));
                return dateTime.format(YEAR_MONTH_FORMATTER);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        if (trimmed.contains("年") && trimmed.contains("月")) {
            return trimmed;
        }
        return fallback;
    }
    
    /**
     * 标准化中文月日
     */
    private String normalizeChineseMonthDay(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = input.trim();
        String normalized = trimmed.replace("日", "").replace("号", "").replace("月", "月");
        
        // 尝试解析多种格式
        String[] layouts = {"MM月dd", "MM月dd日", "M月d日", "MM-dd", "M-d"};
        for (String layout : layouts) {
            try {
                if (layout.contains("月")) {
                    // 处理中文格式
                    Matcher matcher = Pattern.compile("(\\d+)月(\\d+)").matcher(normalized);
                    if (matcher.find()) {
                        int month = Integer.parseInt(matcher.group(1));
                        int day = Integer.parseInt(matcher.group(2));
                        return String.format("%02d月%02d日", month, day);
                    }
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(layout);
                    LocalDateTime dateTime = LocalDateTime.parse("2024-" + normalized, DateTimeFormatter.ofPattern("yyyy-" + layout));
                    return dateTime.format(MONTH_DAY_FORMATTER);
                }
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        return normalized;
    }
    
    /**
     * 标准化时间
     */
    private String normalizeTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = value.trim();
        String normalized = trimmed.replace("点", ":").replace("时", ":").replace("分", "").replace("秒", "");
        normalized = normalized.replace("::", ":");
        
        // 尝试解析多种格式
        String[] layouts = {"HH:mm:ss", "HH:mm", "h:mm a"};
        for (String layout : layouts) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(layout);
                LocalDateTime dateTime = LocalDateTime.parse("2024-01-01 " + normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd " + layout));
                return dateTime.format(TIME_FORMATTER);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        return trimmed;
    }
    
    /**
     * 解析金额
     */
    private Double parseAmount(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 四舍五入价格
     */
    private Double roundPrice(Double val) {
        if (val == null) {
            return 0.0;
        }
        return Math.round(val * 100.0) / 100.0;
    }
    
    /**
     * 解析订单标签
     */
    private OrderTagMeta parseOrderTags(List<String> tags) {
        OrderTagMeta meta = new OrderTagMeta();
        if (tags == null || tags.isEmpty()) {
            return meta;
        }
        
        List<String> normalized = new ArrayList<>();
        for (String raw : tags) {
            if (raw == null) {
                continue;
            }
            String tag = raw.trim()
                .replace("\n", "")
                .replace("\t", "")
                .replace(" ", "")
                .replace("\u00a0", "")
                .replace("\u3000", "");
            
            if (tag.isEmpty()) {
                continue;
            }
            normalized.add(tag);
            
            // 提取元数据
            if (meta.orderType == null || meta.orderType.isEmpty()) {
                if (tag.contains("实时")) {
                    meta.orderType = "实时";
                } else if (tag.contains("预约")) {
                    meta.orderType = "预约";
                }
            }
            if ((meta.takeFeeType == null || meta.takeFeeType.isEmpty()) && tag.contains("佣")) {
                meta.takeFeeType = tag;
            }
            if ((meta.orderChannel == null || meta.orderChannel.isEmpty()) && 
                tag.contains("高德") && tag.contains("渠道")) {
                meta.orderChannel = "高德渠道";
            }
        }
        
        meta.tags = normalized;
        return meta;
    }
    
    /**
     * 订单标签元数据
     */
    private static class OrderTagMeta {
        List<String> tags = new ArrayList<>();
        String chargeType;
        String orderType;
        String takeFeeType;
        String orderChannel;
    }
    
    /**
     * 生成订单ID
     */
    private String generateOrderID(String yearMonth, String monthDay, String timeStr) {
        if (yearMonth == null || monthDay == null || timeStr == null) {
            return "";
        }
        
        List<String> yearTokens = extractDigits(yearMonth);
        List<String> monthDayTokens = extractDigits(monthDay);
        List<String> timeTokens = extractDigits(timeStr);
        
        if (yearTokens.isEmpty() || monthDayTokens.size() < 2 || timeTokens.size() < 2) {
            return "";
        }
        
        String yy = ensureTwoDigits(yearTokens.get(0));
        String mm = ensureTwoDigits(monthDayTokens.get(0));
        String dd = ensureTwoDigits(monthDayTokens.get(1));
        String hh = ensureTwoDigits(timeTokens.get(0));
        String min = ensureTwoDigits(timeTokens.get(1));
        
        if (yy.isEmpty() || mm.isEmpty() || dd.isEmpty() || hh.isEmpty() || min.isEmpty()) {
            return "";
        }
        
        return String.format("%s_%s%s_%s%s", yy, mm, dd, hh, min);
    }
    
    /**
     * 构建订单日期
     */
    private String buildOrderDate(String yearMonth, String monthDay) {
        if (yearMonth == null || monthDay == null) {
            return "";
        }
        
        List<String> yearTokens = extractDigits(yearMonth);
        List<String> monthDayTokens = extractDigits(monthDay);
        
        if (yearTokens.isEmpty() || monthDayTokens.size() < 2) {
            return "";
        }
        
        String year = yearTokens.get(0);
        String month = ensureTwoDigits(monthDayTokens.get(0));
        String day = ensureTwoDigits(monthDayTokens.get(1));
        
        if (year.isEmpty() || month.isEmpty() || day.isEmpty()) {
            return "";
        }
        
        return String.format("%s/%s/%s", year, month, day);
    }
    
    /**
     * 提取数字
     */
    private List<String> extractDigits(String input) {
        List<String> digits = new ArrayList<>();
        Matcher matcher = DIGIT_PATTERN.matcher(input);
        while (matcher.find()) {
            digits.add(matcher.group());
        }
        return digits;
    }
    
    /**
     * 确保两位数字
     */
    private String ensureTwoDigits(String val) {
        if (val == null || val.isEmpty()) {
            return "";
        }
        
        Matcher matcher = DIGIT_PATTERN.matcher(val);
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

