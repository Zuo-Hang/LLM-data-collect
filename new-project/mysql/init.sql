-- AI Agent Orchestrator 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS ai_orchestrator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_orchestrator;

-- 任务表
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) UNIQUE NOT NULL COMMENT '任务ID',
    task_type VARCHAR(32) NOT NULL COMMENT '任务类型',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
    input_data TEXT COMMENT '输入数据',
    output_data TEXT COMMENT '输出数据',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 步骤执行记录表
CREATE TABLE IF NOT EXISTS step_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    step_name VARCHAR(64) NOT NULL COMMENT '步骤名称',
    step_type VARCHAR(32) NOT NULL COMMENT '步骤类型',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态',
    input_data TEXT COMMENT '输入数据',
    output_data TEXT COMMENT '输出数据',
    error_message TEXT COMMENT '错误信息',
    execution_time_ms INT COMMENT '执行耗时(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_step_name (step_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='步骤执行记录表';

-- 质量检查记录表
CREATE TABLE IF NOT EXISTS quality_checks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    check_type VARCHAR(32) NOT NULL COMMENT '检查类型',
    check_result VARCHAR(16) NOT NULL COMMENT '检查结果',
    check_details TEXT COMMENT '检查详情',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_check_type (check_type),
    INDEX idx_check_result (check_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检查记录表';
