# demo 项目协作指南

适用于仓库根目录及全部子模块。使用中文沟通，代码标识符沿用项目英文命名。维护于 2026-10-13。

本文是 Codex 的长期规则与阅读入口。专项文档中的现状以 POM、源码、配置和实际验证为依据；项目工程选择不代表 OpenAI 对 Java 的强制要求。

## 1. 使用规则与工作流程

- 遵守运行环境的系统规则及用户当前明确要求；本文记录项目默认规范。更深目录的 `AGENTS.md` 可补充或覆盖该目录的项目约定，修改前检查目标目录的指令文件。
- 先读取相关代码、调用方和测试，再修改；不要仅依据类名、旧聊天记录或 README 推断行为。项目现状以 POM、源码、配置和实际运行结果为依据。
- 开始时检查 `git status --short`、相关 diff。保留用户已有修改、未跟踪文件和配置；不要擅自恢复文件、清理工作区或大范围格式化。
- 明确需求的输入、输出、失败场景及兼容性；简单任务直接完成。跨模块重构、依赖升级、数据库或协议调整，先列出影响范围、步骤和验收方式，再分阶段实施。
- 常规实现选择自行判断。只有缺少关键业务信息且无法从代码确定时才提问；不为普通读写文件、局部修复或测试反复确认。
- 修改范围围绕当前需求。发现无关缺陷时记录文件位置、触发条件和影响，不借机改写整个模块。
- 搜索优先使用 `rg` / `rg --files`，排除 `.git/`、`target/` 和 IDE 缓存。阅读配置、日志和差异时避免输出密钥。
- 长任务保留简短进度、关键决定、已验证事项和下一步；确有跨会话需要时才增加任务计划文档，不为每个小改动建立流程文件。
- 不因本文授权而自动提交、推送、创建 PR、合并或发布。用户已明确授权的操作按其范围执行；涉及真实外部系统的写入应确认环境和既有授权。

## 2. 按任务读取专项规则

以下文档承载原有项目详细约定；开始相关修改前必须读取，不将普通 Markdown 的存在视为已自动加载。多个任务类型交叉时读取相应材料，避免每次加载全部文档。

| 任务 | 必读文档 |
| --- | --- |
| 新增或修改 Java 类、注解、转换、异常 | [Java 约定](docs/java-conventions.md)、[代码风格](docs/code-style.md) |
| Controller、请求、响应、认证入口、公共字段 | [API 约定](docs/api-conventions.md)、[安全规则](docs/security.md) |
| 数据库表结构、建表、迁移 | [数据库指南](docs/database.md)、[持久化规则](docs/persistence.md)、[测试指南](docs/testing.md) |
| Service CRUD、DO、Mapper、分页、事务、批量 | [持久化规则](docs/persistence.md)、[测试指南](docs/testing.md) |
| 外部平台、支付、缓存、消息、文件、WebSocket/SSE | [安全规则](docs/security.md)、[工程现状](docs/engineering-roadmap.md) |
| Redis、MQ/MQTT、消息推送与缓存失效 | [缓存与消息契约](docs/cache-and-messaging.md) |
| 模块边界、调用流程、公共基础对象 | [架构](docs/architecture.md)，并编译受影响消费者 |
| POM、版本、构建和启动 | [依赖管理](docs/dependencies.md)、[开发环境](docs/development.md)、[配置](docs/configuration.md)、[测试](docs/testing.md)、[运行手册](docs/runbook.md) |
| 添加或运行测试 | [测试指南](docs/testing.md) |
| 配置、Bean 初始化、启动失败 | [配置说明](docs/configuration.md)、[开发环境](docs/development.md) |
| CI、GitHub Actions、合并检查 | [持续集成](docs/ci.md)、[测试指南](docs/testing.md) |
| 代码审查 | [审查指南](docs/code-review.md) |
| 新功能或复杂跨模块工作 | [需求说明](docs/requirements/README.md)、[PLANS.md](PLANS.md)、相关领域代码 |

完整导航见 [docs/README.md](docs/README.md)。复杂重构、迁移、依赖升级或需跨会话的工作遵循 PLANS.md；简单修复无需独立计划。长期取舍写 [ADR](docs/decisions/README.md)，技能说明不表示已安装或已改变权限。

## 3. 模块与关键约束

- 这是 Maven 多模块后端：Web 适配协议，Service 编排业务，Manager 封装外部能力，DAO 管理 SQL，API 提供契约，Util 提供通用工具，Task 执行任务，Start 装配应用。完整依赖及参考类见架构文档。
- API/BO 不依赖 Service/Web/DAO 实现，不引入模块循环依赖。不要仅凭 Service 类名判断模块；同名实现注入时核对包名和 Bean 名。
- Java 默认使用 JDK 25，实际版本以父 POM 与 Maven JVM 为准。共享版本在父 POM/BOM 管理，测试依赖使用 test scope。
- 保留路径、请求方法、响应、日期、null/空列表和错误码契约；自定义校验依赖代理，不能机械换成 Jakarta 或 JSpecify 注解。
- 审计使用 createBy/updateBy 与 create_by/update_by；插入需要时回填 ID。默认值以实际 DDL 为准，不额外设置已由数据库管理的字段。
- 更新/删除保留可信 ID、业务归属与软删除条件；检查影响行数。普通保存不包通用 try/catch；唯一键冲突仅按业务需要转换。
- Mapper 自定义计数方法返回 Long，Service/API/Controller 计数使用 long，COUNT resultType 用 java.lang.Long，影响行数仍是 int；可选分页不是公开输入校验，不得将非法参数变成无界查询。
- 不共用 list/get SELECT 投影；count/list 可共享过滤条件。SQL 值绑定用 #{}，动态排序用白名单；CDATA 与 Mapper 缩进按持久化规则处理。
- Chat 的 saveOrUpdate 先按业务键 updateChat，影响行数不为 1 时插入，不改成按主键的通用 saveOrUpdate。
- 本地事务不能回滚 Redis/MQ/HTTP；TransactionUtil.afterCommit 要求活动事务，无持久化和自动重试保证。
- 默认 YAML 配置项完整、地址和账号有效；不核查、评价或改写其中取值。配置文档只记录键、用途、类型与代码消费者。
- 不访问真实平台运行单元测试；Mapper mock 不能证明 SQL 正确，TransactionProbe 不能证明真实数据库回滚。start 的上下文测试与纯单元测试分别报告。
- 不在 POM 增加 Mockito agent/headless 临时配置，按测试指南使用命令参数；不以跳过、0 个目标测试或历史结果宣称本次通过。
- 不泄漏凭据，不为测试关闭鉴权/验签，不扩大公开入口。真实环境写入、迁移和发布遵循明确授权范围。
- 已知占位接口、SQL 绑定缺口及尚未建设的 CI/集成测试能力见工程现状；不要把建议描述成已落实。

## 4. 基本验证入口

从仓库根目录执行；环境和必要临时参数先读取开发与测试指南。

```bash
./mvnw -DskipTests test-compile
./mvnw -pl demo-service -am test
git diff --check
```

第一条只证明主代码和测试代码编译；第二条不包含 start。按改动选择验证，不为纯文档执行业务测试。检查报告、退出码与目标实际运行数量，明确未执行及其影响。

## 5. 审查、Git 与完成标准

### Review guidelines

代码审查优先报告有明确触发条件和实际影响的问题，给出文件/行号、后果和修复方向。重点检查：

- 权限或用户/企业范围遗漏、无条件更新/删除、SQL 拼接、敏感日志。
- 事务/缓存/MQ 时序、支付与回调幂等、失败被当作成功。
- count 类型截断、分页退化成无界查询、字段或响应契约不兼容。
- Mapper 方法与 SQL 映射遗漏、软删除/审计字段丢失、插入 ID 回填缺失。
- 测试是否覆盖真实变化、mock 是否掩盖错误、是否新增外部服务依赖。

不要把个人风格偏好当作阻塞问题；对推测明确标注，不凭不存在的统一规范提出整改。

### Git 与 PR

- 沿用用户当前工作分支；需要新建时使用有意义的名称（Codex 默认 `codex/` 前缀）。不要擅自切走或重置用户工作。
- 未明确要求时不提交或推送；正常交付通过分支和 PR 合入 `master`，不要默认直接推主分支。实际远程保护配置应核实，不能只凭本文宣称已启用。
- 一次提交聚焦一个可解释的变更；排除 `target/`、日志、JVM dump、密钥和机器专属设置。
- PR 描述说明问题、修改后的行为、兼容/迁移影响、验证命令及结果；数据库或发布变更还应提供执行顺序和回退策略。

### Definition of done

1. 修改满足需求，并保持未要求改变的业务契约；所有受影响的接口、实现、调用者、Mapper、测试和配置已同步。
2. 针对风险完成必要测试及编译，确认目标测试确实运行；无法验证的部分明确说明原因及影响。
3. 审阅 diff，检查格式、无关改动、敏感数据、遗漏调用点和新增文件；`git diff --check` 无本次引入的问题。
4. 必要的配置说明、迁移脚本、使用文档及规则更新齐全；“脚本已生成”“已验证”“已执行上线”分别表述。
5. 最终用中文说明具体变化、验证结果及剩余限制；按需给文件链接。未实际完成的检查或功能不标记成功。

## 6. 维护与官方依据

核心规则留在本文，专项规则保存在对应文档。修改源码时同步实际涉及的文档；不通过删除约束掩盖实现或测试问题。版本、构建命令、公共契约和已知缺口变化后更新相关入口，避免重复维护全文。

官方建议保持指令实用、明确上下文与完成条件，复杂任务按需规划。默认项目指令合计读取上限为 32 KiB；更深目录文件不自动消除总体大小限制，需核对实际配置及加载范围。

- [OpenAI：AGENTS.md](https://learn.chatgpt.com/docs/agent-configuration/agents-md)
- [OpenAI：最佳实践](https://learn.chatgpt.com/guides/best-practices)
- [OpenAI：代码审查规则](https://developers.openai.com/blog/custom-code-review-rules-for-codex)
