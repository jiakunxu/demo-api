# demo-api：接口与业务对象

维护于 2026-10-20。事实来源为本模块 POM、接口及对象定义；本文是阅读入口。

## 职责与边界

向 Web、Service、Manager 和外部契约模块提供接口、BO、异常与公共常量。
不依赖仓库内其他模块，不引入 DAO、Service 或 Web 实现，避免循环依赖。
接口名包含 Service 不代表实现一定在 demo-service，平台和缓存接口的实现常在 Manager。

## 目录与关键入口

路径均相对于本模块，业务包位于 src/main/java/com/example/demo。

| 位置 | 用途 |
| --- | --- |
| `<业务>/api/*Service.java` | 业务或外部能力接口 |
| `<业务>/api/bo/` | 输入、输出及平台数据对象 |
| [BaseBO](src/main/java/com/example/demo/framework/bo/BaseBO.java) | 公共查询、分页、排序和时间属性 |
| [ServiceException](src/main/java/com/example/demo/framework/exception/ServiceException.java) | 业务异常及状态信息 |
| [RedisService](src/main/java/com/example/demo/cache/api/RedisService.java) | 缓存接口、键前缀和 TTL 常量 |
| [LoginUser](src/main/java/com/example/demo/security/api/bo/LoginUser.java) | 登录身份和安全框架契约 |

HTTP 响应包装位于 demo-web/framework/response，不在本模块。

## 依赖与消费者

仓库内直接消费者是 demo-open，其他实现模块通过依赖链取得 API。
当前还依赖 Fastjson2、Lombok、Spring Security 及支付宝/微信支付 SDK；因此不属于完全独立于框架的纯 DTO 包。
修改 SDK 类型、UserDetails 或序列化注解时，要核对所有实现和消费者。

## 修改注意事项

- 保留参数、返回类型、日期、null/空集合、异常和状态值的既有含义。
- count 使用 long；可选分页使用可空类型，不把未提供与 0 混为一谈。
- BO 与 DO 的字段相似不代表复制方式等价；审计和归属必须来自可信上下文。
- 添加方法时同步实现、调用方和测试；不以占位返回值宣称接口可用。
- 公共字段重命名需评估 JSON、客户端、缓存和消息的兼容性。

## 验证

从仓库根目录执行：

```bash
./mvnw -DskipTests test-compile
```

这是全模块主代码和测试代码编译，不是测试通过证据。
当前本模块未发现独立测试；行为由消费者测试部分验证，公共契约变化仍需选择相关回归。

## 相关文档

[架构](../docs/architecture.md) · [Java 约定](../docs/java-conventions.md) · [API 约定](../docs/api-conventions.md) · [测试](../docs/testing.md)
