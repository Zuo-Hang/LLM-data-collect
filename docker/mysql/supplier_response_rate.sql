-- 供应商应答概率表

USE mars_data;

-- 创建供应商应答概率表
CREATE TABLE IF NOT EXISTS `supplier_response_rate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `city_name` VARCHAR(100) NOT NULL COMMENT '城市名称',
    `partner_name` VARCHAR(200) NOT NULL COMMENT '供应商名称',
    `response_rate` DECIMAL(10, 6) NOT NULL DEFAULT 0.000000 COMMENT '应答概率（o_supplier_rate）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_partner` (`city_name`, `partner_name`),
    KEY `idx_city_name` (`city_name`),
    KEY `idx_partner_name` (`partner_name`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商应答概率表';

-- 插入示例数据（可选）
-- INSERT INTO `supplier_response_rate` (`city_name`, `partner_name`, `response_rate`) VALUES
-- ('北京市', '供应商A', 0.850000),
-- ('北京市', '供应商B', 0.920000),
-- ('上海市', '供应商A', 0.880000),
-- ('上海市', '供应商B', 0.900000);

