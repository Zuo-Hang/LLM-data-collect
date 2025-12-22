# 项目问题报告

## 🔴 严重错误（编译失败）

### 1. 缺少类定义
- **ReasonContext**: 代码中使用了 `ReasonContext`，但应该使用 `ReasonRequest.ReasonContext`
- **BSaasPassengerDetailRaw**: 缺少 Raw 类
- **BSaasDriverDetailRaw**: 缺少 Raw 类  
- **BSaasHistoricalStatisticRaw**: 缺少 Raw 类
- **BSaasPerformanceTransactionRaw**: 缺少 Raw 类
- **BSaasPersonalHomepageRaw**: 缺少 Raw 类
- **XLBubbleReasonResult**: 找不到类型

### 2. 缺少方法/字段
- **BusinessContext**: 
  - 缺少 `getBusinessConf()` 方法（虽然有 @Data，但可能 Lombok 未生效）
  - 缺少 `getSourceConf()` 方法
  - 缺少 `setBusinessConf()` 方法
  - 缺少 `setSourceConf()` 方法
- **BusinessConfigService.BusinessSourceConf**: 缺少 `getLevel()` 方法
- **MysqlRow**: 缺少 `setCityID(Integer)` 方法
- **StatsdConfig**: 缺少 `isEnabled()` 方法（虽然有 @Data，但可能 Lombok 未生效）

### 3. 方法可见性问题
- **HiveSinker.printToHive()**: 方法为 `protected`，但子类调用时报错不可见
- **MonitorSinker**: `Sink()` 方法签名不匹配，应该是 `sink()`

### 4. 缺少 @Slf4j 注解
以下类缺少 `@Slf4j` 注解，导致 `log` 变量找不到：
- `MessageHandler`
- `StatsdClient` (已有，但可能 Lombok 未生效)
- `BusinessRegistry`
- `ReasonService`

### 5. 方法调用错误
- **BSaasBusiness.java:49**: `toEpochSecond()` 需要参数，应该使用 `toEpochSecond(ZoneOffset.UTC)`
- **MonitorSinker**: `incrementCounter()` 方法参数不匹配，应该使用 `Map<String, String>` 而不是多个 String 参数
- **PoiService**: `recordRpcMetric()` 最后一个参数应该是 `int` 而不是 `String`
- **QuestService**: 同上

### 6. 类型问题
- **S3Client**: `Item` 类型找不到（可能是 MinIO SDK 版本问题）

## ⚠️ 警告（不影响编译）

### 1. 未使用的导入
- 多个文件中有未使用的 import 语句

### 2. 未使用的变量/字段
- 多个类中有未使用的字段和局部变量

### 3. 废弃的方法
- `JdbcTemplate.query()` 使用了废弃的方法签名
- `RBucket.set()` 方法已废弃
- `URL(String)` 构造函数已废弃

### 4. 类型安全警告
- `QuestService.java:162`: 需要 unchecked conversion

## 📋 修复建议优先级

### 高优先级（必须修复才能编译）
1. 修复 `ReasonContext` 引用（使用 `ReasonRequest.ReasonContext`）
2. 创建缺失的 Raw 类
3. 为缺少 @Slf4j 的类添加注解
4. 修复方法签名不匹配问题
5. 修复 `toEpochSecond()` 调用
6. 修复 `incrementCounter()` 和 `recordRpcMetric()` 参数问题

### 中优先级（影响功能）
1. 修复 `HiveSinker.printToHive()` 可见性问题
2. 修复 `MysqlRow.setCityID()` 缺失问题
3. 修复 S3Client 的 Item 类型问题

### 低优先级（代码质量）
1. 清理未使用的导入
2. 清理未使用的变量
3. 替换废弃的方法调用

