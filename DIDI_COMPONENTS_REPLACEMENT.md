# 滴滴内部组件替换清单

## 📋 当前状态

### ✅ 已替换

| 组件 | 原实现 | 替换方案 | 状态 |
|------|--------|---------|------|
| **DiSF** | 滴滴服务发现 | **Nacos** | ✅ 已完成 |
| - | DiSFUtils | NacosServiceDiscovery | ✅ 已完成 |
| **Dufe** | 特征服务 | **MySQL 表（supplier_response_rate）** | ✅ 已完成 |
| - | DufeClient | SupplierResponseRateService | ✅ 已完成 |
| **DirPC** | RPC 框架 | **已删除（未使用）** | - | ✅ 已完成 |
| - | DirPCInitializer | 已删除 | ✅ 已完成 |

### ⚠️ 待替换

| 组件 | 用途 | 当前状态 | 推荐替换方案 | 优先级 |
|------|------|---------|------------|--------|
| **Apollo** | 配置中心 | 使用中 | **Nacos 配置中心** | 🔴 高 |
| **Odin** | 监控平台 | 已删除 | **Prometheus + Grafana** + **Spring Boot Actuator** | ✅ 已完成 |

### 📝 遗留代码（可清理）

| 组件 | 文件 | 说明 |
|------|------|------|
| **DiSFInitializer** | `config/DiSFInitializer.java` | 已替换为 Nacos，可删除或标记废弃 |

---

## 🔴 高优先级：Apollo 配置中心

### 当前使用情况

- **使用位置**: 19 个文件
- **主要功能**:
  - LLM 配置管理（`OCR_LLM_CONF`）
  - 价格拟合配置（`PRICE_FITTING_CONF`）
  - 业务配置（`OCR_BUSINESS_CONF`）
  - Prompt 管理
  - 供应商验证配置

### 替换方案：Nacos 配置中心

**优势**：
- ✅ 已使用 Nacos 做服务发现，可以复用
- ✅ 支持配置管理功能
- ✅ 支持配置热更新
- ✅ 支持多环境配置

**实现步骤**：
1. 使用 Nacos 配置管理 API
2. 创建 `NacosConfigService` 替换 `ApolloConfigService`
3. 迁移配置数据到 Nacos
4. 更新所有调用 `ApolloConfigService` 的地方

---

## ✅ 已完成：DirPC

### 删除状态

- **原文件**: `config/DirPCInitializer.java` ✅ 已删除
- **状态**: 已确认未实际使用，已删除
- **原因**: 只是占位实现，没有实际的 RPC 调用，无其他代码依赖

---

## ✅ 已完成：Dufe 特征服务

### 替换状态

- **原文件**: `io/DufeClient.java` ✅ 已删除
- **新实现**: `io/SupplierResponseRateService.java`
- **状态**: 已完全替换为 MySQL 数据库查询
- **数据表**: `supplier_response_rate`

### 替换方案（已实施）

**✅ MySQL 数据库存储**
- 使用 MySQL 表存储供应商应答概率
- 通过 MyBatis Plus 进行数据访问
- 表结构：`supplier_response_rate`（city_name, partner_name, response_rate）
- 支持逻辑删除和自动时间戳

**实现细节**:
- `SupplierResponseRateService`: 服务层，提供 `getResponseRate()` 方法
- `SupplierResponseRateMapper`: 数据访问层，查询 MySQL
- `SupplierResponseRate`: 实体类，映射数据库表

---

## ✅ 已完成：Odin 监控

### 替换状态

- **原文件**: `monitor/OdinMonitor.java` ✅ 已删除
- **状态**: 已完全替换为 Prometheus + Spring Boot Actuator
- **实现**: 使用 `MetricsClientAdapter` + `PrometheusMetricsClient`

### 替换方案（已实施）

**✅ Spring Boot Actuator + Prometheus**
- Spring Boot 原生支持
- 标准化的监控方案
- 已配置 `/actuator/prometheus` 端点
- 已集成 Grafana 仪表盘
- 已配置 Prometheus 告警规则

**监控架构**:
```
应用代码 → MetricsClientAdapter → PrometheusMetricsClient → Micrometer → /actuator/prometheus → Prometheus → Grafana
```

---

## 📊 替换优先级建议

### 第一阶段（高优先级）

1. **Apollo → Nacos 配置中心**
   - 影响范围大（19 个文件）
   - 配置管理是核心功能
   - 可以复用现有的 Nacos 基础设施

### 第二阶段（中优先级）

2. ~~**DirPC 清理或替换**~~ ✅ 已完成
   - 已确认未使用，已删除

3. ~~**Dufe 特征服务**~~ ✅ 已完成
   - 已替换为 MySQL 数据库查询
   - 或使用 Feature Store

### 第三阶段（已完成）

4. **Odin 监控** ✅
   - 已删除 `OdinMonitor.java`
   - 使用 Prometheus + Spring Boot Actuator
   - 使用 `MetricsClientAdapter` + `PrometheusMetricsClient`

---

## 🔧 替换影响分析

### Apollo 替换影响

**影响文件**: 19 个文件
- `config/ApolloConfigService.java` - 核心配置服务
- `business/*` - 多个业务模块
- `llm/*` - LLM 相关配置
- `config/*` - 配置服务

**工作量**: 中等
- 需要创建 Nacos 配置服务适配层
- 需要迁移配置数据
- 需要更新所有调用点

### Dufe 替换影响

**影响文件**: 1 个文件
- `io/DufeClient.java`

**工作量**: 取决于业务需求
- 需要了解特征服务的实际需求
- 需要实现特征获取逻辑

### Odin 替换影响（已完成）

**影响文件**: 1 个文件（已删除）
- `monitor/OdinMonitor.java` ✅ 已删除

**工作量**: 已完成
- 已使用 Prometheus + Spring Boot Actuator
- 已使用 `MetricsClientAdapter` + `PrometheusMetricsClient`
- 所有监控代码已迁移到 Prometheus

---

## 📝 建议行动

1. **立即行动**: 
   - 删除或标记废弃 `DiSFInitializer`（已替换为 Nacos）

2. **短期计划**:
   - 替换 Apollo 为 Nacos 配置中心
   - 评估 DirPC 是否使用，未使用则删除

3. **中期计划**:
   - 实现 Dufe 特征服务（根据业务需求）
   - 完善监控方案（使用 Actuator）

4. **长期计划**:
   - 完全移除所有滴滴内部组件依赖
   - 使用标准化的开源组件

---

## 🔗 相关文档

- [Nacos 服务发现文档](src/main/java/com/wuxiansheng/shieldarch/marsdata/utils/README_NACOS_SERVICE_DISCOVERY.md)
- [Docker 部署文档](docker/README.md)

