# demo-util：通用工具

维护于 2026-10-20。事实来源为本模块 POM、工具源码及测试。

## 职责与边界

提供通用转换、日期、计算、HTTP、文档处理和事务回调工具。
不直接依赖其他仓库模块；直接消费者为 demo-manager、demo-service。
虽然名为 Util，仍依赖 Spring、Fastjson2、Apache POI、HttpClient 等库，不是纯 JDK 模块。

## 目录与关键入口

主要包为 src/main/java/com/example/demo/framework/util。

| 工具 | 阅读重点 |
| --- | --- |
| [BeanUtil](src/main/java/com/example/demo/framework/util/BeanUtil.java) | 两种复制方式的语义差异 |
| [TransactionUtil](src/main/java/com/example/demo/framework/util/TransactionUtil.java) | 活动事务中的提交后同步回调 |
| [HttpUtil](src/main/java/com/example/demo/framework/util/HttpUtil.java) | HTTP 请求、响应及资源处理 |
| [DateUtil](src/main/java/com/example/demo/framework/util/DateUtil.java) | 日期格式与时间边界 |
| [ExcelUtil](src/main/java/com/example/demo/framework/util/ExcelUtil.java)、[WordUtil](src/main/java/com/example/demo/framework/util/WordUtil.java) | 文档转换、模板和资源关闭 |
| [SqlUtil](src/main/java/com/example/demo/framework/util/SqlUtil.java) | 排序字符和长度检查；不等于业务字段白名单 |
| [EncryptUtil](src/main/java/com/example/demo/framework/util/EncryptUtil.java) | 算法及编码契约，不能按方法名猜测算法 |

另有 com/google/code/kaptcha 和 com/qq/weixin/mp/aes 下的第三方来源代码。
修改这些代码应单独核对协议与来源，不与普通工具类一起批量重写。

## 使用注意事项

- BeanUtil.copy(source, Class) 经 JSON 转换；Supplier 重载使用不启用 converter 的 BeanCopier。
- 两种方式在字段类型、嵌套对象、JSON 注解和引用关系上不同，不能直接互换。
- TransactionUtil.afterCommit 要求活动事务和事务同步，不创建事务、不切换线程。
- 回调在提交后执行，失败不能撤销已提交数据；该工具不提供持久化或自动重试。
- 公共工具修改需要搜索全部消费者，核对空值、编码、资源所有权和线程安全。
- 新工具应有实际复用需求，不将业务规则搬入 framework。

## 验证

从仓库根目录执行：

```bash
./mvnw -pl demo-util -am test
./mvnw -DskipTests test-compile
```

测试位于 [framework.util 测试包](src/test/java/com/example/demo/framework/util)。
当前新增测试范围限于该包；SqlUtil 尚无对应测试，不据其他工具测试通过推断其已覆盖。
POI 测试在部分环境需要临时 headless 参数，参见测试指南；公共行为改变还需消费者回归。

## 相关文档

[Java 约定](../docs/java-conventions.md) · [持久化](../docs/persistence.md) · [测试](../docs/testing.md) · [依赖](../docs/dependencies.md)
