# demo-service：业务编排

维护于 2026-10-20。事实来源为本模块 POM、业务实现、调用方和测试。

## 职责与边界

实现业务接口，编排数据库、权限、事务、缓存和外部能力。
HTTP 适配在 Web，平台协议主要封装于 Manager，SQL 定义于 DAO。
直接依赖 demo-dao、demo-manager、demo-open、demo-util；直接消费者为 demo-web、demo-task。

## 业务分布与阅读入口

业务包位于 src/main/java/com/example/demo。

| 领域 | 关键入口 |
| --- | --- |
| 常规 CRUD | [BannerServiceImpl](src/main/java/com/example/demo/banner/service/BannerServiceImpl.java) |
| 字典、配置 | dict/service、config/service，结合缓存失效和查询条件阅读 |
| 用户和角色关系 | [UserRoleServiceImpl](src/main/java/com/example/demo/user/service/UserRoleServiceImpl.java) |
| 菜单权限关系 | [RoleMenuServiceImpl](src/main/java/com/example/demo/role/service/RoleMenuServiceImpl.java) |
| 聊天与会话 | [ChatServiceImpl](src/main/java/com/example/demo/chat/service/ChatServiceImpl.java)、[ChatDetailServiceImpl](src/main/java/com/example/demo/chat/service/ChatDetailServiceImpl.java) |
| 认证与权限 | security/service；登录业务另见 login/service |
| 消息分发 | mq/service、mqtt/service、subscribe/service |
| 自定义参数校验 | [ValidateAspect](src/main/java/com/example/demo/framework/aspectj/ValidateAspect.java) |

平台同名 Service 的 API/Manager 实现与本模块业务编排并存，核对全限定名称再修改。
类存在不代表功能完整，占位接口见工程现状。

## 修改注意事项

- 先核对 API、调用方、Mapper 和测试，保留输入输出、归属、软删除和异常契约。
- CRUD 参考 Banner 的局部模式；普通保存不包通用异常，插入按需回填 ID。
- 更新/删除使用可信 ID 和用户/企业范围，检查影响行数；批量查询避免循环内逐个查询。
- Chat.saveOrUpdate 先按业务键 updateChat，影响行数不为 1 时插入，不换成通用按主键保存。
- UserRole.listRoles 当前对角色 ID 去重后直接返回批量结果，不恢复关联顺序和重复项。
- 自定义 NotNull/NotBlank/NotEmpty 通过 AOP 执行，直接 new 和自调用不等于代理调用。
- 本地事务不能回滚 Redis/MQ；缓存删除和发送消息的实际时序见专项文档。

## 验证与测试入口

从仓库根目录执行：

```bash
./mvnw -pl demo-service -am test
./mvnw -DskipTests test-compile
```

第一条包含 Manager、Util 等上游测试，不包含 start；临时 agent 参数见测试指南。
[ServiceTestSupport](src/test/java/com/example/demo/support/ServiceTestSupport.java) 初始化 Mapper/TableInfo；
[TransactionProbe](src/test/java/com/example/demo/support/TransactionProbe.java) 验证 Spring 事务 advice，不能证明数据库回滚。
参考 [BannerServiceImplTest](src/test/java/com/example/demo/banner/service/BannerServiceImplTest.java) 和
[ValidateAspectTest](src/test/java/com/example/demo/role/service/ValidateAspectTest.java)。
Mapper mock 不证明真实 SQL 正确；当前没有完整隔离数据库集成测试设施。

## 相关文档

[持久化](../docs/persistence.md) · [Java 约定](../docs/java-conventions.md) · [缓存与消息](../docs/cache-and-messaging.md) · [测试](../docs/testing.md)
