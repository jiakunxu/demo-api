# start：应用装配与启动

维护于 2026-10-20。事实来源为本模块 POM、Application 和资源布局。

## 职责与依赖

通过 demo-web、demo-task 装配应用及其上游业务模块，添加 MySQL 驱动和日志相关依赖。
使用 spring-boot-maven-plugin 的 repackage 生成可执行 JAR。
业务代码按职责进入 API、DAO、Service、Manager、Web 或 Task，不集中在启动模块。
本模块是应用部署入口，与 demo-open 的契约组装 JAR 不同。

## 关键入口

| 文件或目录 | 用途 |
| --- | --- |
| [Application](src/main/java/com/example/demo/Application.java) | Spring Boot 启动、EnableAsync、EnableDubbo、MapperScan |
| [application.yml](src/main/resources/application.yml) | 应用配置资源；具体键和消费者见配置文档 |
| [logback-spring.xml](src/main/resources/logback-spring.xml) | 日志装配 |
| [ApplicationTests](src/test/java/com/example/demo/ApplicationTests.java) | SpringBootTest 完整上下文加载 |
| [pom.xml](pom.xml) | 直接依赖和 Boot 重打包规则 |

MapperScan 指向 com.example.demo.*.dao.mapper；变更包路径需同步扫描和资源配置。
新增 Bean 还要核对扫描范围、同名实现及相关平台初始化。

## 构建与启动

从仓库根目录执行，JDK/Maven 要求以父 POM、Wrapper 和开发环境文档为准：

```bash
./mvnw -pl start -am -DskipTests package
java -jar start/target/start-1.0.jar
```

先核对 target 中实际版本和产物名称，版本变化后同步命令。
打包命令跳过测试，不能作为测试通过证明。启动前明确运行环境和外部副作用；
MySQL、Redis、MQ/MQTT、Dubbo/注册配置中心、XXL-JOB 及平台 Bean 的初始化由实际装配决定。
默认 YAML 配置完整有效，不核查或复制真实取值；配置用途统一在配置文档维护。

## 编译与测试边界

```bash
# 全部主代码和测试代码编译，不启动上下文
./mvnw -DskipTests test-compile

# 纯单元测试入口，不包含 start
./mvnw -pl demo-service -am test

# 全应用上下文及上游测试：仅在明确需要且具备适当环境时执行
./mvnw -pl start -am test
```

以上均从仓库根目录运行；Mockito/headless 临时参数见测试指南。
ApplicationTests 可能初始化外部连接和执行器，必须与纯单元测试分别报告。
当前没有完整隔离测试 profile 或本地依赖编排；端口监听成功不代表业务、SQL 和平台集成均正确。
发布前还需核对数据库/协议兼容、观察信号及回退条件，具体步骤以运行手册为入口。

## 相关文档

[开发环境](../docs/development.md) · [配置](../docs/configuration.md) · [测试](../docs/testing.md) · [运行手册](../docs/runbook.md) · [架构](../docs/architecture.md)
