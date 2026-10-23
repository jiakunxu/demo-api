# demo-task：调度任务适配

维护于 2026-10-20。事实来源为本模块 POM、配置类与任务源码。

## 职责与边界

接入 XXL-JOB 执行器，将调度调用转交业务 Service。
直接依赖 demo-service，由 start 装配，不是独立启动应用。
业务状态转换与数据库操作应复用 Service，不在任务类复制 Controller 或 Mapper 逻辑。

## 当前入口与任务清单

| 入口 | 当前行为 |
| --- | --- |
| [XxlJobConfig](src/main/java/com/example/demo/framework/config/XxlJobConfig.java) | 创建并配置 XxlJobSpringExecutor |
| [TestTask](src/main/java/com/example/demo/test/task/TestTask.java) | 注册 testJobHandler，输出类名和当前时间 |

当前只发现上述示例任务，没有业务批处理或补偿任务清单。
不能把示例注册等同于调度平台已配置作业，或认定消息重试已有定时任务兜底。

## 配置与生命周期

执行器消费 xxl.job.admin.* 和 xxl.job.executor.* 参数，具体键及用途见配置文档。
XxlJobConfig 将 enabled、地址、端口、日志目录和保留天数等传给执行器。
本文不复制配置取值、不核查账号；完整启动可能初始化执行器并与调度平台交互。
实际调度、阻塞策略和失败重试需要结合平台配置，不从 Java 注解推断。

## 新增任务时说明什么

- handler 名称、用途、输入参数、调用 Service 和成功/失败判定。
- 单次执行的数据范围、批大小、时间边界与恢复位置。
- 并发、重复调度、重试和跨实例运行下的业务幂等约束。
- 数据库与缓存、消息、平台调用的事务边界及部分成功处理。
- 运行日志、脱敏字段、超时、取消和补偿方式。
- 平台侧需要的作业配置及发布顺序，区分建议与已经验证的事实。

## 验证

从仓库根目录执行：

```bash
./mvnw -pl demo-task -am -DskipTests test-compile
```

当前本模块未发现独立测试；编译不会证明平台注册、调度或失败重试可用。
新增任务可 mock Service 验证参数与控制流，不以访问真实平台作为单元测试前提。
实际调度测试前明确环境、数据范围和授权，避免误触发真实业务。

## 相关文档

[Service 模块](../demo-service/README.md) · [配置](../docs/configuration.md) · [安全](../docs/security.md) · [测试](../docs/testing.md) · [运行手册](../docs/runbook.md)
