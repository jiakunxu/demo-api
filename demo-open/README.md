# demo-open：对外契约与组装产物

维护于 2026-10-20。事实来源为本模块接口、POM 和 assembly 描述。

## 职责与边界

承载面向外部调用的契约入口及其组装规则，直接依赖 demo-api。
仓库内直接消费者为 demo-manager、demo-service。
与 demo-api 的分工是对外发布边界；当前实际业务契约仍很少，不能仅凭模块名宣称 RPC 能力已完成。

## 当前内容

| 文件 | 当前作用 |
| --- | --- |
| [ICorpService](src/main/java/com/example/demo/corp/api/ICorpService.java) | 目前为空接口，没有可调用业务方法 |
| [pom.xml](pom.xml) | package 阶段执行 maven-assembly-plugin |
| [jar-with-dependencies.xml](src/main/resources/jar-with-dependencies.xml) | 选择依赖内容并解包组装 |

assembly 的筛选包括 corp/api/I*、corp/api/bo/*、framework/**、license/* 和 META-INF/**。
它并非把所有业务接口和所有依赖类完整打包；新接口即使编译成功，也可能没有进入组装产物。
该 JAR 未配置 Spring Boot 应用启动入口，不能当作 start 的可执行应用 JAR。

## 修改注意事项

- 外部方法明确参数、返回、异常、序列化及版本兼容，不能暴露实现模块类型。
- 修改 demo-api 后检查组装筛选是否包含新增契约与所需对象。
- 核对外部消费者需要的依赖，不从 JAR 文件名推断可独立运行。
- 发布前检查实际 JAR 内容、重复资源和契约引用；不能只验证 IDE 中的模块依赖。
- 当前空接口不是功能实现，不添加仅验证空返回值的测试来宣称可用。

## 验证

从仓库根目录执行：

```bash
./mvnw -pl demo-open -am -DskipTests package
./mvnw -DskipTests test-compile
```

第一条生成普通 JAR 和带 assembly 标识的组装 JAR，以 target 实际产物为准；
第二条检查仓库内消费者的源码兼容，二者均不执行测试。
当前本模块未发现独立测试，仓库外消费者兼容需要单独验证。

## 相关文档

[API 模块](../demo-api/README.md) · [架构](../docs/architecture.md) · [API 约定](../docs/api-conventions.md) · [依赖](../docs/dependencies.md)
