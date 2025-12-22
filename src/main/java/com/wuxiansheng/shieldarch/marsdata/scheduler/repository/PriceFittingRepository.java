package com.wuxiansheng.shieldarch.marsdata.scheduler.repository;

import com.wuxiansheng.shieldarch.marsdata.config.GlobalConfig;
import com.wuxiansheng.shieldarch.marsdata.io.MysqlWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 特价车价格拟合数据访问层
 */
@Slf4j
@Repository
public class PriceFittingRepository {

    private static final String ECONOMIC_CAR_TYPE = "经济型";
    private static final int FITTED_TYPE = 1; // 拟合结果类型
    private static final int BATCH_SIZE = 1000; // 批量查询大小

    private final JdbcTemplate jdbcTemplate;
    private final GlobalConfig globalConfig;

    @Autowired
    public PriceFittingRepository(MysqlWrapper mysqlWrapper, GlobalConfig globalConfig) {
        this.jdbcTemplate = new JdbcTemplate(mysqlWrapper.getDataSource());
        this.globalConfig = globalConfig;
    }

    /**
     * 获取未拟合的特价车数据
     */
    public List<MysqlRow> getUnfittedSpecialPrices(String fortyEightHoursAgo, List<String> openedCityNames) {
        if (openedCityNames == null || openedCityNames.isEmpty()) {
            throw new IllegalArgumentException("城市列表不能为空");
        }

        String tableName = getSpecialPriceTableName();

        // 1. 查询已拟合的 estimate_id 列表
        String fittedSql = "SELECT DISTINCT estimate_id FROM " + tableName +
                " WHERE update_time > ? AND type = ?";
        List<String> fittedEstimateIDs = jdbcTemplate.queryForList(
                fittedSql, String.class, fortyEightHoursAgo, FITTED_TYPE);
        Set<String> fittedEstimateIDsSet = new HashSet<>(fittedEstimateIDs);

        // 2. 查询所有符合条件的特价车数据
        String placeholders = openedCityNames.stream()
                .map(city -> "?")
                .collect(Collectors.joining(","));
        String allSql = "SELECT * FROM " + tableName +
                " WHERE update_time > ? AND city_name IN (" + placeholders + ")";

        List<Object> params = new ArrayList<>();
        params.add(fortyEightHoursAgo);
        params.addAll(openedCityNames);

        List<MysqlRow> allResults = jdbcTemplate.query(allSql, params.toArray(), new MysqlRowRowMapper());

        log.info("[PriceFittingRepository] GetUnfittedSpecialPrices SQL: SELECT * FROM {} WHERE update_time > {} AND city_name IN (...{}个...), rows={}",
                tableName, fortyEightHoursAgo, openedCityNames.size(), allResults.size());

        // 3. 在内存中排除已经拟合过的记录
        List<MysqlRow> results = allResults.stream()
                .filter(row -> !fittedEstimateIDsSet.contains(row.getEstimateId()))
                .collect(Collectors.toList());

        // 4. 去重（基于 estimate_id + partner_name）
        Map<String, MysqlRow> resultsMap = new HashMap<>();
        for (MysqlRow row : results) {
            String key = row.getEstimateId() + "_" + row.getPartnerName();
            resultsMap.putIfAbsent(key, row);
        }

        List<MysqlRow> finalResults = new ArrayList<>(resultsMap.values());

        log.info("[PriceFittingRepository] GetUnfittedSpecialPrices: 查询到 {} 条特价车数据，已拟合 {} 个 estimate_id，过滤后剩余 {} 条未拟合数据",
                allResults.size(), fittedEstimateIDsSet.size(), finalResults.size());

        return finalResults;
    }

    /**
     * 获取经济型数据（按 estimate_id 和已开城 city_name 过滤）
     */
    public List<EconomyBubble> getEconomyBubbles(List<String> estimateIDs, List<String> openedCityNames) {
        if (estimateIDs == null || estimateIDs.isEmpty()) {
            return Collections.emptyList();
        }
        if (openedCityNames == null || openedCityNames.isEmpty()) {
            throw new IllegalArgumentException("已开城列表不能为空");
        }

        // estimate_id 去重
        Set<String> uniqueIDs = new HashSet<>(estimateIDs);
        List<String> dedupIDs = new ArrayList<>(uniqueIDs);

        String tableName = getOCRBubbleTableName();
        List<EconomyBubble> allResults = new ArrayList<>();

        // 分批查询，避免 SQL 过长
        for (int i = 0; i < dedupIDs.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, dedupIDs.size());
            List<String> batchIDs = dedupIDs.subList(i, end);

            String placeholders = batchIDs.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(","));
            String cityPlaceholders = openedCityNames.stream()
                    .map(city -> "?")
                    .collect(Collectors.joining(","));

            String sql = "SELECT estimate_id, partner_name, est_pay, city_id, city_name FROM " + tableName +
                    " WHERE estimate_id IN (" + placeholders + ")" +
                    " AND car_type = ?" +
                    " AND city_name IN (" + cityPlaceholders + ")";

            List<Object> params = new ArrayList<>();
            params.addAll(batchIDs);
            params.add(ECONOMIC_CAR_TYPE);
            params.addAll(openedCityNames);

            List<EconomyBubble> batch = jdbcTemplate.query(sql, params.toArray(), new EconomyBubbleRowMapper());
            allResults.addAll(batch);
        }

        return allResults;
    }

    /**
     * 保存拟合结果
     */
    @Transactional
    public void saveFittedResults(List<MysqlRow> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        String tableName = getSpecialPriceTableName();

        // 打印样本数据
        List<String> samples = new ArrayList<>();
        for (int i = 0; i < Math.min(3, results.size()); i++) {
            MysqlRow res = results.get(i);
            if (res != null) {
                samples.add(String.format("estimate_id=%s, city_id=%d, cap_price=%.2f, partner_name=%s",
                        res.getEstimateId(), res.getCityId(), res.getCapPrice(), res.getPartnerName()));
            }
        }
        log.info("[PriceFittingRepository] SaveFittedResults SQL: INSERT INTO {} (...) VALUES (...), rows={}, sample_rows={}",
                tableName, results.size(), samples);

        // 使用 ON DUPLICATE KEY UPDATE 实现 upsert
        // 唯一键：estimate_id + partner_name
        String sql = "INSERT INTO " + tableName +
                " (estimate_id, bubble_image_url, partner_name, cap_price, reduce_price, car_type, city_id, city_name, create_time, update_time, type)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                " ON DUPLICATE KEY UPDATE" +
                " cap_price = VALUES(cap_price)," +
                " reduce_price = VALUES(reduce_price)," +
                " update_time = VALUES(update_time)," +
                " type = VALUES(type)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (MysqlRow row : results) {
            if (row == null) {
                continue;
            }
            Object[] args = new Object[]{
                    row.getEstimateId(),
                    row.getBubbleImageUrl(),
                    row.getPartnerName(),
                    row.getCapPrice(),
                    row.getReducePrice(),
                    row.getCarType(),
                    row.getCityId(),
                    row.getCityName(),
                    row.getCreateTime(),
                    row.getUpdateTime(),
                    row.getType()
            };
            batchArgs.add(args);
        }

        try {
            jdbcTemplate.batchUpdate(sql, batchArgs);
            log.info("[PriceFittingRepository] 成功保存 {} 条拟合结果", batchArgs.size());
        } catch (Exception e) {
            log.error("[PriceFittingRepository] 保存拟合结果失败", e);
            throw new RuntimeException("保存拟合结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据环境返回特价车表名
     */
    private String getSpecialPriceTableName() {
        if ("prod".equals(globalConfig.getEnv())) {
            return "bubble_gd_special_price";
        }
        return "bubble_gd_special_price_pre";
    }

    /**
     * 根据环境返回GD冒泡表名
     */
    private String getOCRBubbleTableName() {
        if ("prod".equals(globalConfig.getEnv())) {
            return "ocr_bubble_data";
        }
        return "ocr_bubble_data_v2_pre";
    }

    /**
     * MysqlRow 的 RowMapper
     */
    private static class MysqlRowRowMapper implements RowMapper<MysqlRow> {
        @Override
        public MysqlRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            MysqlRow row = new MysqlRow();
            row.setId(rs.getLong("id"));
            row.setEstimateId(rs.getString("estimate_id"));
            row.setBubbleImageUrl(rs.getString("bubble_image_url"));
            row.setPartnerName(rs.getString("partner_name"));
            row.setCapPrice(rs.getDouble("cap_price"));
            row.setReducePrice(rs.getDouble("reduce_price"));
            row.setCarType(rs.getString("car_type"));
            row.setCityId(rs.getInt("city_id"));
            row.setCityName(rs.getString("city_name"));
            row.setCreateTime(rs.getString("create_time"));
            row.setUpdateTime(rs.getString("update_time"));
            row.setType(rs.getInt("type"));
            return row;
        }
    }

    /**
     * EconomyBubble 的 RowMapper
     */
    private static class EconomyBubbleRowMapper implements RowMapper<EconomyBubble> {
        @Override
        public EconomyBubble mapRow(ResultSet rs, int rowNum) throws SQLException {
            EconomyBubble bubble = new EconomyBubble();
            bubble.setEstimateId(rs.getString("estimate_id"));
            bubble.setPartnerName(rs.getString("partner_name"));
            bubble.setEstPay(rs.getDouble("est_pay"));
            bubble.setCityId(rs.getInt("city_id"));
            bubble.setCityName(rs.getString("city_name"));
            return bubble;
        }
    }
}

