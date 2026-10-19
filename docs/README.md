# 项目文档导航

维护于 2026-10-13。本目录服务于本项目和 Codex 协作；除特殊入口外，普通 Markdown 不会因存在于仓库而自动成为模型指令。按任务读取相关材料，避免每次加载所有文档。

| 工作类型 | 阅读入口 | 作用 |
| --- | --- | --- |
| 首次进入项目 | [根 README](../README.md)、[开发环境](development.md) | 技术基线、构建与启动前提 |
| Codex 协作 | [AGENTS.md](../AGENTS.md)、[Codex 工作流](codex-workflow.md) | 长期规则与任务上下文 |
| 修改模块边界、数据流程 | [架构说明](architecture.md) | 依赖方向和关键行为 |
| Java、对象与注解 | [Java 约定](java-conventions.md)、[代码风格](code-style.md) | 类型、转换、代理、异常和书写规范 |
| Controller/API/JSON | [API 约定](api-conventions.md) | 解析、响应、身份、分页和兼容性 |
| 表结构、建表、数据库迁移 | [数据库指南](database.md) | 证据状态、字段字典、单表草案、迁移和验证 |
| CRUD、DO、SQL、事务 | [持久化规则](persistence.md) | 审计、ID、批量、软删除与真实 SQL |
| POM、BOM、SDK 和插件升级 | [依赖管理](dependencies.md) | 声明来源、解析、兼容、迁移与下游验证 |
| 配置消费者 | [配置说明](configuration.md) | 键名、用途、类型和消费位置，不核查 YAML 取值 |
| Redis、MQ、MQTT 与推送 | [缓存与消息契约](cache-and-messaging.md) | 键格式、TTL、失效、消息路由与结构、重复和重试边界 |
| 跨多个编码领域 | [编码规则导航](coding-standards.md) | 选择专项规则及检查顺序 |
| 添加或运行测试 | [测试指南](testing.md) | 测试范围、运行方法、结果解释 |
| CI、GitHub Actions、合并检查 | [持续集成指南](ci.md) | 工作流现状、待建设门禁、报告、权限与排障 |
| 代码审查、PR | [审查指南](code-review.md)、[贡献指南](../CONTRIBUTING.md) | 有实际影响的问题与交付证据 |
| 安全、支付、回调 | [安全开发](security.md)、[安全报告政策](../SECURITY.md) | 信任边界与报告渠道 |
| 启动、打包、故障排查 | [运行手册](runbook.md) | 环境核对、定位和回退 |
| 新功能需求 | [需求说明](requirements/README.md)、[需求模板](requirements/template.md) | 可验证的业务契约 |
| 复杂任务 | [PLANS.md](../PLANS.md)、[计划模板](plans/template.md) | 阶段、决策、验证及续接 |
| 长期技术决策 | [ADR 索引](decisions/README.md) | 理由、替代方案、后果 |
| 重复操作 | [技能设计说明](skills/README.md) | 何时值得整理成 Codex 技能 |
| 工程建设 | [工程现状与建议](engineering-roadmap.md) | 已知缺口与待建设能力 |

## 更新约定

`AGENTS.md` 保留关键约束与强制阅读路由；专项文档承载相应领域详细规则、背景、操作和示例。每条详细规则只在所属文档维护，避免重复全文。需求、计划、ADR 分别记录行为、实施路径、长期取舍。发生冲突先核对用户指令和实际源码，不凭旧文档断言现状。

修改相关代码或配置的作者同步文档；审查者核对命令、路径、链接和事实。示例仅用虚构数据，不保存个人路径、真实配置值、历史聊天记录或完整运行日志。未来建设应标明未落地，不作为已存在的质量门禁。

## 第一批的维护责任与事实来源

| 文档 | 主要事实来源 | 同步更新时机 |
| --- | --- | --- |
| architecture | 模块 POM、调用方、装配类 | 依赖边界或关键流程改变 |
| java-conventions / code-style | 基础对象、BeanUtil、ValidateAspect、项目选择 | 类型/注解/转换语义或风格改变 |
| api-conventions | Controller、BaseController、Response、JSON/安全配置 | 公共契约或请求解析改变 |
| database | DO/父类、Mapper、Service、经验证脚本与实例结构证据 | 字段、约束、索引、迁移或验证状态改变 |
| persistence | Service、DO、BaseQuery、Mapper/XML、事务工具 | CRUD、SQL、分页、审计或事务改变 |
| configuration | YAML 键结构、Java @Value、日志引用 | 键名、类型、默认表达式或消费者改变 |
| cache-and-messaging | Redis 常量及调用方、MQ/MQTT 生产者和消费者 | 键、TTL、失效入口、topic/tag/group、消息结构或失败处理改变 |
| dependencies | 根/模块 POM、Wrapper、官方资料与实际解析 | 版本、BOM、scope、排除项或插件改变 |
| ci | 工作流文件、POM、测试布局与远程运行证据 | 触发条件、检查名、权限、报告和分支规则改变 |
| testing | 测试 POM、辅助设施、样例和实际运行 | 工具、命令、验证范围改变 |

改动作者同步相关文档，审查者核对事实与证据。新增规范标注为项目选择，现状附源码入口，未实施的格式化/CI/集成能力明确说明。源码和文档冲突时先定位并修正文档，不能删除测试来迎合说明。配置按用户约定默认完整有效，不复述或审查 YAML 值。
