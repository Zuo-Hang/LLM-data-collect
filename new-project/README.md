# New Project

基于 Spring Boot 3.2.0 的新项目。

## 项目结构

```
new-project/
├── pom.xml                                    # Maven 配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/wuxiansheng/shieldarch/newproject/
│   │   │       ├── NewProjectApplication.java    # 主启动类
│   │   │       └── controller/                   # 控制器
│   │   │           └── HealthController.java
│   │   └── resources/
│   │       └── application.yml                   # 配置文件
│   └── test/
│       └── java/
│           └── com/wuxiansheng/shieldarch/newproject/
└── README.md
```

## 技术栈

- **框架**: Spring Boot 3.2.0
- **语言**: Java 21
- **构建工具**: Maven
- **监控**: Prometheus + Actuator

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+

### 运行

```bash
# 编译打包
mvn clean package

# 运行
java -jar target/new-project-1.0.0-SNAPSHOT.jar

# 或者使用 Maven 直接运行
mvn spring-boot:run
```

### 验证

访问健康检查接口：
```bash
curl http://localhost:8080/api/health
```

访问 Actuator 健康检查：
```bash
curl http://localhost:8080/actuator/health
```

## 说明

- 此项目将复用当前项目（LLM-data-collect）中的部分代码
- 可以根据需要添加更多依赖和模块

## 待完成

- [ ] 确定需要复用的代码模块
- [ ] 添加业务功能模块
- [ ] 配置数据库连接（如需要）
- [ ] 配置 Redis 连接（如需要）
