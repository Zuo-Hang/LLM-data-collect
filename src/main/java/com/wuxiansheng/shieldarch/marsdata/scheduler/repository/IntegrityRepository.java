package com.wuxiansheng.shieldarch.marsdata.scheduler.repository;

import com.wuxiansheng.shieldarch.marsdata.io.MysqlWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 完整性校验数据访问层
 */
@Slf4j
@Repository
public class IntegrityRepository {

    private final JdbcTemplate jdbcTemplate;

    public IntegrityRepository(MysqlWrapper mysqlWrapper) {
        DataSource dataSource = mysqlWrapper.getDataSource();
        if (dataSource == null) {
            log.error("[IntegrityRepository] MySQL 数据源未配置，查询功能不可用");
            this.jdbcTemplate = null;
        } else {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }
    }

    /**
     * 查询指定时间范围和城市ID的GD冒泡数据，按城市、里程段分组统计 estimate_id 数量
     *
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（不含）
     * @param cityIds   城市 ID 列表
     */
    public List<IntegrityCheckGroupResult> queryDataToCheck(String startTime,
                                                            String endTime,
                                                            List<Integer> cityIds) {
        if (jdbcTemplate == null) {
            log.warn("[IntegrityRepository] jdbcTemplate 未初始化，直接返回空结果");
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder(
                "SELECT city_name, city_id, dis_range, COUNT(DISTINCT estimate_id) AS count " +
                        "FROM ocr_bubble_data " +
                        "WHERE create_time >= ? " +
                        "AND create_time < ? " +
                        "AND car_type = ? "
        );
        List<Object> args = new ArrayList<>();
        args.add(startTime);
        args.add(endTime);
        args.add("经济型");

        if (cityIds != null && !cityIds.isEmpty()) {
            sql.append("AND city_id IN (");
            for (int i = 0; i < cityIds.size(); i++) {
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("?");
                args.add(cityIds.get(i));
            }
            sql.append(") ");
        }

        sql.append("GROUP BY city_id, city_name, dis_range");

        try {
            List<IntegrityCheckGroupResult> results = jdbcTemplate.query(
                    sql.toString(),
                    args.toArray(),
                    new IntegrityGroupResultRowMapper()
            );
            log.info("[IntegrityRepository] QueryDataToCheck 查询成功: resultCount={}, startTime={}, endTime={}, cityIds={}",
                    results.size(), startTime, endTime, cityIds);
            return results;
        } catch (Exception e) {
            log.info("[IntegrityRepository] QueryDataToCheck 查询失败: err={}, startTime={}, endTime={}, cityIds={}",
                    e.getMessage(), startTime, endTime, cityIds);
            return Collections.emptyList();
        }
    }

    private static class IntegrityGroupResultRowMapper implements RowMapper<IntegrityCheckGroupResult> {
        @Override
        public IntegrityCheckGroupResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            IntegrityCheckGroupResult result = new IntegrityCheckGroupResult();
            result.setCityName(rs.getString("city_name"));
            result.setCityId(rs.getInt("city_id"));
            result.setDisRange(rs.getString("dis_range"));
            result.setCount(rs.getInt("count"));
            return result;
        }
    }
}


