# 数据库结构、建表与演进指南

维护于 2026-10-22。适用于 demo 项目的数据库结构说明、建表基线、字段映射、索引设计、数据迁移及验证。当前按表名字母顺序收录 `tb_dict_data`、`tb_dict_type`、`tb_login_log`、`tb_menu`、`tb_operate_log`、`tb_role`、`tb_role_menu` 的源码信息与建表草案；其他表尚未补充，不调整实际数据库结构。

**当前状态：源码已核对，真实数据库结构未读取，文中建表草案未执行。** 本文不代表已有完整数据库基线或自动迁移能力。默认 YAML 配置项完整、地址和账号有效，不核查、评价或改写其中取值。

## 1. 文档依据与使用目的

OpenAI 官方建议为 Codex 提供明确的任务上下文、工程约定、验证入口和完成标准，保持 AGENTS.md 简洁，并引用专项 Markdown。[OpenAI 最佳实践](https://learn.chatgpt.com/guides/best-practices)

本文是上述原则在 demo 数据库领域的项目化应用。官方没有要求使用 database.md 这个文件名，也没有规定 MySQL 字段类型、索引、迁移工具或本页章节模板。具体数据库设计须依据项目业务、源码和数据库证据。

普通 Markdown 不因文件存在而自动加载；数据库任务通过 [AGENTS.md](../AGENTS.md) 阅读路由引用本页。Codex 的指令发现机制见 [官方 AGENTS.md 指南](https://learn.chatgpt.com/docs/agent-configuration/agents-md)。

本页帮助开发者和 Codex 回答：表保存什么、字段如何映射、哪些限制已被证实、如何创建或升级、怎样验证正确、哪些结论仍然未知。不能把推测的字段长度和默认值当成已部署事实。

## 2. 与其他文档的分工

| 入口 | 内容归属 |
| --- | --- |
| 本文 | 数据库结构、字段字典、建表草案、约束、索引、初始化、迁移和验证 |
| [持久化规则](persistence.md) | Service CRUD、MyBatis、审计、软删除、事务及 Mapper 编码规范 |
| [架构](architecture.md) | 模块职责和调用关系 |
| [配置说明](configuration.md) | 配置键、类型、用途与消费者，不存放真实连接信息 |
| [测试指南](testing.md) | 单元测试、集成验证边界、运行方法 |
| [持续集成](ci.md) | 自动检查范围、报告及尚未建设的门禁 |
| [运行手册](runbook.md) | 发布、运行故障与恢复流程 |
| [决策记录](decisions/README.md) | 迁移工具、唯一性策略等长期取舍 |

未来经验证的可执行 DDL 以 SQL 文件为唯一维护来源，本页引用它并解释设计。当前没有对应 SQL 文件，草案暂保存在第 7、8.4 及第 9—13 节各表条目；提取正式脚本时移除本页重复 SQL，避免两份定义漂移。

## 3. 事实来源与证据状态

数据库现状与目标设计必须分开记录：真实实例元数据描述该实例，版本化脚本描述仓库预期，Java/Mapper 描述应用需要。三者冲突时记录差异并定位，不擅自以其中一份覆盖其他环境。

| 状态 | 含义 | 需要的证据 |
| --- | --- | --- |
| 源码确认 | 字段/条件/业务值可从仓库确定 | DO、父类、Mapper、Service、调用者 |
| 设计草案 | 为满足业务提出的类型或约束 | 设计理由、假设、待确认项 |
| 隔离验证 | 已在独立 MySQL 环境执行 | 版本、执行步骤、结果、失败及清理记录 |
| 实例核实 | 已读取明确环境的真实结构 | 脱敏 SHOW CREATE TABLE / 索引信息及核对日期 |
| 已部署 | 变更已在指定环境完成 | 版本、执行时间、操作记录和验收结果 |

当前仓库未发现完整建表 SQL、版本化 schema 基线或 Flyway/Liquibase 配置；第 16.1 节的单项迁移仍为设计，所列脚本当前缺失。不能从 MyBatis DO 自动还原全部数据库定义，也不能把源码中的关联字段当作已存在的外键。

## 4. 环境基线应记录什么

| 项目 | 本次结论或填写要求 |
| --- | --- |
| 数据库产品 | 项目 SQL 使用 MySQL 方言 |
| 实际服务端版本 | 尚未核实；JDK、驱动或 Boot 版本不能证明服务端版本 |
| 存储引擎 | 本页草案建议 InnoDB；现有表引擎未核实 |
| 字符集/排序规则 | 草案为 utf8mb4 / utf8mb4_unicode_ci；实际值未核实 |
| SQL mode | 记录实际运行与集成测试环境，不预设其一致 |
| 时间处理 | 记录应用、JDBC、数据库会话时区及字段精度，不能直接按开发电脑时区推定 |
| ID 策略 | 本文七张表的 DO 均明确 AUTO；整数宽度及是否无符号仍需确认 |
| 隔离级别 | 实例/会话实际值及相关业务要求待核实 |
| 迁移方式 | 当前未建立统一自动迁移流程 |
| 备份恢复 | 实际工具、保留期、恢复点和恢复演练证据待建设/核实 |

后续在明确环境与授权范围内，可通过以下只读语句收集结构证据；本次未连接数据库执行：

```sql
SELECT VERSION();
SELECT @@SESSION.sql_mode, @@SESSION.time_zone;
SHOW CREATE TABLE `tb_dict_data`;
SHOW INDEX FROM `tb_dict_data`;
SHOW CREATE TABLE `tb_dict_type`;
SHOW INDEX FROM `tb_dict_type`;
SHOW CREATE TABLE `tb_login_log`;
SHOW INDEX FROM `tb_login_log`;
SHOW CREATE TABLE `tb_menu`;
SHOW INDEX FROM `tb_menu`;
SHOW CREATE TABLE `tb_operate_log`;
SHOW INDEX FROM `tb_operate_log`;
SHOW CREATE TABLE `tb_role`;
SHOW INDEX FROM `tb_role`;
SHOW CREATE TABLE `tb_role_menu`;
SHOW INDEX FROM `tb_role_menu`;
```

输出只保留必要的结构信息，不收集真实用户数据、账号、密码或连接串。检查结构不等同于检查 YAML 取值。

## 5. 表结构条目的统一格式

未来每张表按需补充以下信息。当前收录首页列出的七张表，其他表按后续任务逐步补充。表条目按表名排序；执行初始化时另按依赖顺序。

1. 表名、用途、业务归属、维护日期、证据状态。
2. 对应 DO/BO、Mapper XML、主要 Service 和公开消费者。
3. 列名、Java 属性、SQL 类型、长度/精度、可空、默认值、字段含义。
4. 主键、自增、业务唯一性、外键或应用关联、软删除及生命周期。
5. 主要查询条件、排序、分页和索引依据。
6. 初始化数据要求、升级顺序、历史数据处理及回退限制。
7. 验证步骤、已执行结果、尚未确定的问题。

字段类型以实际数据范围和协议为依据。Java String 不包含长度信息，BigInteger 不保证对应 BIGINT UNSIGNED，Date 也不指定 DATETIME/TIMESTAMP 或精度。

## 6. tb_dict_data：源码确认的结构与行为

### 6.1 用途与代码入口

字典数据保存类型关联、名称和值，以及启用状态、备注和审计信息。

- [DictDataDO](../demo-dao/src/main/java/com/example/demo/dict/dao/dataobject/DictDataDO.java)：表名、主键与直接持久化字段。
- [BaseDO](../demo-dao/src/main/java/com/example/demo/framework/dataobject/BaseDO.java)：软删除与审计字段。
- [BaseQuery](../demo-dao/src/main/java/com/example/demo/framework/query/BaseQuery.java)：查询参数，均为非持久化字段。
- [DictData](../demo-api/src/main/java/com/example/demo/dict/api/bo/DictData.java)：业务对象及 E/D 状态枚举。
- [DictDataMapper.xml](../demo-dao/src/main/resources/mappers/DictDataMapper.xml)：筛选、投影、排序与分页。
- [DictDataServiceImpl](../demo-service/src/main/java/com/example/demo/dict/service/DictDataServiceImpl.java)：读写、ID 回填和缓存处理。
- [DictTypeServiceImpl](../demo-service/src/main/java/com/example/demo/dict/service/DictTypeServiceImpl.java)：类型变更时更新关联数据，删除类型时调用字典数据删除。

### 6.2 字段字典

下表 SQL 定义均为草案；字段存在及映射可由源码确认，但长度、非空和数据库默认值尚未从实例核实。

| 列名 | Java 属性 | 草案定义 | 语义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED，自增主键 | @TableId(AUTO) 确认自增；无符号为建议 |
| type_id | BigInteger typeId | BIGINT UNSIGNED NOT NULL | 字典类型 ID；Service 插入时设置 |
| type_value | String typeValue | VARCHAR(100) NOT NULL | 类型编码，查询和缓存使用 |
| name | String name | VARCHAR(100) NOT NULL | 展示名称，支持模糊查询 |
| value | String value | VARCHAR(255) NOT NULL | 字典值，DO 显式反引号映射 |
| remark | String remark | VARCHAR(500)，允许 NULL | 备注 |
| sort_order | Integer sortOrder | INT NOT NULL DEFAULT 0 | Mapper 投影及排序依赖该列 |
| status | String status | CHAR(1) NOT NULL DEFAULT 'E' | E 启用、D 停用；默认 E 是草案选择 |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | Mapper 正常记录条件为 0，应用删除设置 true |
| create_by | String createBy | VARCHAR(100) NOT NULL | 来自可信创建人参数；不能因为是审计人就改成数字用户 ID |
| create_time | Date createTime | DATETIME，默认当前时间 | 草案由数据库初始化 |
| update_by | String updateBy | VARCHAR(100)，允许 NULL | 新记录可能尚无更新人 |
| update_time | Date updateTime | DATETIME，初始化并自动更新时间 | 草案选择，当前实例行为未知 |

不建列的属性：typeValues，以及 BaseQuery 的 search、start、end、codes、sort、dir、pageNo、pageSize、offset。它们标记 exist=false；serialVersionUID 同样不是业务列。

### 6.3 当前行为与不变量

- count/list 显式过滤 is_deleted=0，共享 queryCondition。
- 按 typeId、typeValue、typeValues、name、status 筛选；name 使用项目通配符处理器。
- 列表按 type_id、sort_order 升序，id 降序；正数页码和页大小同时存在时才添加 LIMIT。
- 按类型编码和值的 getData 使用 MyBatis-Plus lambdaQuery/getOne，精确过滤 typeValue、value 和启用状态 E，按 id 降序并固定 LIMIT 1，保留原 XML 取最新一条的语义；这不能证明数据有业务唯一约束。软删除过滤依赖应用 MyBatis-Plus 配置。
- 按类型编码和值获取数据的 Service 请求启用状态 E，并通过 Redis 缓存；缓存不是数据库唯一性保证。
- insertData 设置 typeId、createBy，保存后回填 ID；没有在此方法内给 typeValue/name/value/status 提供完整字段校验。
- 单条更新清除 DO 的 typeId/typeValue，保留原类型归属；类型变更有专门的批量更新路径。
- BaseDO 审计字段均为数据库列；2026-10-22 的字段写入策略仅控制实体生成的 INSERT/UPDATE，不改变列定义或查询投影。createBy/updateBy 的 JSONField(serialize=false) 已移除，不能再依赖该注解隐藏审计人。详细策略见 [持久化规则](persistence.md#basedo-插入与更新策略)。

### 6.4 排序字段命名与映射

排序列统一使用 `sort_order`，Java 使用可空 Integer sortOrder；DictDataDO、DictData 和 Mapper 已同步。Menu/Role 的 BO、DO 和 Mapper 同样采用该命名。与 BaseQuery 的动态排序字段 sort 分开，未提供排序值时保留 null，不在 Java 中补数据库默认值。

旧数据库的 order 列需要迁移；代码修改不代表数据库已经更新。迁移脚本与发布兼容要求见第 16.1 节。接口输入输出的排序字段由 order 改为 sortOrder，调用端需要同步；旧缓存对象可能仍保存 order，发布时按业务缓存范围失效并重新加载。

## 7. tb_dict_data 建表草案

用途：讨论与隔离环境验证。不是实际数据库导出，不是对已有表执行的迁移，不应以此覆盖已部署结构。类型长度、非空、默认值、排序规则和索引均须按第 16、17 节验证。

```sql
CREATE TABLE `tb_dict_data` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
    `type_id`     BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
    `type_value`  VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    `name`        VARCHAR(100) NOT NULL COMMENT '字典名称',
    `value`       VARCHAR(255) NOT NULL COMMENT '字典值',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `sort_order`  INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status`      CHAR(1) NOT NULL DEFAULT 'E' COMMENT '状态：E启用，D停用',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1已删除',
    `create_by`   VARCHAR(100) NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(100) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_dict_data_type_id` (`type_id`, `is_deleted`),
    KEY `idx_dict_data_type_value_value`
        (`type_value`, `value`, `is_deleted`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '字典数据';
```

上述 CREATE 不带 DROP，不能重建已有表；也没有用 IF NOT EXISTS 隐藏表已存在但结构不一致的问题。正式初始化之前明确目标库为空及执行范围。

## 8. tb_dict_type：基本信息与建表草案

核对日期：2026-10-17。证据状态：已核对源码、调用方和现有单元测试；未读取真实表结构，未执行建表或数据库集成验证。

### 8.1 用途与代码入口

`tb_dict_type` 保存字典类型的名称、编码、备注和状态。`tb_dict_data.type_id` 在应用中关联类型 ID，`tb_dict_data.type_value` 保存类型编码；类型编码修改和类型删除由 Service 同步处理关联数据，不代表数据库已有外键或级联规则。

| 入口 | 作用 |
| --- | --- |
| [DictTypeDO](../demo-dao/src/main/java/com/example/demo/dict/dao/dataobject/DictTypeDO.java) | @TableName 指向 tb_dict_type；自增 ID、业务字段及继承的 BaseDO 字段 |
| [DictType](../demo-api/src/main/java/com/example/demo/dict/api/bo/DictType.java) | 对外业务对象；当前没有状态枚举 |
| [DictTypeMapper](../demo-dao/src/main/java/com/example/demo/dict/dao/mapper/DictTypeMapper.java) / [XML](../demo-dao/src/main/resources/mappers/DictTypeMapper.xml) | long count、列表投影、过滤、排序与固定分页 |
| [DictTypeServiceImpl](../demo-service/src/main/java/com/example/demo/dict/service/DictTypeServiceImpl.java) | CRUD、缓存、关联数据同步及事务编排 |
| [DictTypeController](../demo-web/src/main/java/com/example/demo/dict/web/DictTypeController.java) | /system/dict/type 下的 list/get/save/update/delete，要求 dict:crud 权限 |
| [DictTypeServiceImplTest](../demo-service/src/test/java/com/example/demo/dict/service/DictTypeServiceImplTest.java) | mock/spy 验证计数、转换、ID 回填、关联更新删除、缓存失效与事务 advice |

继承字段及非持久化查询参数的来源见第 6.1 节 BaseDO、BaseQuery 链接。

### 8.2 字段字典

共 10 个持久化字段。SQL 类型、长度、可空性和默认值为本页设计草案，尚未由实际 DDL 确认。

| 列名 | Java 属性 | 草案定义 | 语义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED，自增主键 | @TableId(AUTO)；整数范围及无符号仍需确认 |
| name | String name | VARCHAR(100) NOT NULL | 类型名称；列表支持包含式模糊查询 |
| value | String value | VARCHAR(100) NOT NULL | 类型编码，DO 显式反引号映射；列表模糊匹配，getType 精确匹配 |
| remark | String remark | VARCHAR(500)，允许 NULL | 类型备注 |
| status | String status | VARCHAR(32)，允许 NULL | 列表按非空状态精确过滤；当前对象未定义值域，草案暂不设置默认状态 |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | 软删除标记，来自 BaseDO；XML 正常记录条件为 0 |
| create_by | String createBy | VARCHAR(100) NOT NULL | 插入时来自可信创建人参数 |
| create_time | Date createTime | DATETIME，默认当前时间 | 来自 BaseDO；列表投影及日期过滤依赖该列 |
| update_by | String updateBy | VARCHAR(100)，允许 NULL | 更新/软删除时设置 |
| update_time | Date updateTime | DATETIME，初始化并自动更新时间 | 来自 BaseDO；默认行为是草案选择 |

不建列：BaseQuery 中 search、start、end、codes、sort、dir、pageNo、pageSize、offset，以及静态 serialVersionUID。当前 DO、BO 和 Mapper 均没有类型排序属性，不沿用字典数据表的 sort_order 列。

### 8.3 查询与写入行为

- count/list 共享 queryCondition，显式过滤 is_deleted=0。name、value 使用 WildcardTypeHandler 包含式模糊匹配；status 为精确匹配。
- start 为创建时间起始日（含），end 为结束日的次日之前（不含）；日期格式、会话时区仍需真实 MySQL 验证。
- Mapper list 按 id 降序，始终添加 LIMIT offset, pageSize；它目前没有字典数据表的可选分页判断，调用者必须提供有效分页。BaseQuery 任一分页参数为 null 时 offset 为 null，不能将此接口描述为支持不传分页查全量。
- Mapper list 只投影 id/name/value/remark/status/create_time，不包含全部审计字段。count 返回 long，XML resultType 为 java.lang.Long。
- getType 的两个重载使用 MyBatis-Plus getOne；ID 与编码同时存在时按两者共同匹配，不附加 status 条件。正常记录过滤依赖项目 MyBatis-Plus 逻辑删除配置，不由此表 XML 提供。
- getType(String, String) 先查 Redis，键为 CACHE_KEY_DICT_TYPE 加 id + "@" + value；缺失记录不缓存。BigInteger 重载直接查询数据库。按编码查到多行时，getOne 可能抛多结果异常，不能据此推断已经存在唯一键。
- insertType 设置 createBy，调用 save 后将生成的 ID 回填原 BO；方法没有逐字段校验 name/value/status，不额外设置数据库默认字段。
- updateType 使用参数 ID，检查原记录与更新结果，设置 updateBy；同一 Spring 事务方法中调用 updateData(id, type.getValue(), updateBy) 更新关联数据的 type_value。该调用即使未修改编码也会发生，子方法要求非空白 typeValue。
- deleteType 设置 is_deleted=true 与 updateBy，并调用 deleteData(id, null, updateBy) 软删除关联字典数据。此关联删除由应用实现。
- 更新和删除清理旧类型的 id@value、id@null、null@value 三类缓存。Redis 操作不受数据库事务回滚保护；并发与提交时序另行验证，不将现有清理流程描述为严格缓存一致性保证。

### 8.4 建表草案与约束依据

以下 SQL 仅供讨论和隔离验证，尚未执行。审计字段沿用字典数据草案；类型编码长度暂取 100，与子表 type_value 草案一致。状态值域缺少源码定义，暂用可空 VARCHAR(32)，不直接套用 DictData 的 E/D 枚举。正式采用前须确认必填规则、实际数据和状态协议。

```sql
CREATE TABLE `tb_dict_type` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    `value`       VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `status`      VARCHAR(32) DEFAULT NULL COMMENT '状态，值域待确认',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0正常，1已删除',
    `create_by`   VARCHAR(100) NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(100) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_dict_type_value` (`value`, `is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '字典类型';
```

候选非唯一索引 idx_dict_type_value 面向按编码精确查找与正常记录过滤，尚未通过执行计划验证；不保证加速列表的包含式 LIKE。没有依据添加业务唯一键，编码是否在未删除记录中唯一、删除后是否可重用、大小写是否敏感均待确认；布尔删除标记参与唯一键的历史冲突问题见第 14.2 节。

与 tb_dict_data 的逻辑关系为类型 ID 到数据 type_id 的一对多，type_value 为同步保存的编码。草案不添加外键；若需外键或唯一约束，先核对两表类型、排序规则、重复/孤儿数据与软删除策略。必要公共初始类型及其字典值尚未确定，不编造种子数据。

### 8.5 初始化、演进与验证

后续正式脚本规划为 sql/schema/tb_dict_type.sql；当前仅保留本文草案。两表初始化建议先类型、后数据，种子数据先取得真实生成的类型 ID 再建立关联，不假设固定自增 ID。已有库应先比对 SHOW CREATE TABLE，按第 16 节设计增量迁移，不直接执行 CREATE 替换旧表；恢复遵循同节要求。

| 验证项 | 验收内容 | 当前状态 |
| --- | --- | --- |
| 静态映射 | 5 个直接字段与 5 个继承字段，排除查询参数 | 已核对源码 |
| 建表及默认值 | 指定版本 MySQL 建表、ID 回填、审计时间与软删除默认值 | 未执行数据库验证 |
| 查询 | count/list 条件一致；模糊与精确匹配差异；日期边界；id 降序和固定分页 | 未执行数据库验证 |
| 编码与状态 | 重复编码、大小写、状态值域、停用类型的读取约定 | 待确认契约 |
| 关联变更 | 类型编码同步、类型与子数据软删除、失败时真实数据库回滚 | 未执行数据库验证 |
| 缓存与并发 | 各查询键失效、Redis 失败、事务提交时序及并发读取 | 未执行本次测试或集成验证 |
| 数据演进 | 历史重复/孤儿数据、字段长度、非空收紧、迁移与恢复 | 未核实实际数据 |

现有单元测试使用 mock Mapper 与持久化 spy，TransactionProbe 只验证事务 advice；它们不证明真实 SQL、唯一约束或数据库回滚。类型表的数据库集成验证仍未执行。

## 9. tb_login_log：基本信息与建表草案

核对日期：2026-10-17。用途：登录日志。状态：已核对源码与测试；真实 DDL 未读取，建表草案及数据库集成验证未执行。

### 9.1 代码入口

[DO](../demo-dao/src/main/java/com/example/demo/login/dao/dataobject/LoginLogDO.java)；[BO](../demo-api/src/main/java/com/example/demo/login/api/bo/LoginLog.java)；[Mapper](../demo-dao/src/main/java/com/example/demo/login/dao/mapper/LoginLogMapper.java)；[XML](../demo-dao/src/main/resources/mappers/LoginLogMapper.xml)；[Service](../demo-service/src/main/java/com/example/demo/login/service/LoginLogServiceImpl.java)；[Controller](../demo-web/src/main/java/com/example/demo/log/web/LoginLogController.java)；[测试](../demo-service/src/test/java/com/example/demo/login/service/LoginLogServiceImplTest.java)。继承字段见第 6.1 节 BaseDO/BaseQuery。

登录事件生产方：[LoginServiceImpl](../demo-service/src/main/java/com/example/demo/login/service/LoginServiceImpl.java)。

### 9.2 字段字典

共 12 列（7 个直接字段、5 个继承字段）。下列 SQL 类型、长度、非空及默认值均为草案；Java 类型、字段映射和业务枚举来源于源码。

| 数据库列 | Java 属性 | 草案定义 | 含义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED NOT NULL AUTO_INCREMENT | @TableId(AUTO)；主键范围与无符号仍需确认 |
| username | String username | VARCHAR(100) DEFAULT NULL | 登录账号；列表包含式模糊匹配 |
| ip | String ip | VARCHAR(255) DEFAULT NULL | 来源地址原文；列表精确匹配 |
| ip_addr | String ipAddr | VARCHAR(255) DEFAULT NULL | 地址描述，可为空 |
| login_time | Date loginTime | DATETIME NOT NULL | 登录事件时间，由调用方提供；与 create_time 分开 |
| status | String status | CHAR(1) NOT NULL | S 成功、F 失败；不默认成功 |
| err_msg | String errMsg | TEXT | 失败说明，可为空 |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | 软删除标记，来自 BaseDO；0 正常、1 删除 |
| create_by | String createBy | VARCHAR(100) NOT NULL | 创建人，来自可信调用参数；非空为草案约束 |
| create_time | Date createTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 记录入库时间，草案由数据库初始化 |
| update_by | String updateBy | VARCHAR(100) DEFAULT NULL | 更新人，新记录可为空 |
| update_time | Date updateTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 草案由数据库维护；非事件发生时间 |

排除：无额外业务查询列；所有 BaseQuery 查询参数和 serialVersionUID 都不建列。BaseDO 的字段写入策略不改变数据库列定义；当前没有原有的审计人 JSON 序列化排除注解。

### 9.3 查询、写入与关联行为

- Mapper count/list 共享 username、ip、status 和 login_time 日期区间条件，并过滤 is_deleted=0；结束日为小于次日，计数使用 long / java.lang.Long。
- 列表固定 LIMIT offset,pageSize，排序为 ${sort} ${dir}, id DESC。LoginLogServiceImpl 不补默认排序，也没有排序白名单；当前无界/非法分页与可控 SQL 拼接风险应单独修复，不能因文档或索引存在就认为已经解决。BaseQuery.search 虽可传递，但此 XML 没有使用它。
- getLog 使用 MyBatis-Plus getById；insertLog 返回 void，设置 createBy、save 后回填传入 BO 的 ID。该 Service 未提供日志更新、删除或清理接口。
- LoginServiceImpl.handle 构造账号、IP、loginTime、S/F 状态及错误消息并发送 login.log.save 消息；不能由生产端发送推断已完成消费落库，也不能推断消息必达/幂等。

### 9.4 建表草案与索引依据

以下仅为独立空库的创建草案，不是原库导出或现有表迁移。未添加物理外键和业务唯一键；非空/默认值均需验证。

```sql
CREATE TABLE `tb_login_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '登录日志ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '登录账号',
    `ip` VARCHAR(255) DEFAULT NULL COMMENT '来源地址原文',
    `ip_addr` VARCHAR(255) DEFAULT NULL COMMENT '地址描述，可为空',
    `login_time` DATETIME NOT NULL COMMENT '登录事件时间，由调用方提供',
    `status` CHAR(1) NOT NULL COMMENT 'S 成功、F 失败',
    `err_msg` TEXT COMMENT '失败说明，可为空',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记，来自 BaseDO',
    `create_by` VARCHAR(100) NOT NULL COMMENT '创建人，来自可信调用参数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间，草案由数据库初始化',
    `update_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人，新记录可为空',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '草案由数据库维护',
    PRIMARY KEY (`id`),
    KEY `idx_login_log_time` (`is_deleted`, `login_time`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '登录日志';
```

候选索引 idx_login_log_time 面向正常记录的事件时间区间与时间排序；不能覆盖任意动态排序，也不保证优化 username 的包含式 LIKE。事件时间不默认 CURRENT_TIMESTAMP，避免用消费/入库时间替代真实登录时间。

IP 当前直接取 X-Forwarded-For 或 remoteAddr，可能是代理链文本；VARCHAR(255) 是待验证容量选择，不宣称所有值都是单个 IPv4/IPv6 地址。账号、IP 和错误消息属于访问受控的日志信息；保留周期、归档与脱敏需明确，当前没有据此建立自动清理能力。无业务唯一键，重复投递防重策略待设计。

### 9.5 初始化、演进与验证

验证账号模糊匹配、IP/状态精确匹配、login_time 日期边界和排序白名单修复；比较事件时间与入库时间，验证 void insertLog 的 ID 回填及消息重复处理。当前未确认必要种子数据，空表应可接收新日志，不导入真实登录记录。

未来 schema 路径为 sql/schema/tb_login_log.sql，当前未创建。已有库先核对结构与数据，再按第 16 节迁移/恢复，不使用 CREATE 覆盖现有表。

| 验证层次 | 本次状态 |
| --- | --- |
| 字段映射及 SQL 引用 | 已静态核对，DO/BO/投影差异按上文记录 |
| 建表、插入、默认值及 ID 回填 | 未执行数据库验证 |
| 过滤、排序、分页与执行计划 | 未执行数据库验证 |
| 业务约束、生命周期及迁移恢复 | 待结合隔离数据库和实际契约验证 |
| 现有单元测试 | 已阅读相关测试，本次未运行；mock 不能证明真实 SQL 或回滚 |

## 10. tb_menu：基本信息与建表草案

核对日期：2026-10-17。用途：系统菜单。状态：已核对源码与测试；真实 DDL 未读取，建表草案及数据库集成验证未执行。

### 10.1 代码入口

[DO](../demo-dao/src/main/java/com/example/demo/menu/dao/dataobject/MenuDO.java)；[BO](../demo-api/src/main/java/com/example/demo/menu/api/bo/Menu.java)；[Mapper](../demo-dao/src/main/java/com/example/demo/menu/dao/mapper/MenuMapper.java)；[XML](../demo-dao/src/main/resources/mappers/MenuMapper.xml)；[Service](../demo-service/src/main/java/com/example/demo/menu/service/MenuServiceImpl.java)；[Controller](../demo-web/src/main/java/com/example/demo/menu/web/MenuController.java)；[测试](../demo-service/src/test/java/com/example/demo/menu/service/MenuServiceImplTest.java)。继承字段见第 6.1 节 BaseDO/BaseQuery。

### 10.2 字段字典

共 19 列（14 个直接字段、5 个继承字段）。下列 SQL 类型、长度、非空及默认值均为草案；Java 类型、字段映射和业务枚举来源于源码。

| 数据库列 | Java 属性 | 草案定义 | 含义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED NOT NULL AUTO_INCREMENT | @TableId(AUTO)；主键范围与无符号仍需确认 |
| pid | BigInteger pid | BIGINT UNSIGNED NOT NULL DEFAULT 0 | 父菜单 ID；树入口使用 0 表示根 |
| type | String type | CHAR(1) NOT NULL | C 目录、M 菜单、B 按钮；不指定默认类别 |
| icon | String icon | VARCHAR(255) DEFAULT NULL | 图标标识 |
| name | String name | VARCHAR(100) NOT NULL | 菜单名称；列表包含式模糊匹配 |
| path | String path | VARCHAR(500) DEFAULT NULL | 路由或外部地址 |
| component | String component | VARCHAR(255) DEFAULT NULL | 组件路径 |
| query | String query | VARCHAR(1000) DEFAULT NULL | 路由参数；DO 使用反引号映射，不按 JSON 类型推断 |
| code | String code | VARCHAR(100) DEFAULT NULL | 权限标识；唯一性未确认 |
| sort_order | Integer sortOrder | INT NOT NULL DEFAULT 0 | 排序值，越小越靠前 |
| is_external | Boolean external | TINYINT NOT NULL DEFAULT 0 | 0 非外部、1 外部 |
| is_cached | Boolean cached | TINYINT NOT NULL DEFAULT 0 | 0 不缓存、1 缓存；默认值为建议 |
| is_hidden | Boolean hidden | TINYINT NOT NULL DEFAULT 0 | 0 显示、1 隐藏 |
| status | String status | CHAR(1) NOT NULL DEFAULT 'E' | E 启用、D 停用；默认 E 是建议 |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | 软删除标记，来自 BaseDO；0 正常、1 删除 |
| create_by | String createBy | VARCHAR(100) NOT NULL | 创建人，来自可信调用参数；非空为草案约束 |
| create_time | Date createTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 记录入库时间，草案由数据库初始化 |
| update_by | String updateBy | VARCHAR(100) DEFAULT NULL | 更新人，新记录可为空 |
| update_time | Date updateTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 草案由数据库维护；非事件发生时间 |

排除：ids、types、userId、roleCodes；BO 派生的 leaf 也不建列；所有 BaseQuery 查询参数和 serialVersionUID 都不建列。BaseDO 的字段写入策略不改变数据库列定义；当前没有原有的审计人 JSON 序列化排除注解。

### 10.3 查询、写入与关联行为

- Mapper count 只统计指定 pid 的未删除记录；Mapper list 还可按 ids、pid、type/types、name、status、userId 过滤，因此不能将此 count 当成全部列表筛选条件的等价计数。
- 列表不分页，按 pid、sort_order 升序、id 降序。userId 非空时使用 EXISTS 关联 tb_role_menu、tb_user_role、tb_role，过滤三者软删除并要求角色状态 E；SQL 未额外检查用户角色 status。
- 按 type/types 查询的 Service 重载设置菜单启用状态 E；普通列表及树重载没有统一加启用状态。树从 pid=0 递归，不保证所有过滤后的节点都可连到根；当前没有写入时防环校验。
- getMenu 使用 getById。insertMenu 使用参数 pid、createBy 并回填 ID；updateMenu 使用参数 ID 与 updateBy，失败抛业务异常。
- deleteMenu 在没有未删除直接子菜单、没有未删除角色菜单关联时软删除，并检查更新结果。应用层先查再改不等于数据库并发约束，也没有数据库物理级联删除。

### 10.4 建表草案与索引依据

以下仅为独立空库的创建草案，不是原库导出或现有表迁移。未添加物理外键和业务唯一键；非空/默认值均需验证。

```sql
CREATE TABLE `tb_menu` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '系统菜单ID',
    `pid` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父菜单 ID',
    `type` CHAR(1) NOT NULL COMMENT 'C 目录、M 菜单、B 按钮',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标标识',
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '路由或外部地址',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `query` VARCHAR(1000) DEFAULT NULL COMMENT '路由参数',
    `code` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `is_external` TINYINT NOT NULL DEFAULT 0 COMMENT '0 非外部、1 外部',
    `is_cached` TINYINT NOT NULL DEFAULT 0 COMMENT '0 不缓存、1 缓存',
    `is_hidden` TINYINT NOT NULL DEFAULT 0 COMMENT '0 显示、1 隐藏',
    `status` CHAR(1) NOT NULL DEFAULT 'E' COMMENT 'E 启用、D 停用',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记，来自 BaseDO',
    `create_by` VARCHAR(100) NOT NULL COMMENT '创建人，来自可信调用参数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间，草案由数据库初始化',
    `update_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人，新记录可为空',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '草案由数据库维护',
    PRIMARY KEY (`id`),
    KEY `idx_menu_parent_sort` (`pid`, `is_deleted`, `sort_order`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统菜单';
```

候选 idx_menu_parent_sort 面向父级筛选、软删除与排序；全树和用户权限 EXISTS 的执行计划仍需验证。pid=0 是应用树的根约定，不能给 pid 直接增加要求父行存在的自引用外键。菜单路径、权限 code 是否唯一、是否允许跨父级重名均未证实，草案不增加唯一键。

is_external/is_cached/is_hidden 的 0 默认值为草案选择；需要结合客户端行为确认。Menu.getLeaf 在目录场景会读取 external，需验证数据库返回的 Boolean 非空语义。query 为现有列名，保持反引号；本次不扩展命名迁移范围。

### 10.5 初始化、演进与验证

初始化前明确最小目录/菜单/按钮、权限 code 与授权角色，使用虚构或公开必要数据；未获得菜单清单前不编造管理权限。测试根节点/多层树、孤儿节点、循环输入、类型和用户范围、排序、布尔标记、软删除阻断及并发引用创建。

未来 schema 路径为 sql/schema/tb_menu.sql，当前未创建。已有库先核对结构与数据，再按第 16 节迁移/恢复，不使用 CREATE 覆盖现有表。

| 验证层次 | 本次状态 |
| --- | --- |
| 字段映射及 SQL 引用 | 已静态核对，DO/BO/投影差异按上文记录 |
| 建表、插入、默认值及 ID 回填 | 未执行数据库验证 |
| 过滤、排序、分页与执行计划 | 未执行数据库验证 |
| 业务约束、生命周期及迁移恢复 | 待结合隔离数据库和实际契约验证 |
| 现有单元测试 | 已阅读相关测试，本次未运行；mock 不能证明真实 SQL 或回滚 |

## 11. tb_operate_log：基本信息与建表草案

核对日期：2026-10-17。用途：操作日志。状态：已核对源码与测试；真实 DDL 未读取，建表草案及数据库集成验证未执行。

### 11.1 代码入口

[DO](../demo-dao/src/main/java/com/example/demo/operate/dao/dataobject/OperateLogDO.java)；[BO](../demo-api/src/main/java/com/example/demo/operate/api/bo/OperateLog.java)；[Mapper](../demo-dao/src/main/java/com/example/demo/operate/dao/mapper/OperateLogMapper.java)；[XML](../demo-dao/src/main/resources/mappers/OperateLogMapper.xml)；[Service](../demo-service/src/main/java/com/example/demo/log/service/OperateLogServiceImpl.java)；[Controller](../demo-web/src/main/java/com/example/demo/log/web/OperateLogController.java)；[测试](../demo-service/src/test/java/com/example/demo/log/service/OperateLogServiceImplTest.java)。继承字段见第 6.1 节 BaseDO/BaseQuery。

操作事件采集方：[LogAspect](../demo-web/src/main/java/com/example/demo/framework/aspectj/LogAspect.java)。

### 11.2 字段字典

共 20 列（15 个直接字段、5 个继承字段）。下列 SQL 类型、长度、非空及默认值均为草案；Java 类型、字段映射和业务枚举来源于源码。

| 数据库列 | Java 属性 | 草案定义 | 含义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED NOT NULL AUTO_INCREMENT | @TableId(AUTO)；主键范围与无符号仍需确认 |
| operator | String operator | VARCHAR(100) DEFAULT NULL | 操作人标识；可能为用户 ID 文本或 anonymous |
| module | String module | VARCHAR(100) DEFAULT NULL | 操作模块 |
| operate_type | String operateType | VARCHAR(100) DEFAULT NULL | 操作类型/描述；当前切面取 @Log.desc，不推断数字枚举 |
| order_no | String orderNo | VARCHAR(100) DEFAULT NULL | 单据编号，仅 DO 定义；不是排序字段 |
| content | String content | TEXT | 操作内容，当前切面设置 null |
| operate_time | Date operateTime | DATETIME NOT NULL | 操作事件时间，由调用方提供 |
| clazz | String clazz | VARCHAR(255) DEFAULT NULL | 来源类标识；当前切面使用简单类名 |
| request_uri | String requestUri | VARCHAR(1024) DEFAULT NULL | 请求 URI |
| request_method | String requestMethod | VARCHAR(16) DEFAULT NULL | 请求方法 |
| request_params | String requestParams | LONGTEXT | 请求文本；不保证总是 JSON，长度上限/脱敏待完善 |
| ip | String ip | VARCHAR(255) DEFAULT NULL | 来源地址原文 |
| ip_addr | String ipAddr | VARCHAR(255) DEFAULT NULL | 地址描述，当前切面设置 null |
| status | String status | CHAR(1) NOT NULL | S 成功、F 失败；不默认成功 |
| err_msg | String errMsg | TEXT | 异常消息，可为空 |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | 软删除标记，来自 BaseDO；0 正常、1 删除 |
| create_by | String createBy | VARCHAR(100) NOT NULL | 创建人，来自可信调用参数；非空为草案约束 |
| create_time | Date createTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 记录入库时间，草案由数据库初始化 |
| update_by | String updateBy | VARCHAR(100) DEFAULT NULL | 更新人，新记录可为空 |
| update_time | Date updateTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 草案由数据库维护；非事件发生时间 |

排除：无额外业务查询列；所有 BaseQuery 查询参数和 serialVersionUID 都不建列。BaseDO 的字段写入策略不改变数据库列定义；当前没有原有的审计人 JSON 序列化排除注解。

### 11.3 查询、写入与关联行为

- Mapper count/list 按 clazz、operator 包含式匹配、status 精确匹配及 operate_time 日期区间筛选，显式过滤软删除，计数使用 long / java.lang.Long；search 不是当前 SQL 的过滤条件。
- 列表固定分页，排序直接使用 ${sort} ${dir}, id DESC；Service 在 sort 或 dir 任一为空白时统一补 id DESC，但没有白名单，传入非空值仍存在可控 SQL 拼接风险。
- listLogs 和 getLog(String) 将 operator=anonymous 显示为空字符串，getLog(BigInteger) 不做此显示转换；数据库原值不会因此更新。
- insertLog 返回 void，设置 createBy、save 后回填原 BO 的 ID。当前 Service 没有公开更新、删除或保留期清理方法。
- LogAspect 依据 @Log 组装 operateTime、clazz、请求文本和 S/F；当前发送语句被注释，不能声称注解操作日志已自动落库。requestParams 可能是 JSON、表单或查询字符串，不适合直接推导成 JSON 数据库类型。
- order_no 仅在 OperateLogDO 中存在，BO 无 orderNo 且 Mapper list 未投影该列；草案包含该列以满足 DO 的持久化映射，不宣称当前业务已填写或返回它。

### 11.4 建表草案与索引依据

以下仅为独立空库的创建草案，不是原库导出或现有表迁移。未添加物理外键和业务唯一键；非空/默认值均需验证。

```sql
CREATE TABLE `tb_operate_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
    `operator` VARCHAR(100) DEFAULT NULL COMMENT '操作人标识',
    `module` VARCHAR(100) DEFAULT NULL COMMENT '操作模块',
    `operate_type` VARCHAR(100) DEFAULT NULL COMMENT '操作类型/描述',
    `order_no` VARCHAR(100) DEFAULT NULL COMMENT '单据编号，仅 DO 定义',
    `content` TEXT COMMENT '操作内容，当前切面设置 null',
    `operate_time` DATETIME NOT NULL COMMENT '操作事件时间，由调用方提供',
    `clazz` VARCHAR(255) DEFAULT NULL COMMENT '来源类标识',
    `request_uri` VARCHAR(1024) DEFAULT NULL COMMENT '请求 URI',
    `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
    `request_params` LONGTEXT COMMENT '请求文本',
    `ip` VARCHAR(255) DEFAULT NULL COMMENT '来源地址原文',
    `ip_addr` VARCHAR(255) DEFAULT NULL COMMENT '地址描述，当前切面设置 null',
    `status` CHAR(1) NOT NULL COMMENT 'S 成功、F 失败',
    `err_msg` TEXT COMMENT '异常消息，可为空',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记，来自 BaseDO',
    `create_by` VARCHAR(100) NOT NULL COMMENT '创建人，来自可信调用参数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间，草案由数据库初始化',
    `update_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人，新记录可为空',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '草案由数据库维护',
    PRIMARY KEY (`id`),
    KEY `idx_operate_log_time` (`is_deleted`, `operate_time`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '操作日志';
```

候选 idx_operate_log_time 面向事件时间筛选和时间排序，不能保证优化所有动态列排序及包含式 LIKE。大文本容量、写入频率和保留期需要真实规模验证，不能凭 LONGTEXT 无限接收客户端正文。

请求参数和 err_msg 可能包含凭据或个人信息；当前切面没有统一字段脱敏。正式启用落库前需要明确定向过滤/脱敏与容量策略，而非将原始请求作为种子数据。operator 是字符串标识，不从字段名推断强制用户外键；order_no 也不推导到任何订单表。

### 11.5 初始化、演进与验证

验证 operate_time 与 create_time 区别、长文本/非 JSON 请求内容、S/F 状态、匿名显示重载差异、order_no 的 DO/BO 不对称，以及日志采集到落库的实际链路。暂无必要公共种子数据，保留期及删除授权待明确。

未来 schema 路径为 sql/schema/tb_operate_log.sql，当前未创建。已有库先核对结构与数据，再按第 16 节迁移/恢复，不使用 CREATE 覆盖现有表。

| 验证层次 | 本次状态 |
| --- | --- |
| 字段映射及 SQL 引用 | 已静态核对，DO/BO/投影差异按上文记录 |
| 建表、插入、默认值及 ID 回填 | 未执行数据库验证 |
| 过滤、排序、分页与执行计划 | 未执行数据库验证 |
| 业务约束、生命周期及迁移恢复 | 待结合隔离数据库和实际契约验证 |
| 现有单元测试 | 已阅读相关测试，本次未运行；mock 不能证明真实 SQL 或回滚 |

## 12. tb_role：基本信息与建表草案

核对日期：2026-10-17。用途：系统角色。状态：已核对源码与测试；真实 DDL 未读取，建表草案及数据库集成验证未执行。

### 12.1 代码入口

[DO](../demo-dao/src/main/java/com/example/demo/role/dao/dataobject/RoleDO.java)；[BO](../demo-api/src/main/java/com/example/demo/role/api/bo/Role.java)；[Mapper](../demo-dao/src/main/java/com/example/demo/role/dao/mapper/RoleMapper.java)；[XML](../demo-dao/src/main/resources/mappers/RoleMapper.xml)；[Service](../demo-service/src/main/java/com/example/demo/role/service/RoleServiceImpl.java)；[Controller](../demo-web/src/main/java/com/example/demo/role/web/RoleController.java)；[测试](../demo-service/src/test/java/com/example/demo/role/service/RoleServiceImplTest.java)。继承字段见第 6.1 节 BaseDO/BaseQuery。

### 12.2 字段字典

共 11 列（6 个直接字段、5 个继承字段）。下列 SQL 类型、长度、非空及默认值均为草案；Java 类型、字段映射和业务枚举来源于源码。

| 数据库列 | Java 属性 | 草案定义 | 含义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED NOT NULL AUTO_INCREMENT | @TableId(AUTO)；主键范围与无符号仍需确认 |
| code | String code | VARCHAR(100) NOT NULL | 角色编码，getRoleId 精确查询，列表模糊查询 |
| name | String name | VARCHAR(100) NOT NULL | 角色名称 |
| remark | String remark | VARCHAR(500) DEFAULT NULL | 备注 |
| sort_order | Integer sortOrder | INT NOT NULL DEFAULT 0 | 排序值，越小越靠前 |
| status | String status | CHAR(1) NOT NULL DEFAULT 'E' | E 启用、D 停用；默认 E 是建议 |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | 软删除标记，来自 BaseDO；0 正常、1 删除 |
| create_by | String createBy | VARCHAR(100) NOT NULL | 创建人，来自可信调用参数；非空为草案约束 |
| create_time | Date createTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 记录入库时间，草案由数据库初始化 |
| update_by | String updateBy | VARCHAR(100) DEFAULT NULL | 更新人，新记录可为空 |
| update_time | Date updateTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 草案由数据库维护；非事件发生时间 |

排除：BO 的 menuIds；所有 BaseQuery 查询参数和 serialVersionUID 都不建列。BaseDO 的字段写入策略不改变数据库列定义；当前没有原有的审计人 JSON 序列化排除注解。

### 12.3 查询、写入与关联行为

- Mapper count/list 共享 code、name、status、create_time 区间过滤及软删除条件；count 另有 id 条件，list 不按 id 过滤。code/name 为包含式 LIKE。
- Mapper list 按 sort_order 升序、id 降序；仅正数 pageNo/pageSize 同时存在才分页。Service 的无参重载查询全部业务列表；按 ID 集合重载使用 listByIds，不保证输入顺序或 XML 排序。
- getRole(String) 直接 getById；getRole(BigInteger) 使用 CACHE_KEY_ROLE + id 缓存，数据库缺失时抛“角色不存在”。getRoleId(code) 用 getOne 精确匹配编码，不按启用状态筛选。
- insertRole 在事务内保存、回填 ID、同步角色菜单；捕获 DuplicateKeyException 转为“编号已存在”，但捕获异常不能证明原库存在 code 唯一键，也可能包含其他唯一约束冲突。
- updateRole(完整对象) 在事务内更新角色、同步菜单并清理缓存；状态重载更新状态并清理缓存。deleteRole 拒绝仍关联用户的角色，成功软删除后按需软删除角色菜单并清理缓存。
- Redis 失效不受数据库回滚保护；角色状态变更不等于所有已缓存用户权限已经刷新，权限刷新范围需结合用户角色与 Token 流程验证。

### 12.4 建表草案与索引依据

以下仅为独立空库的创建草案，不是原库导出或现有表迁移。未添加物理外键和业务唯一键；非空/默认值均需验证。

```sql
CREATE TABLE `tb_role` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '系统角色ID',
    `code` VARCHAR(100) NOT NULL COMMENT '角色编码，getRoleId 精确查询，列表模糊查询',
    `name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status` CHAR(1) NOT NULL DEFAULT 'E' COMMENT 'E 启用、D 停用',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记，来自 BaseDO',
    `create_by` VARCHAR(100) NOT NULL COMMENT '创建人，来自可信调用参数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间，草案由数据库初始化',
    `update_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人，新记录可为空',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '草案由数据库维护',
    PRIMARY KEY (`id`),
    KEY `idx_role_code` (`code`, `is_deleted`),
    KEY `idx_role_sort` (`is_deleted`, `sort_order`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统角色';
```

idx_role_code 是按编码精确查询的候选非唯一索引；idx_role_sort 面向正常记录排序。getRoleId 使用 getOne 对活动编码有单条结果预期，但真实唯一规则未知，草案不臆造唯一约束。

需明确 code 是否全局唯一、软删除后能否重用、大小写是否敏感，再选择约束策略。不能直接使用 UNIQUE(code,is_deleted) 假定支持无限次删除后重建。menuIds 是请求中的授权集合，不是 tb_role 列；角色、菜单与用户关系通过独立关联表维护。

### 12.5 初始化、演进与验证

若需要管理角色初始数据，先确定编码、权限菜单和最小授权范围，不生成默认管理员账号或口令。验证编码重复/大小写策略、可选分页、批量 ID 顺序语义、排序、状态、关联菜单差异更新、关联用户阻断删除、事务失败与缓存时序。

未来 schema 路径为 sql/schema/tb_role.sql，当前未创建。已有库先核对结构与数据，再按第 16 节迁移/恢复，不使用 CREATE 覆盖现有表。

| 验证层次 | 本次状态 |
| --- | --- |
| 字段映射及 SQL 引用 | 已静态核对，DO/BO/投影差异按上文记录 |
| 建表、插入、默认值及 ID 回填 | 未执行数据库验证 |
| 过滤、排序、分页与执行计划 | 未执行数据库验证 |
| 业务约束、生命周期及迁移恢复 | 待结合隔离数据库和实际契约验证 |
| 现有单元测试 | 已阅读相关测试，本次未运行；mock 不能证明真实 SQL 或回滚 |

## 13. tb_role_menu：基本信息与建表草案

核对日期：2026-10-17。用途：角色菜单关系。状态：已核对源码与测试；真实 DDL 未读取，建表草案及数据库集成验证未执行。

### 13.1 代码入口

[DO](../demo-dao/src/main/java/com/example/demo/role/dao/dataobject/RoleMenuDO.java)；[BO](../demo-api/src/main/java/com/example/demo/role/api/bo/RoleMenu.java)；[Mapper](../demo-dao/src/main/java/com/example/demo/role/dao/mapper/RoleMenuMapper.java)；[XML](../demo-dao/src/main/resources/mappers/RoleMenuMapper.xml)；[Service](../demo-service/src/main/java/com/example/demo/role/service/RoleMenuServiceImpl.java)；[Controller](../demo-web/src/main/java/com/example/demo/role/web/RoleMenuController.java)；[测试](../demo-service/src/test/java/com/example/demo/role/service/RoleMenuServiceImplTest.java)。继承字段见第 6.1 节 BaseDO/BaseQuery。

### 13.2 字段字典

共 8 列（3 个直接字段、5 个继承字段）。下列 SQL 类型、长度、非空及默认值均为草案；Java 类型、字段映射和业务枚举来源于源码。

| 数据库列 | Java 属性 | 草案定义 | 含义与证据 |
| --- | --- | --- | --- |
| id | BigInteger id | BIGINT UNSIGNED NOT NULL AUTO_INCREMENT | @TableId(AUTO)；主键范围与无符号仍需确认 |
| role_id | BigInteger roleId | BIGINT UNSIGNED NOT NULL | 角色 ID，应用关联 tb_role.id |
| menu_id | BigInteger menuId | BIGINT UNSIGNED NOT NULL | 菜单 ID，应用关联 tb_menu.id |
| is_deleted | Boolean deleted | TINYINT NOT NULL DEFAULT 0 | 软删除标记，来自 BaseDO；0 正常、1 删除 |
| create_by | String createBy | VARCHAR(100) NOT NULL | 创建人，来自可信调用参数；非空为草案约束 |
| create_time | Date createTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 记录入库时间，草案由数据库初始化 |
| update_by | String updateBy | VARCHAR(100) DEFAULT NULL | 更新人，新记录可为空 |
| update_time | Date updateTime | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 草案由数据库维护；非事件发生时间 |

排除：DO 的 ids；所有 BaseQuery 查询参数和 serialVersionUID 都不建列。BaseDO 的字段写入策略不改变数据库列定义；当前没有原有的审计人 JSON 序列化排除注解。

### 13.3 查询、写入与关联行为

- Mapper count 按 menu_id 统计未删除关系；Mapper list 按 role_id 查询未删除关系，只投影 id/role_id/menu_id，无分页、无 ORDER BY。不同方法服务于不同范围，不能用同一计数证明任意列表总量。
- String roleId 重载返回 menuId 列表，沿用 Mapper 返回顺序，没有额外排序或去重保证。
- updateRoleMenus 为事务方法：保留已有选择，saveBatch 新增关系，将取消选择的关系按可信 roleId 与记录 IDs 软删除；null 或空 menuIds 表示清空选择，不是忽略更新。
- 批量插入设置 roleId/createBy，并从保存后的 DO 转换返回，初次插入路径可返回生成 ID；已有关系的差异更新路径返回新构造的输入列表，不能保证每个返回元素都带关系 ID。
- deleteRoleMenu 返回 void，至少需要 roleId/menuId 之一，同时存在时保留两者条件，仅设置软删除与 updateBy。按当前幂等删除约定，update 返回 false（零影响行）仍正常返回，不要求调用方检查结果；持久化层抛出的异常继续向上传播。
- 本 Service 未逐个验证角色/菜单存在性、输入重复或空元素，也未单独刷新角色缓存；不能假定所有调用路径都自动解决这些约束。主要写入方是 RoleService，RoleMenuController 当前提供读取入口。

### 13.4 建表草案与索引依据

以下仅为独立空库的创建草案，不是原库导出或现有表迁移。未添加物理外键和业务唯一键；非空/默认值均需验证。

```sql
CREATE TABLE `tb_role_menu` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色菜单关系ID',
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色 ID，应用关联 tb_role.id',
    `menu_id` BIGINT UNSIGNED NOT NULL COMMENT '菜单 ID，应用关联 tb_menu.id',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记，来自 BaseDO',
    `create_by` VARCHAR(100) NOT NULL COMMENT '创建人，来自可信调用参数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录入库时间，草案由数据库初始化',
    `update_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人，新记录可为空',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '草案由数据库维护',
    PRIMARY KEY (`id`),
    KEY `idx_role_menu_role` (`role_id`, `is_deleted`, `menu_id`),
    KEY `idx_role_menu_menu` (`menu_id`, `is_deleted`, `role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '角色菜单关系';
```

这是角色到菜单的多对多关系表；物理外键与业务唯一约束未确认。两个候选非唯一索引分别面向角色菜单读取/权限 EXISTS 和按菜单检查引用。授权按未删除关系解释，应用层差异计算不能替代并发防重。

先明确同一角色菜单是否仅允许一条有效关系、输入是否去重、删除后重建及历史保留方式，再加唯一约束。UNIQUE(role_id,menu_id,is_deleted) 可能使多条已删除历史冲突，因此草案不直接加入。关联记录没有 status 列，不添加角色或菜单的状态属性。

### 13.5 初始化、演进与验证

种子关系需要先存在有效角色与菜单，并以实际生成 ID 建立关联；初始化顺序不按文档字母排序。验证新增/保留/取消/清空授权、重复与空 ID、批量生成键、角色范围条件、重复授权并发、软删除后重新授权、权限 EXISTS 的多表软删除条件及失败回滚。

未来 schema 路径为 sql/schema/tb_role_menu.sql，当前未创建。已有库先核对结构与数据，再按第 16 节迁移/恢复，不使用 CREATE 覆盖现有表。

| 验证层次 | 本次状态 |
| --- | --- |
| 字段映射及 SQL 引用 | 已静态核对，DO/BO/投影差异按上文记录 |
| 建表、插入、默认值及 ID 回填 | 未执行数据库验证 |
| 过滤、排序、分页与执行计划 | 未执行数据库验证 |
| 业务约束、生命周期及迁移恢复 | 待结合隔离数据库和实际契约验证 |
| 现有单元测试 | 已阅读相关测试，本次未运行；mock 不能证明真实 SQL 或回滚 |

## 14. 约束、索引和默认值的设计边界

### 14.1 默认值和空值

BaseDO 的 createTime/updateTime 已同时配置 insertStrategy=NEVER、updateStrategy=NEVER，updateBy 配置 insertStrategy=NEVER，createBy 配置 updateStrategy=NEVER。普通实体插入不会提交这两个时间列和 update_by，普通实体更新不会提交创建人和这两个时间列。对应时间默认值及自动更新时间必须由实际数据库机制支持；本页 DDL 仍为草案，本次没有修改或验证真实表结构。手写 SQL 和 Wrapper 显式 SET 的规则另见 [持久化规则](persistence.md#basedo-插入与更新策略)。

tb_dict_data 草案设置 is_deleted=0、sort_order=0、status='E'；tb_dict_type 草案设置 is_deleted=0，暂不设置默认状态。两表 create_time/update_time 初始化为数据库当前时间。自动更新时间不是完整审计轨迹，也不保证每次无实际变化的 UPDATE 都改变时间。

非空不等于非空白：VARCHAR NOT NULL 仍可能接收空字符串。数据有效性需要业务校验与数据库约束共同明确。数据库默认值通常作用于列被省略或使用 DEFAULT 的情况，不能假设显式传 NULL 就自动采用默认值。

DATETIME 精度暂为秒；若需毫秒，应同步 JDBC、Java、JSON 和测试契约。排序规则会影响大小写、重音及比较结果；类型编码和值是否区分大小写尚未确定，不能将草案规则视为原库事实。

### 14.2 唯一性与关系

不根据 getData 的 LIMIT 1 推导业务唯一性。需要明确同一类型下字典值是否允许重复，以及停用、软删除后是否允许重新创建。

不要直接假定 UNIQUE(type_value, value, is_deleted) 能支持无限次删除后重建：布尔删除标记可能使多条历史删除记录冲突。若需要唯一性，先选择业务历史保留策略，再设计约束并清理已有重复数据。

type_id 表示应用关联；物理外键尚未确认。后续设计外键前核对关联主键类型、历史孤儿数据、软删除与应用级关联删除。此处不创建关联表、不添加外键或级联删除。

### 14.3 索引依据

- idx_dict_data_type_id 面向按类型 ID 加软删除条件的查询。
- idx_dict_data_type_value_value 面向类型编码、字典值的精确查找。
- 以上仅为候选索引，不保证覆盖全部筛选或 ORDER BY；列表排序是否额外排序需查看执行计划。
- 当前 name 为包含式模糊匹配，不以简单增加 name 索引承诺消除扫描。
- 不为每个字段单独加索引；结合基数、查询频率、数据量、写入成本和慢查询再调整。

## 15. 脚本组织与初始化规划

以下是建议组织，schema/seeds 尚未创建；排序列迁移曾规划为下列文件，但本次核对该文件缺失，不能按已提供脚本执行：

```text
sql/
  schema/
    tb_dict_data.sql
    tb_dict_type.sql
    tb_login_log.sql
    tb_menu.sql
    tb_operate_log.sql
    tb_role.sql
    tb_role_menu.sql
  migrations/
    20261017_rename_order_to_sort_order.sql
  seeds/
    README.md
```

schema 表示经确认的创建基线；migrations 表示已有结构到新结构的增量步骤；seeds 仅保存明确必要的公共字典初始数据。测试数据使用虚构内容，不导入真实账号、用户或支付数据。

本次草案经验证后再提取 schema 文件。初始化文档需记录目标 MySQL 版本、空库前提、执行顺序、必要种子数据及预期结果。单表脚本不代表整个 demo 可以从空库启动。

若未来引入 Flyway/Liquibase，需单独决定版本编号、已有库基线和历史记录策略；不将当前建议目录视为迁移工具已经识别的目录。不要同时维护两套互不关联的迁移历史。

## 16. 变更、迁移与恢复要求

每项数据库变更记录：原因、目标环境、旧结构证据、新结构、影响行数/规模、兼容窗口、执行顺序、验证和回退边界。

1. 确认真实结构和数据分布；新增 NOT NULL、长度收紧、唯一键前检查历史数据。
2. 根据业务选择分阶段新增字段、兼容读写、回填、切换和清理；不能把 Java 重命名当成数据库字段已经迁移。
3. 评估执行期间的锁、耗时、磁盘与并发写入；不凭 ALTER 文本承诺无锁或瞬间完成。
4. 在隔离环境演练正常升级与中途失败，记录可重入条件；已执行脚本保留历史，后续修正用新迁移。
5. 明确恢复方式：备份恢复、反向迁移或前向修复，注明数据可能丢失/不可逆的部分。不能假设 Spring 事务可回滚 DDL，也不能认为代码回退会自动回退 schema。
6. 部署时记录脚本版本/校验信息、执行人、时间、结果及应用版本；真实操作按明确授权范围进行。

恢复目标、备份频率、保留天数和演练频率由实际业务要求确定，本页不虚构已存在的备份或 SLA。应在独立环境验证恢复过程，不能只以“备份任务成功”证明可恢复。

### 16.1 排序列重命名（尚未执行）

计划路径：sql/migrations/20261017_rename_order_to_sort_order.sql；本次核对文件缺失，以下保留迁移设计，不表示已有可执行脚本。范围为 tb_menu、tb_role、tb_dict_data，只重命名列，不改排序值、类型、默认值或索引定义。使用 MySQL 8.0+ 的 RENAME COLUMN；实际版本和结构未核实，低版本不能直接执行。[MySQL ALTER TABLE 文档](https://dev.mysql.com/doc/refman/8.0/en/alter-table.html)

执行前确认三表均存在 order 且不存在 sort_order，留存 SHOW CREATE TABLE 与备份，检查视图、存储程序、外部 SQL 和客户端字段引用。执行顺序：停止旧应用及写入方 → 按脚本迁移 → 发布新应用和调用端 → 按业务范围失效菜单/角色/字典缓存 → 验证排序读写与原数据一致。不支持新旧应用同时使用两套列名滚动运行。

每条 ALTER 独立执行，三表迁移不构成整体事务；中途失败先检查每张表当前列名，再选择继续未完成项或反向重命名。回退时停止新应用，对已迁移表执行 RENAME COLUMN sort_order TO order，再恢复旧应用及调用端并刷新相关缓存。恢复脚本后应核对反向语句，不能不检查状态就整批重跑。

数据库集成验证需覆盖迁移前后行数/排序值、三张表的查询与排序、MyBatis-Plus 插入更新、逻辑删除和数据库默认值。本次没有连接数据库执行该脚本。

## 17. tb_dict_data 验证清单

| 验证层次 | 需要验证的场景 | 当前状态 |
| --- | --- | --- |
| 静态映射 | DO/父类列、非持久化字段排除、Mapper 引用列 | 已阅读，sort_order 的 DO/BO/XML 已同步；数据库未验证 |
| DDL 执行 | 在指定版本隔离 MySQL 成功建表，检查 SHOW CREATE TABLE | 未执行 |
| 插入与默认值 | 自增 ID 回填、删除标记、排序/状态、时间默认值、未提供更新人 | 未执行数据库验证 |
| 字段边界 | 中文、特殊字符、长度边界、空值/空白、状态非法值 | 待明确约束并验证 |
| 查询 | 按类型 ID/编码/编码集合、名称、状态筛选，count/list 一致 | 需真实 Mapper 与数据库验证 |
| 排序分页 | type_id/sort_order/id 排序、可选分页、同值稳定性 | 未执行数据库验证 |
| 更新删除 | 审计字段、类型归属、软删除后不可见、关联数据生命周期 | 未执行数据库验证 |
| 兼容与索引 | 原库结构差异、已有数据可迁移、代表性查询计划 | 未核实 |
| 缓存边界 | 数据变更后的缓存失效，失败和事务时序 | 结合 Service 测试另行验证 |

验证采用隔离 MySQL，使用真实 MyBatis-Plus 配置和 Mapper。mock Mapper 只能证明调用参数及业务分支，不能证明表结构、SQL、默认值、约束或事务回滚正确。H2 的通过不自动证明 MySQL 行为等价。

本次未执行建表、应用启动或真实平台访问。未来数据库验证报告写明数据库版本、脚本版本、执行步骤、结果和未覆盖项，不能以代码测试替代本节数据库验证。

## 18. 当前待确认事项

| 事项 | 为什么需要确认 | 当前处理 |
| --- | --- | --- |
| 原库真实 DDL | 无法从 DO 还原长度/约束/引擎 | 使用明确标记的草案 |
| 排序列迁移 | 代码已使用 sort_order，原库结构未读取 | 迁移设计保留，原脚本文件缺失且未执行 |
| 类型和值的唯一性 | 决定重复数据与删除后重建策略 | 不添加业务唯一键 |
| typeValue/name/value 的必填规则 | 草案非空比当前 Service 字段校验更严格 | 正式采用前同步业务验收 |
| 字典类型状态与编码约束 | DictType 没有状态枚举，按编码 getOne 需要明确重复记录处理 | 状态值域、默认值及编码唯一性待确认，见第 8 节 |
| 日志排序与留存 | 两类日志动态排序未设白名单，未建设自动归档/清理 | 仅记录源码现状，后续专项修复 |
| 角色与授权唯一性 | 角色编码和角色菜单有效关系需要明确重复/历史策略 | 草案仅建非唯一候选索引 |
| 操作日志 order_no | DO 有字段，BO 和列表无对应属性/投影 | 保留列草案，业务用途待确认 |
| 时间默认值与精度 | 影响新记录和更新行为 | 默认值标记为建议 |
| 字符串长度与排序规则 | 影响截断、大小写和查询结果 | 核对实际数据/协议后定稿 |
| 索引效果与关联约束 | 需实际规模及相关结构证据 | 候选索引，无外键 |
| 可重建数据库与集成环境 | 当前缺少完整基线 | 后续按表补齐，当前只收录本文七张表 |

## 19. Codex 修改数据库相关代码的流程

开始时读取本页、持久化规则、目标 DO 的父类、Mapper、Service、调用方和测试；区分当前实例结构、应用预期和推导草案。复杂迁移按 [PLANS.md](../PLANS.md) 规划，长期选型记录 ADR。

涉及结构变化时同步字段字典、正式脚本（若存在）、对象和 SQL 映射、消费者与测试。不要仅修改 DO 后宣称数据库已经更新，也不要因某字段未在 DO 出现就删除 Mapper 仍依赖的列。

交付分别说明：脚本已生成、静态检查已完成、隔离验证已完成、指定环境已执行。缺少数据库证据时准确说明范围；只涉及文档时验证链接、代码块、源码对照和 diff 即可。

## 20. 维护与完成标准

结构变更作者维护脚本和本页，审查者核对源码、真实结构证据及验证结果。记录维护日期和相应环境；新增表先限定任务范围，不自动推导全库。

完成一个正式表结构条目至少需要：字段映射齐全、默认值和约束有依据、SQL 可执行且已隔离验证、业务查询覆盖、迁移/回退说明明确、相关文档一致。当前七张表均仅达到源码分析与草案阶段。

OpenAI 的工作流教程强调明确目标、成功条件、约束和仍未确定的问题；本文用证据状态和验证清单落实这些原则，不把教程示例当成数据库设计标准。[OpenAI 开发工作流教程](https://developers.openai.com/cookbook/examples/codex/iterating-development-workflows-with-codex)
