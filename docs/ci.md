# 持续集成（CI）指南

维护于 2026-11-06。适用于 GitHub Actions、PR 检查、构建与测试报告、分支必需检查的维护。

**状态：Maven 工作流已配置，独立汇总门禁仍待实施。** 当前仓库包含 Maven 构建、CodeQL 和 Dependency Review 三个工作流。文件存在、工作流配置存在、远程执行成功、分支规则已生效是四种不同状态，必须分别提供证据。

本文不自动启用 CI、不修改 GitHub 设置、不授权部署或访问真实平台。默认 YAML 配置项完整、地址和账号有效，不核查或改写其中取值。

## 1. 职责与阅读入口

| 文档或配置 | 负责内容 |
| --- | --- |
| 本文 | 自动检查何时执行、任务范围、成功标准、报告、权限和维护 |
| [测试指南](testing.md) | 用例设计、本地测试、Mockito/headless 参数和结果解释 |
| [开发环境](development.md) | JDK、Maven Wrapper、模块构建与启动前提 |
| [依赖管理](dependencies.md) | POM/BOM、版本来源、解析和升级验证 |
| [审查指南](code-review.md) | 人工审查重点和交付证据 |
| [安全规则](security.md) | 凭据、外部系统和业务安全边界 |
| [运行手册](runbook.md) | 应用运行、发布、故障处理与回退 |
| [.github/workflows](../.github/workflows) | 实际执行的自动化配置，以文件和对应运行记录为准 |

CI 提供可重复验证，不替代需求验收、代码审查、真实 SQL 验证或发布审批。本文中的任务名和保留期限属于项目建议，不是 GitHub 或 OpenAI 对 Java 项目的强制规范。

## 2. 已核查的仓库现状

| 工作流 | 当前触发方式 | 当前配置 | 尚未核实 |
| --- | --- | --- | --- |
| [Java CI with Maven](../.github/workflows/maven.yml) | push 到任意分支、PR 目标为 master、手动运行 | ubuntu-24.04；Temurin 25；仓库 Maven Wrapper；build 执行全模块打包、单元测试及报告校验；dependency-submission 在任意分支 push 且 build 成功后提交依赖图 | GitHub 远程运行、依赖图功能可用性、分支是否要求其通过 |
| [CodeQL](../.github/workflows/codeql-analysis.yml) | push 到 master、PR 目标为 master、定时 cron `17 9 * * 6` | ubuntu-latest；Java；checkout@v3；codeql-action init/autobuild/analyze@v2；job 名 Analyze | 远程最近运行、平台支持情况、自动构建实际 JDK、分支是否要求其通过 |
| [Dependency Review](../.github/workflows/dependency-review.yml) | pull_request，未限制目标分支 | ubuntu-latest；checkout@v3；dependency-review-action@v1；contents: read | 远程运行、仓库功能可用性及实际阻断策略 |

上表记录文件当前声明。Maven 工作流使用已核对完整 SHA 的 checkout v7、setup-java v6、upload-artifact v6 和 maven-dependency-submission-action v6.0.1；两个既有安全工作流尚未迁移，不凭版本号断言它们本次运行失败。维护时核对官方迁移说明、runner 要求和实际检查结果，再选择受支持版本。

当前还没有：

- 独立的编译、单元测试 job 和统一汇总门禁；当前由 Maven 工作流的 build job 顺序执行。
- 已配置的 JaCoCo 覆盖率门槛、Spotless/Checkstyle 格式门禁。
- 完整隔离数据库集成测试、统一迁移基线或专门 Failsafe 流程。
- 在仓库文件中能够证明的 GitHub 必需检查、管理员绕过策略或合并队列设置。

[ApplicationTests](../start/src/test/java/com/example/demo/ApplicationTests.java) 使用 `@SpringBootTest`。全仓库 `test` 会包含它，可能初始化外部组件；普通 PR 单元测试应使用明确模块范围。

## 3. 当前流水线与后续拆分建议

当前 `.github/workflows/maven.yml` 保留 build 检查名：先跳过测试执行完成全模块打包（包括测试源码编译），再运行 `demo-service` 及上游的单元测试。测试开始后，即使 Maven 失败，也会校验并上传 Surefire XML/TXT 报告，保留 14 天；报告缺失、无实际测试、失败、错误或跳过都会使校验失败。普通 PR 只有 `contents: read`，不上传 JAR、不运行 Start 上下文测试。依赖图单独使用 `contents: write`，在任意分支 push 且 build 成功后执行，标签推送不触发；在其独立 runner 中先于同一 Maven reactor 执行 `-DskipTests package`，再生成依赖图，避免依赖缓存中的旧模块产物。

后续若将编译和单元测试拆分，可采用下表稳定的 job 显示名；当前没有单独的 `.github/workflows/java-ci.yml` 或 Java CI gate，不应在分支规则中直接要求不存在的检查。

| 建议检查名 | 任务 | 验证价值 | 首批合并要求 |
| --- | --- | --- | --- |
| Java compile | 全模块 test-compile，跳过执行测试 | 验证主代码、测试源码及跨模块消费者编译 | 必需 |
| Unit tests | demo-service 及其上游模块 test | 执行现有 Util、Manager、Service 单元测试 | 必需 |
| Java CI gate | 汇总前两项及报告校验结果 | 提供稳定、明确的合并检查入口 | 建议设置为必需检查 |
| 现有 CodeQL 检查 | 单独的安全分析工作流 | 分析代码问题 | 迁移并验证运行后按仓库策略配置 |
| 现有 Dependency Review 检查 | 单独的依赖变更审查 | 检查依赖变更风险 | 确认可用和失败策略后配置 |

编译和单元测试可以使用独立 job，各自 checkout、设置 JDK 并使用依赖缓存。独立 job 不共享工作目录或 target 产物；不能假设前一个 job 已经为后一个生成本地模块 JAR。

Java CI gate 只汇总自己工作流明确依赖的 job；不能声称它已汇总其他独立工作流。若采用汇总门禁，设置 `needs` 和 `if: always()`，显式要求各前置任务 result 为 success。失败、取消或意外 skipped 都不能被汇总成成功。报告缺失或校验失败也须传播到结果。

暂不把完整应用启动、真实支付/通知、发布、数据库迁移放进普通 PR 流程。后续隔离集成测试需要独立环境与验收，再纳入门禁。

## 4. 触发、并发与运行范围

| 事件 | 建议执行 | 说明 |
| --- | --- | --- |
| pull_request，目标 master | 编译、单元测试及适用安全检查 | 覆盖 PR 创建、后续提交、重新打开；核对实际 checkout 的提交 |
| push 到任意分支 | Maven 打包、单元测试，成功后提交依赖图 | 覆盖包含 `/` 的分支名；既有 CodeQL 的 push 触发仍仅限 master |
| workflow_dispatch | 手动运行同一套检查 | 用于排障，不接受任意 shell 命令输入 |
| schedule | 适用安全扫描、按需定期验证 | 不是每个 PR 的替代检查 |
| merge_group | 启用合并队列时追加对应必需检查 | 尚未确认仓库使用合并队列 |

建议同一 PR 的新提交取消旧的 Java 验证运行；并发组包含工作流身份及 PR 编号或分支引用，避免不同 PR、不同工作流互相取消。部署任务不能照搬验证任务的取消策略。

首批必需工作流先不设置顶层 paths/paths-ignore，保证每个目标 PR 都产生检查。纯文档的本地验证仍遵循测试指南；将来需要 CI 跳过业务构建时，增加明确的变更分类与始终运行的汇总检查，分类失败应执行完整检查或失败，不能默认成功。

GitHub 对被路径、分支或提交信息过滤的工作流可能保留 Pending；job 被条件跳过又不等同于测试已执行。使用合并队列时，必需工作流还需支持 merge_group。[GitHub 必需检查排障](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks)

## 5. 构建环境与可重复性

- 项目当前目标 JDK 25，Maven Wrapper 当前指定 3.9.16；以 [父 POM](../pom.xml) 与 [Wrapper 配置](../.mvn/wrapper/maven-wrapper.properties) 为事实来源。
- CI 显式设置 JDK 发行版和版本，不依赖 runner 自带 Java。记录实际 `java -version` 与 `./mvnw -version`，包括补丁版本；需要严格复现时固定经过验证的补丁版本并维护升级。
- 首批可采用 Linux runner；实施时选定受支持的镜像标签并记录。现有 ubuntu-latest 是可变化的环境，不代表 OS 和预装工具永久不变。
- 使用仓库 Wrapper，从根目录运行；核对脚本执行权限。不要将个人电脑的 JDK、IDEA Maven 或本地仓库路径写进工作流。
- 缓存依赖不代表依赖已经完整下载；首次运行默认允许正常解析，不默认使用 `-o`。不使用 `-U` 作为每次构建的例行选项。
- 需要时明确测试时区，记录选择；不要无依据把业务时区统一改成 UTC。测试应避免依赖 runner 的隐式时区、系统语言或当前日期。
- GitHub setup-java 支持 JDK 设置和 Maven 缓存；具体 Action 版本、参数和 runner 支持范围在实施时核对官方资料。[Java/Maven CI 指南](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)

### Maven 缓存

建议使用 setup-java 的 Maven 缓存能力，缓存键关联全部模块 POM，并考虑 Wrapper、JDK/OS 变化。缓存主要用于第三方依赖和插件，不缓存 target、测试报告、settings.xml、凭据或完整工作区。

仓库内模块通过 reactor 构建，不能依赖缓存中旧的 `com.example` 模块产物。先尝试冷缓存复现再认定代码问题；失效缓存时精确处理，不让清缓存成为每次失败的默认步骤。禁止让不可信 PR 生成的可执行构建产物被高权限任务无条件信任。

## 6. 命令入口与 Mockito 参数

以下为当前 Maven 工作流的命令入口；已写入配置不代表已在 GitHub 远程运行成功：

```bash
# 记录实际构建环境
java -version
./mvnw -version

# 全模块打包并编译测试源码，不执行测试；同时解析 Mockito 的实际路径
./mvnw -B -ntp -DskipTests package dependency:build-classpath \
  -DincludeGroupIds=org.mockito -DincludeArtifactIds=mockito-core \
  -Dmdep.outputFile=target/mockito-classpath.txt

# 单元测试：包含 Service 及上游 Manager、Util 等，不包含 Start
./mvnw -B -ntp -pl demo-service -am test \
  "-DargLine=-javaagent:\"${MOCKITO_JAR}\" -Djava.awt.headless=true"
```

`-B` 使用批处理模式，`-ntp` 减少下载进度输出。当前打包和测试分为同一 job 的两个步骤，结果分别判断。普通单元测试不直接运行根目录 `./mvnw test` 或 `verify`，以免无意执行 Start 上下文测试。

Mockito inline/static mock 在部分 JDK/runner 环境需要显式 agent；POI/图形测试可能需要 headless。配置原则与 [测试指南](testing.md) 一致，不在 POM 添加临时 agent/headless 配置。

```bash
# 示例占位路径：运行前替换成该 job 实际解析的 Mockito JAR
MOCKITO_JAR=/absolute/path/to/resolved/mockito-core.jar
test -f "$MOCKITO_JAR"
./mvnw -B -ntp -pl demo-service -am test \
  "-DargLine=-javaagent:\"$MOCKITO_JAR\" -Djava.awt.headless=true"
```

当前工作流读取 demo-util、demo-manager、demo-service 的 `target/mockito-classpath.txt`，要求每个模块解析出一个存在的 JAR，且三个路径一致，再写入 `MOCKITO_JAR`。因此版本和仓库路径来自 Maven 实际解析，不扫描目录取“最高版本”，也不使用机器专属路径。如多个模块解析到不同 Mockito 版本，应先解决差异或分别运行，不能共用未经核实的 agent。

已有 argLine 时合并必要参数，尤其未来 JaCoCo agent；不能覆盖已有 agent。下载依赖的准备步骤也要检查失败结果，不能继续执行空路径命令。具体解析方法见 [依赖管理](dependencies.md)，文档不虚构尚未实现的 CI 脚本。

## 7. 成功标准与测试报告校验

编译 job 成功至少要求 Maven 退出码为 0、所有预期模块构建成功、没有因参数或条件跳过应编译的测试源码。`-Dmaven.test.skip=true` 不能代替 test-compile 验证。

单元测试 job 成功至少要求：

1. Maven 退出码为 0，预期 reactor 模块完成。
2. 本次生成的 `TEST-*.xml` 存在且可以解析；不得读取缓存或旧工作目录的历史报告。
3. 当前预期测试模块 demo-util、demo-manager、demo-service 各自实际执行数量大于零。
4. failures 和 errors 为零；tests 减去 skipped 后仍有实际执行。首批建议预期模块 skipped 为零；确需保留已知跳过用例时逐项登记原因、范围和恢复条件。
5. 输出按模块及总计的 tests、failures、errors、skipped，保留报告和对应提交信息。

检查脚本必须在报告缺失、解析失败或条件不满足时返回非零；只有报告上传不能证明完成了验证。不要用历史固定总数作为永久阈值，参数化用例和正常新增删除都会影响数量；异常骤减应审查选择器、排除配置和测试改动。

不要全局设置 failIfNoTests 来破坏本来没有测试的上游模块。需要非零的是预期测试模块，而不是 reactor 的每个模块。定向本地排障使用的 `-Dtest`、`-Dsurefire.failIfNoSpecifiedTests=false` 不应误带进正常全范围 CI。

禁止 `|| true`、无条件 continue-on-error、吞掉测试退出码或删除失败断言来制造成功。若使用管道保存 Maven 输出，配置 Bash 的 pipefail。Maven 失败后仍可运行报告收集，但最终必须保留失败状态。

## 8. 报告、日志与构建产物

| 内容 | 建议收集范围 | 使用方式 |
| --- | --- | --- |
| Surefire 报告 | demo-util、demo-manager、demo-service 各自 target/surefire-reports | XML 用于统计，文本用于定位失败 |
| 编译/测试日志 | 本次 job 的必要输出和首个有效异常 | 不启用默认全量 debug，不打印全部环境变量 |
| 运行元数据 | commit SHA、事件、工作流运行链接、JDK/Maven、模块范围 | 关联代码版本与验证证据 |
| JAR | 仅专门打包验证任务按需收集 | 普通编译/单元测试任务不必上传 |

建议报告收集步骤使用适当的 always 条件，让失败时也能上传；runner 被终止时不能保证报告完整。上传 artifact 名包含 job/运行身份，避免并发覆盖。首批可采用 14 天报告保留期，具体不超过仓库/组织允许范围；这是待实施的项目选择。

报告上传和报告有效性校验分开设计：测试尚未启动就失败时，上传缺文件可给提示，但整个任务仍须失败。禁止上传完整配置、settings.xml、私钥、证书、用户数据或包含敏感值的 dump。

不默认上传 Start JAR；应用资源可能随打包进入产物。若将来保存产物，应明确访问范围与内容审查，这不要求核查或修改当前 YAML 值。

## 9. 分支保护与必需检查

工作流启用后按顺序验证：

1. 在真实测试 PR 运行成功，记录 GitHub 实际显示的检查名、来源和目标 SHA。
2. 验证一个编译/测试失败的受控 PR，确认失败能传递到门禁。
3. 将稳定的 Java CI gate 或两个基础检查设置为 master 的必需检查；安全检查单独选择，不凭文档中建议名称直接假定存在。
4. 验证缺失、失败和取消的检查会按预期阻止合并，合法更新后能恢复。
5. 根据团队既有要求核对 PR 必需、管理员绕过和更新分支要求；本文不更改远程设置。

各工作流的 job 显示名保持唯一和稳定。改名时同步分支规则，否则旧检查可能一直等待。启用路径跳过、合并队列、矩阵任务或工作流拆分时重新验证门禁。

GitHub 显示绿色不等于业务测试确实执行，汇总规则必须检查实际 job 状态和报告。CodeQL 执行成功也不自动证明不存在告警，应核对安全告警及仓库实际阻断策略。[GitHub 必需检查说明](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks)

## 10. 权限与不可信 PR

- 编译、单元测试 job 默认只需读取仓库；按 job 最小化权限，不授予写仓库、发布包或部署权限。
- 业务单元测试使用虚构数据和 mock，不需要生产凭据。外部平台写入不因 CI 执行而获得授权。
- 普通 PR 检查采用 pull_request；不能为了取得 secrets 改用 pull_request_target 后检出并执行不可信 PR 代码。[GitHub 安全说明](https://docs.github.com/en/actions/reference/security/securely-using-pull_request_target)
- fork PR 的权限和 secrets 可用性按 GitHub 实际配置处理。需要审批或无法获得依赖权限时明确显示阻塞，不通过扩大权限掩盖问题。
- Action 优先固定经核对的完整 commit SHA，并保留可读版本说明；升级时核查来源与变更。不要在本文复制未经验证的 SHA。[GitHub 安全使用参考](https://docs.github.com/en/actions/reference/security/secure-use)
- PR 标题、分支名和手动输入按不可信文本处理，避免直接插入 shell；使用受控参数、环境变量传递及正确引用。
- CodeQL 等安全任务确需的写权限单独限定；普通编译任务不继承不必要的高权限。不要将不可信代码送到能够访问真实业务网络的持久化 runner。

## 11. 常见失败定位

| 现象 | 核查顺序 | 处理边界 |
| --- | --- | --- |
| Wrapper/依赖下载失败 | 下载阶段、镜像、网络、权限、缓存完整性 | 不认定为业务测试失败；不默认加离线参数 |
| release/类版本不匹配 | Maven JVM、JAVA_HOME、JDK 设置步骤、目标版本 | 不降低源码目标来绕过错误 |
| Mockito 初始化失败 | 实际 Mockito 版本、agent 文件、argLine、JDK | 使用临时参数，不往 POM 添加机器路径 |
| 字体/图形错误 | 失败测试、headless、runner 字体条件 | 不删除导出断言来修绿 |
| 0 测试或报告缺失 | 模块、选择器、skip 参数、Surefire 配置、前置失败 | 按失败/阻塞报告，不能宣布通过 |
| ERROR 日志但测试通过 | XML 报告、预期异常断言、退出码 | 不仅凭日志级别判断失败 |
| Start 初始化外部组件 | 实际命令是否误含 start、测试范围 | 普通单元流程收窄范围，不关闭鉴权或验签 |
| 必需检查一直 Pending | 工作流触发过滤、检查改名、来源、merge_group | 修复工作流与分支规则对应关系 |
| 缓存命中后缺类/行为异常 | 当前 reactor、依赖解析、缓存 key、冷缓存结果 | 不依赖历史仓库内模块 JAR |
| 本地成功、CI 失败 | 同一提交、JDK/Maven、OS、时区、大小写、资源和静态状态 | 最小化复现，再决定是否扩大测试 |

排查记录包含运行链接、提交、事件、具体 job、首个有效异常、退出码和报告统计。允许对明确的网络偶发故障重跑，但不持续重跑不稳定测试直到偶然变绿；应记录随机、并发、时间或共享状态问题。

## 12. 分阶段建设与验收

| 阶段 | 交付内容 | 验收证据 |
| --- | --- | --- |
| 第一阶段 | Java 编译、单元测试、agent 解析、报告校验与上传、稳定汇总检查 | 干净 runner 成功；失败用例能阻断；无真实平台调用 |
| 第二阶段 | 核对并迁移已有安全工作流，配置必需检查 | 正常/失败 PR 的远程结果与分支规则一致 |
| 第三阶段 | 隔离 MySQL、Web 契约等集成验证 | SQL/权限/回滚实际验证，环境可重建、数据可清理 |
| 第四阶段 | 根据基线添加覆盖率、风格和耗时优化 | 门槛有依据，不靠排除业务代码或批量格式化达标 |

全模块编译、单元测试、集成测试、应用装配、打包和发布分别报告。未来新增 Web/DAO 测试后，更新测试执行范围及预期模块；当前 `-pl demo-service -am` 不会自动包含下游 Web/Task/Start。

## 13. 文档与工作流维护

修改工作流的作者同步本文现状表、检查名、命令、权限和报告范围；修改测试布局时同步 [testing.md](testing.md)，修改工具版本时同步 [dependencies.md](dependencies.md)。长期取舍按需记录 [ADR](decisions/README.md)，复杂建设遵循 [PLANS.md](../PLANS.md)。

仅文档变更：核对事实、相对链接、代码块语法和 diff，不运行业务测试或访问远程平台。实际工作流变更：还需验证 YAML/表达式、真实触发与运行结果；语法正确不能代替远程执行。

完成报告分别列出：已写配置、已运行的检查、目标测试数量、报告位置、已设置的远程门禁和未完成事项。Maven 工作流已写入配置；远程运行与分支规则须另行提供证据，后续拆分和汇总门禁仍未实施。
