# 测试与验证指南

状态：已核查测试设施与项目规范。维护于 2026-10-13。运行命令均从仓库根目录执行，选择范围以本次改动为准。本项目协作默认 YAML 配置项完整、地址和账号有效；测试不以核查 YAML 取值为前置步骤。

自动检查的触发、门禁、报告保留及权限见 [持续集成指南](ci.md)。已有 CodeQL 与 Dependency Review 工作流；专门单元测试任务和覆盖率门槛尚未建设，工作流存在不代表远程执行成功。

## 现有设施和边界

| 层次 | 现有方式 | 可证明与不可证明 |
| --- | --- | --- |
| Util | JUnit/AssertJ，工具包测试 | 空值、转换、资源与边界；不代表平台兼容 |
| Manager | Mockito mock HTTP、SDK、Redis、MQ | 请求、解析、错误转换、TTL；不访问真实平台 |
| Service | mock Mapper/Manager，必要时 spy 持久化 | 业务范围、审计、状态、副作用；不证明 SQL |
| 自定义校验 | AspectJProxyFactory | 注解执行与目标是否被调用；不等于直接 new |
| 事务 | TransactionProbe + Spring advice | 提交/回滚分支；不模拟实际持久化 |
| Start | ApplicationTests @SpringBootTest | 全应用装配；可能初始化外部组件 |
| 数据库集成 | 尚无完整隔离测试体系 | SQL、DDL、约束、默认值、并发仍需专门验证 |

[ServiceTestSupport](../demo-service/src/test/java/com/example/demo/support/ServiceTestSupport.java) 设置 baseMapper 并初始化 MyBatis TableInfo，它创建的配置不是应用 YAML 自动装配的替代品。[TransactionProbe](../demo-service/src/test/java/com/example/demo/support/TransactionProbe.java) 用真实 Spring advice 统计提交/回滚，不是数据库事务实现。

## 当前测试范围与已知空白

以下为 2026-10-18 的源码与测试布局核对结果，不是覆盖率报告，也不表示本次执行了测试。通过数量和失败详情以每次实际运行报告为准。

| 范围 | 当前状态与限制 |
| --- | --- |
| demo-util | framework.util 包已有多项工具测试；SqlUtil 尚无对应测试，排序字符、长度边界和关键字检查仍需验证 |
| demo-manager | 已有实现类对应测试；CaptchaServiceImpl.getText 经 getCaptcha 间接执行，但现有用例未校验算式与答案一致，也未确定性覆盖各运算分支 |
| demo-service | 已有主要业务实现测试；ValidateAspectTest 位于 role/service 测试包，不应因包位置不同认定缺失；占位功能见工程现状 |
| demo-web | 当前未发现模块内测试；BaseController 请求绑定、排序/分页及 TokenAuthenticationFilter 认证上下文和刷新行为尚缺专项测试 |
| demo-dao / demo-open / demo-task | 当前未发现模块内测试；Service 中的 Mapper mock 不证明 DAO SQL 正确 |
| start | ApplicationTests 为全应用上下文测试，可能初始化外部组件，与纯单元测试分开报告 |

[UserDetailsServiceImplTest](../demo-service/src/test/java/com/example/demo/security/service/UserDetailsServiceImplTest.java) 当前验证按用户名加载；TokenServiceImplTest 中按 ID 加载用户是 mock，不能证明真实 loadUserById 已实现。不要为占位方法仅添加返回 null/空集合断言来宣称功能已覆盖。

[DingCallbackCrypto](../demo-manager/src/main/java/com/example/demo/dingtalk/manager/DingCallbackCrypto.java) 当前静态初始化设置 crypto.policy=limited；[对应测试](../demo-manager/src/test/java/com/example/demo/dingtalk/manager/DingCallbackCryptoTest.java) 在 BeforeAll 要求初始化不改变 JVM 策略。当原策略为 unlimited 时，该检查失败并阻止类内测试正常执行；初始化错误不能解读为全部加解密用例已执行。

当前未配置 JaCoCo 采集和报告，不能从测试通过数量推算行或分支覆盖率。功能缺口见 [工程现状](engineering-roadmap.md)，不因本页记录而自动实施。

## 运行命令

### 日常验证

```bash
# 检查环境
java -version
./mvnw -version

# 全模块编译，同时编译测试源码，但不执行测试
./mvnw -DskipTests test-compile

# Service 及其上游模块的单元测试（包括 Manager、Util，不包含 start）
./mvnw -pl demo-service -am test

# 单个模块及上游：按本次修改选择
./mvnw -pl demo-manager -am test
./mvnw -pl demo-util -am test

# 定向测试；-am 纳入上游，避免误用本地仓库旧模块产物
./mvnw -pl demo-service -am test \
  -Dtest=LoginLogServiceImplTest,OperateLogServiceImplTest \
  -Dsurefire.failIfNoSpecifiedTests=false

# 全仓库测试：包括需要外部配置的 ApplicationTests
./mvnw test

# 环境具备时的完整生命周期验证
./mvnw verify

# 基本差异检查（同时人工检查新增未跟踪文件）
git diff --check
```

- `-DskipTests` 不是测试通过；`-Dmaven.test.skip=true` 连测试编译也可能跳过，不能用于证明接口改动兼容。
- `-Dsurefire.failIfNoSpecifiedTests=false` 仅用于定向 reactor 测试，让没有匹配类的上游模块继续；必须确认目标测试实际执行，不能把“运行 0 个测试”当成功。
- 仅在依赖及插件已缓存时加 `-o`。失败时区分源码错误、依赖未缓存、网络/权限限制和外部服务不可用，不反复执行同一个必然失败的命令。
- 测试结果查看各模块 `target/surefire-reports/`，同时检查退出码、失败/错误/跳过数量和 reactor 汇总。测试中的预期异常可能打印 ERROR 日志，应结合报告判断。
- 当前没有仓库配置的 Checkstyle、Spotless、PMD、JaCoCo 覆盖率门槛或专门的 Failsafe 集成测试流程。不要声称这些检查已自动执行；`verify` 不会自动提供未配置的检查。

### JDK 25 / 受限运行环境的测试参数

Mockito 的 inline/static mock 在部分环境不能动态挂载 agent；POI/字体相关测试在无图形环境可能需要 headless。遇到对应问题时使用命令行参数，保留项目 POM 的现状：

```bash
# 用实际解析版本和实际 Maven 本地仓库路径替换；不要盲选目录中最高版本
MOCKITO_JAR=/absolute/path/to/mockito-core-resolved-version.jar
test -f "$MOCKITO_JAR"
./mvnw -pl demo-service -am test \
  "-DargLine=-javaagent:\"$MOCKITO_JAR\" -Djava.awt.headless=true"
```

可通过 effective POM / dependency tree 核对 Mockito 版本，通过 Maven settings 核对本地仓库；这些插件也需要可用缓存或网络。设置 `argLine` 前检查是否已有其他 agent 参数（如覆盖率），必要时合并，不要覆盖。项目已明确选择不在 POM 中额外加入 Mockito agent/headless 的 Surefire 配置；需要长期改变时按新的需求实施。



## 测试设计规则

当前测试集中于 `demo-util`、`demo-manager`、`demo-service`，使用 JUnit Jupiter、Mockito、AssertJ；测试类放在相同业务包的 `src/test/java`，以 `*Test.java` 命名。`start` 另有全应用上下文测试。

- 从需求、契约和历史缺陷设计用例，不复制实现制造必然通过的断言。修复缺陷优先添加能暴露该缺陷的回归测试；纯文档/缩进改动无需新增业务测试。
- 覆盖有效输入、边界、非法输入、缺失数据、外部失败和关键副作用。按风险补充权限范围、事务、ID 回填、批量部分失败、长整型计数、缓存失效与幂等场景。
- Util 测试当前限于 `com.example.demo.framework.util` 包。未经相应任务要求，不为第三方来源验证码/微信工具包扩张测试范围。
- Manager 测试 mock 外部 HTTP、SDK、Redis、MQ，不访问真实平台、不发送通知/短信、不创建真实订单。
- Service 测试 mock Mapper/Manager，必要时用 spy 隔离 MyBatis-Plus 持久化。已有 `ServiceTestSupport` 可初始化 Mapper 和 TableInfo；不要 mock 掉待验证的业务方法本身。
- 用 `ArgumentCaptor` 验证业务范围、审计值、状态和请求参数；避免仅验证“某方法被调用一次”而不检查结果。
- 自定义校验和事务注解通过代理测试；`TransactionProbe` 验证 Spring 事务 advice 的提交/回滚路径，不代表真实数据库回滚能力。
- 真实 SQL、约束、默认值、逻辑删除、并发与隔离级别需要数据库集成测试。当前尚无完备设施；需要时建立隔离 MySQL 测试环境，不能用 H2 自动代替 MySQL 特性并宣称等价。
- 时间/随机数使用固定值、可控时钟或合理边界断言，避免 sleep；静态 mock、全局时间/时区、系统属性及 SecurityContext 用后恢复。临时文件使用 JUnit 临时目录并正确关闭资源。
- 为协议/密码学编写独立固定向量或已知样例；只做加密后再解密不能充分证明兼容外部协议。
- 不删除失败断言、不批量加 `@Disabled`、不把异常吞掉来“修绿”。未实现接口不能通过断言 null/空对象被包装为功能完成。
- 按改动逐步验证：目标测试 → 所属模块及上游 → 公共契约的下游编译/回归。变更 `demo-api`、基础对象、序列化或父 POM 时，应覆盖全模块编译；新增证据出现前不无意义重复全量测试。
- 报告本次实际命令和结果；明确跳过、未执行、环境阻塞及其影响。历史通过数量和覆盖率不能当作本次结果。

## 按改动选择验证

| 改动 | 首先执行 | 扩大范围的条件 |
| --- | --- | --- |
| 纯文档/缩进 | 链接、命令语法、diff 检查 | 引入可执行逻辑或新证据时再扩大 |
| 工具转换/编码/资源 | 目标 Util 测试 | 公共语义影响调用方时执行上游与消费者回归 |
| Manager SDK/缓存参数 | 目标 Manager 测试 | Service 依赖其结果、异常或缓存语义时补消费者 |
| CRUD/审计/ID/批量 | 目标 Service 测试 | 权限、关系、缓存、事务变化时执行模块与相关消费者 |
| 自定义注解/AOP | 代理回归 | 切点、调用路径或签名改变时验证实际消费者 |
| Mapper/动态 SQL | 绑定与生成 SQL 检查、隔离库验证 | DDL、约束、逻辑删除或并发涉及真实 MySQL |
| BO/API/序列化 | 对应契约测试、全模块 test-compile | JSON、HTTP 或安全入口变化须验证真实 Web 行为 |
| 父 POM/公共依赖 | 全模块 test-compile、相关模块测试 | 按影响核对应用装配与平台兼容 |

先修复缺陷的回归，再模块/上游，不在已经通过且无新增证据时重复全量测试。ApplicationTests 需要显式区分装配与外部初始化副作用，不为文档修改启动全应用。

## 可复用样例

- [RedisServiceImplTest](../demo-manager/src/test/java/com/example/demo/cache/manager/RedisServiceImplTest.java)：捕获 Duration、默认 TTL、写入失败和外部异常。
- [BannerServiceImplTest](../demo-service/src/test/java/com/example/demo/banner/service/BannerServiceImplTest.java)：CRUD、审计及 ID 回填。
- [ValidateAspectTest](../demo-service/src/test/java/com/example/demo/role/service/ValidateAspectTest.java)：代理校验与禁止执行目标。
- [BeanUtilTest](../demo-util/src/test/java/com/example/demo/framework/util/BeanUtilTest.java)：转换路径和边界。
- [TransactionUtilTest](../demo-util/src/test/java/com/example/demo/framework/util/TransactionUtilTest.java)：事务同步与提交后行为。

阅读样例时保留其业务范围，不把现有断言直接视为所有新需求的验收。未实现接口需要明确契约，不能用返回 null 的断言包装成已完成功能。

## 失败定位与报告

| 现象 | 下一步 |
| --- | --- |
| 依赖或 Wrapper 下载失败 | 区分缓存、网络、镜像和权限，不把它当源码失败 |
| Mockito inline/static 初始化失败 | 核对实际解析的 agent、Maven JVM、已有 argLine |
| 字体或图形环境异常 | 按需临时启用 headless，并保留 POM 选择 |
| 目标类未运行 | 检查选择器、模块、报告和数量，禁止宣称通过 |
| ERROR 日志但报告成功 | 核对是否预期异常用例，以报告及断言为准 |
| 全应用初始化异常 | 定位实际异常与 Bean，结合代码分析；不默认归因 YAML 值 |

报告本次工作目录、JDK/Maven、完整命令、目标范围、实际执行数量、失败/错误/跳过、退出码和未验证项。可摘录脱敏证据，但不复制凭据或把历史结果当本次结果。
