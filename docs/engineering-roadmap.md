# 工程现状与演进建议

维护于 2026-10-13。以下分为已核查现状和建议，相关任务开始前重新确认源码。本页不要求每次修改同时修复所有问题。

## 已定位的功能缺口

| 位置 | 现状与影响 | 验收方向 |
| --- | --- | --- |
| [RedisServiceImpl](../demo-manager/src/main/java/com/example/demo/cache/manager/RedisServiceImpl.java) | 三个 replace 重载返回 null，尚不能视为完整替换功能 | 明确不存在键、TTL 和原值返回语义，补充测试 |
| [UserDetailsServiceImpl](../demo-service/src/main/java/com/example/demo/security/service/UserDetailsServiceImpl.java) | loadUserById 返回 null | 明确用户缺失和状态契约，实现并验证认证消费者 |
| [BannerMapper](../demo-dao/src/main/java/com/example/demo/banner/dao/mapper/BannerMapper.java)、[ArticleMapper](../demo-dao/src/main/java/com/example/demo/article/dao/mapper/ArticleMapper.java) | 自定义 count/list 未发现对应 XML 或方法注解 | 核对注入器及绑定，用真实映射测试验证 |
| [日志 SQL](../demo-dao/src/main/resources/mappers/OperateLogMapper.xml)及 [登录日志 SQL](../demo-dao/src/main/resources/mappers/LoginLogMapper.xml) | 使用动态 sort/dir 拼接，非空条件无法防注入 | 追踪输入，建立允许列名/方向映射和回归测试 |
| [ChatDetailServiceImpl](../demo-service/src/main/java/com/example/demo/chat/service/ChatDetailServiceImpl.java) | 事务中发送消息，数据库和消息不是同一回滚边界 | 验证发送失败、回滚、重复和提交时序 |

2026-10-18 补充核对：

| 位置 | 现状与影响 | 验收方向 |
| --- | --- | --- |
| [AlipayServiceImpl](../demo-service/src/main/java/com/example/demo/alipay/service/AlipayServiceImpl.java) | query、close、refund、queryRefund 返回 null，pay 的测试不能证明这些功能可用 | 明确查询、关闭、退款契约后实现并隔离平台测试 |
| [DingtalkNotifyServiceImpl](../demo-service/src/main/java/com/example/demo/dingtalk/service/DingtalkNotifyServiceImpl.java) | notify 返回空 Map | 明确验签、解密、事件处理和响应契约 |
| [BytedanceNotifyServiceImpl](../demo-service/src/main/java/com/example/demo/bytedance/service/BytedanceNotifyServiceImpl.java) | verify 返回空字符串，notify 返回 null | 明确验证与通知处理契约后实现并测试 |
| [DingCallbackCrypto](../demo-manager/src/main/java/com/example/demo/dingtalk/manager/DingCallbackCrypto.java) | 静态初始化修改 JVM 全局加密策略，与初始化不改策略的测试契约冲突 | 评估旧 JCE 反射处理在 JDK 25 的必要性；修复后验证固定加解密向量及策略保持 |

上述检查不代表全仓库安全审计；其他未实现接口应在涉及它们的任务中进一步确认。

测试范围及空白见 [测试指南](testing.md#当前测试范围与已知空白)：SqlUtil、Web 参数与认证入口缺少专项测试，验证码运算缺少确定性分支验证。CorpRoleServiceImpl 没有自定义业务逻辑，不为增加测试数量重复验证框架继承方法。

## 当前基础设施边界

已有 Util、Manager、Service 单元测试和 Spring 全上下文测试。Mapper mock 无法验证 SQL，事务探针不模拟真实数据库回滚。当前没有完整隔离 MySQL 测试设施、版本化 schema/迁移流程、独立本地依赖编排或测试 profile。

现有 CI 配置为 [CodeQL](../.github/workflows/codeql-analysis.yml) 与 [Dependency Review](../.github/workflows/dependency-review.yml)，没有专门的单元测试任务或覆盖率门槛。配置存在不证明远程执行成功；其中 action 版本及 Java 25 构建方式需另行核查。

## 建议优先顺序（尚未实施）

1. 修复本次业务实际使用的占位接口和 Mapper 映射，先定义验收契约。
2. 建立不依赖真实平台的单元测试 CI，记录 JDK、agent 参数及报告；成功后配置实际分支检查。
3. 建立隔离 MySQL 集成测试和迁移基线，验证默认值、软删除、约束、并发和回滚。
4. 明确本地配置及依赖启动方案，减少全应用测试对未知外部条件的依赖。
5. 按实际风险完善排序白名单、数据归属和消息一致性，长期选择记录 ADR。
6. 基于数据决定覆盖率和静态检查门槛，避免为数字而写镜像断言或强制全库格式化。

每项建设需要具体需求、影响分析、验证和维护安排；不因写入本页而视为已落实，也不是 OpenAI 对 Java 项目的强制选型。

## 原项目指令中的边界约定

规划功能、涉及外部集成/SQL/安全、补充测试或评估工程能力时，读取 [工程现状与演进建议](engineering-roadmap.md)，按相关条目核查代码。该文档包含已定位的占位实现、SQL 绑定缺口、启动依赖、现有 CI/文档限制，以及尚未落地的建设建议。

优先牢记：

- 部分 Redis/登录/支付/通知接口仍未实现，Banner/Article 的自定义 count/list 未发现对应 SQL 映射。类存在、编译通过及 mock 测试通过不能证明功能可用。
- 自定义排序、角色归属校验和事务内 MQ 发送存在待完善点；相关修改需验证安全与一致性。
- 尚无完整的 schema/迁移体系、独立集成测试环境、专门单元测试 CI 或覆盖率门槛。不得将未来建议描述成项目现状。

这些缺口不要求在每个任务中一并修复；按当前需求实施，发现涉及本次变更的阻塞问题则明确报告。后续建议属于本项目工程判断，不是 OpenAI 对 Java 项目的强制技术选型。
