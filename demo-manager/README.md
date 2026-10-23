# demo-manager：外部能力与通用处理

维护于 2026-10-20。事实来源为本模块 POM、平台封装、配置 Bean 和测试。

## 职责与边界

封装平台 HTTP/SDK、Redis、MQ/MQTT、文件、WebSocket/SSE 等能力，适配返回值和异常。
可按现有结构访问 DAO；跨业务流程、权限决策和数据库事务编排主要放在 Service。
直接依赖 demo-dao、demo-open、demo-util；仓库内直接消费者为 demo-service。

## 目录与关键入口

业务包位于 src/main/java/com/example/demo。

| 包或入口 | 作用 |
| --- | --- |
| [RedisServiceImpl](src/main/java/com/example/demo/cache/manager/RedisServiceImpl.java) | 缓存操作、秒单位 TTL 与异常转换 |
| [MQ ProducerServiceImpl](src/main/java/com/example/demo/mq/manager/ProducerServiceImpl.java) | RocketMQ 普通、同步及延迟发送 |
| [MQTT ProducerBean](src/main/java/com/example/demo/mqtt/manager/ProducerBean.java) | MQTT 生产端生命周期 |
| `socket/manager`、`sse/manager` | 本节点连接管理和发送 |
| `alipay/manager`、`wxpay/manager` | 支付 SDK 封装 |
| `weixin/manager`、`dingtalk/manager`、`bytedance/manager` | 平台 token、消息及协议能力 |
| `aliyun/manager`、`huawei/manager`、`google/manager`、`getui/manager`、`openai/manager` | 云平台、地图、推送和模型调用 |
| [RedisServiceImplTest](src/test/java/com/example/demo/cache/manager/RedisServiceImplTest.java) | 外部依赖 mock 和参数捕获参考 |

接口通常位于 demo-api。TokenServiceImpl、MessageServiceImpl、OssServiceImpl 等类名跨包重复，
注入时检查接口包名、显式 Bean 名和 Qualifier，尤其区分 MQ 与 MQTT。

## 修改注意事项

- 核对平台响应、空体、错误码和异常传播；超时及重试以实际 SDK 配置和代码为准。
- 支付、回调和消息发送不能无条件重试；验签和身份校验不得为了测试关闭。
- 保留输入流、文件与客户端资源所有权，及时关闭本方法创建的资源。
- 缓存键、TTL、序列化和消息结构变化需要同步 Service、消费者和旧数据兼容。
- Redis replace 等占位能力以及 DingCallbackCrypto 的全局策略修改问题见工程现状。
- 不记录平台密钥、完整缓存键、用户或支付敏感数据。

## 验证

从仓库根目录执行：

```bash
./mvnw -pl demo-manager -am test
./mvnw -DskipTests test-compile
```

测试位于 src/test/java，使用 mock HTTP、SDK、Redis、MQ；禁止访问真实平台。
Mockito agent/headless 临时参数按测试指南传入，不写死在 POM。
单元测试不证明真实平台协议、Broker 投递或 SDK 初始化兼容；完整应用可能初始化连接。

## 相关文档

[安全](../docs/security.md) · [缓存与消息](../docs/cache-and-messaging.md) · [测试](../docs/testing.md) · [工程缺口](../docs/engineering-roadmap.md)
