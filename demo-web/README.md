# demo-web：HTTP 与连接入口

维护于 2026-10-20。事实来源为本模块 POM、Controller、过滤器和配置类。

## 职责与边界

适配 HTTP、请求绑定、响应包装、认证和方法权限，配置 WebSocket 及平台相关 Bean。
业务编排交给 Service；Controller 不能仅凭客户端传入的用户或企业 ID 认定权限。
直接依赖 demo-service，由 start 装配运行，本模块不是独立应用入口。

## 目录与关键入口

路径位于 src/main/java/com/example/demo。

| 位置 | 作用 |
| --- | --- |
| [BannerController](src/main/java/com/example/demo/banner/web/BannerController.java) | 常规列表、查询和 CRUD 入口参考 |
| [BaseController](src/main/java/com/example/demo/framework/web/BaseController.java) | 参数解析、身份、分页和排序处理 |
| [WebSecurityConfig](src/main/java/com/example/demo/framework/config/WebSecurityConfig.java) | 认证、公开入口与安全装配 |
| [TokenAuthenticationFilter](src/main/java/com/example/demo/framework/filter/TokenAuthenticationFilter.java) | 从 token 加载用户、设置上下文和续期 |
| [JsonConfigurer](src/main/java/com/example/demo/framework/config/JsonConfigurer.java) | JSON 转换行为 |
| [BaseExceptionHandler](src/main/java/com/example/demo/framework/handler/BaseExceptionHandler.java) | 异常到响应的转换 |
| [response](src/main/java/com/example/demo/framework/response) | ObjectResponse、ListResponse 等响应结构 |
| [RedisConfigurer](src/main/java/com/example/demo/framework/config/RedisConfigurer.java) | Redis 序列化配置 |
| [WebSocketHandler](src/main/java/com/example/demo/framework/handler/WebSocketHandler.java) | 连接事件与心跳 |

支付/平台配置分布于 framework/config，敏感取值不在 README 重复维护。

## 新增或修改入口

- 核对现有路径、HTTP 方法、响应包装、日期和错误码，避免无关协议变化。
- 同步 API/Service、BO、参数解析、方法权限及异常处理；涉及配置时登记消费者。
- 方法权限不替代 Service/SQL 的用户、企业和数据归属条件。
- BaseController 的表单绑定与 JSON 解析是不同路径，不能假定过滤规则完全相同。
- 当前表单排序由 SqlUtil 检查字符及长度，未知 dir 清空排序；仍不是业务字段白名单。
- 回调保留原始体、签名和身份语义；不为调试扩大公开路径或关闭验签。
- 新增连接功能同时核对连接归属、断连清理和发送失败。

## 验证

从仓库根目录执行：

```bash
./mvnw -pl demo-web -am -DskipTests test-compile
./mvnw -pl demo-service -am test
```

第一条检查本模块及上游编译，第二条验证业务和上游单元测试，均不启动完整应用。
当前本模块未发现独立测试；Service 测试通过不证明 HTTP 绑定、过滤器链或安全装配正确。
需要验证这些行为时使用隔离的 Web 测试；真实请求验证应明确环境和外部副作用。

## 相关文档

[API 约定](../docs/api-conventions.md) · [安全](../docs/security.md) · [配置](../docs/configuration.md) · [测试](../docs/testing.md)
