# 持久化、CRUD、SQL 与事务

状态：项目规则与源码现状。维护于 2026-10-22。修改 Service/DO/Mapper 前必读。数据库 DDL、默认值、约束及隔离级别须结合实际数据库确认，不能从 Java 类推断完整 schema。

## Service 与 CRUD 规则

- 常规 CRUD 参考 `BannerServiceImpl`，同时保留目标业务特有的归属、状态、权限及异常处理。
- 保存时设置必要业务字段与 `createBy`；对已由 MySQL 默认值管理的 `deleted/createTime` 等不重复赋值。数据库 DDL 未确认时先核对，不能假设每张表都有相同默认值。
- BO 需要保存后的 ID 时，在 `save` 后从 DO 回填；不能把返回了原 BO 误认为 ID 已自动同步。`LoginLogService.insertLog` 与 `OperateLogService.insertLog` 当前返回 **void**，仍回填传入对象 ID。
- 审计命名统一：Java `createBy/updateBy`，数据库 `create_by/update_by`。不要再次引入本项目旧字段 `creator/modifier`；第三方协议中的 `creatorId/modifierId` 不属于此次命名规则。
- 普通保存无需包裹通用 `try/catch`；数据库异常交由既有异常处理。确需将唯一键冲突转换为业务提示时捕获 `DuplicateKeyException` 等具体异常；外部 SDK 边界的异常转换按 Manager 契约处理。
- 更新以方法参数中的可信 ID/业务范围为准，审计人来自可信调用上下文。禁止生成没有有效 WHERE 限制的更新/删除。
- `updateById` / `update` 返回 false 时，按已有业务约定处理未更新记录，不能无条件返回成功；批量或幂等操作的零影响行是否正常应单独判断。
- 软删除通常设置 `deleted=true` 和 `updateBy`，保留原范围条件。不要用物理删除替换；也不要在带 `@TableLogic`/全局逻辑删除配置的实体上假设任意自定义 XML 自动过滤删除数据。
- 能用 MyBatis-Plus `getById` / lambda wrapper 表达且语义一致的简单操作可复用；涉及 join、归属过滤、特别排序或聚合时保留自定义 SQL。
- 多条同类独立插入可先构造 DO 集合再 `saveBatch`，处理空集合、批量大小、事务失败以及需要时的 ID 回填映射。不能将依赖前一条生成 ID、不同实体或交错外部副作用的循环机械批量化。
- `ChatServiceImpl.saveOrUpdate(userId, friendId, chat)` 先按业务键执行自定义 `updateChat`，影响行数不为 1 时才插入；不能改成按主键的通用 `saveOrUpdate` 而改变原语义。并发幂等、唯一约束和影响行数约定需另行验证。
- 多表/多步骤原子变更在 Service 层设置合适事务边界；维持现有回滚语义，检查受检异常、自调用、异步线程和代理绕过。
- 数据库事务不能回滚 Redis、MQ、HTTP 或支付操作。需要提交后执行的动作可考虑 `TransactionUtil.afterCommit`：要求活动事务，同步回调，失败时数据库已经提交，既不持久化也不自动重试。可靠投递需求应另行设计 outbox/补偿和幂等。
- 改动角色/菜单/用户关系时检查权限刷新、令牌失效、缓存清理和关联记录生命周期，不能只修改数据库单行。

## MyBatis 与数据库规则

- DO 使用 `@TableName` / `@TableField` 映射，继承 `BaseDO` 的实体具有审计/软删除字段；查询附加属性在 `BaseQuery` 中使用 `@TableField(exist=false)`。
- 业务排序列使用 `sort_order`，Java 属性使用可空 `Integer sortOrder`；不使用保留字 `order`，也不复用表示动态排序字段的 `sort`。旧列迁移见 [数据库指南](database.md)。
- 当前应用配置 `id-type=AUTO`、`logic-delete-field=deleted`、删除值 1/正常值 0、下划线转驼峰。改用手工 MyBatis 配置或独立测试环境时需显式核对这些行为。
- 计数 API、实现、Mapper 方法、Controller 局部变量使用 `long`；XML `COUNT` 的 `resultType` 使用 `java.lang.Long`。不要与 insert/update/delete 的影响行数 `int` 混淆。
- `pageNo/pageSize` 使用可空 `Long`，页码从 1 开始；`BaseQuery.getOffset()` 在任一参数为 null 时返回 null，否则计算 `(pageNo - 1) * pageSize`。
- 目前 Role、DictData 的可选分页使用以下条件；需要“未传分页则返回完整业务列表”的接口遵循该模式，不硬编码 99/999 条伪装全量：

```xml
<if test="pageNo != null and pageNo > 0 and pageSize != null and pageSize > 0">
	LIMIT #{offset}, #{pageSize}
</if>
```

- 其他 Mapper 尚存在必须分页的查询；不得全库无差别切换。公开列表接口仍应按需求验证页码、页大小上限及偏移溢出，避免非法分页绕过 LIMIT 造成无界查询。可选分页与公开端点的输入约束分别处理。
- count/list 相同过滤条件提取到局部 `<sql id="queryCondition">` 并通过 `<include>` 复用，保留 SQL 表别名与参数作用域；不要把分页/排序加入 count。
- 默认保留 list/get 各自 SELECT 字段，项目已选择不为这两者抽取公共 SELECT 列清单。确有新需求再调整，避免因共用投影改变返回字段。
- 普通 SQL 文本不使用不必要的 CDATA；`<`、`&` 等 XML 特殊字符需要转义或局部 CDATA。MyBatis 的 `<if>/<foreach>` 等动态节点不能放进 CDATA。
- Mapper 格式遵循周边文件：普通 `<if>` 中 SQL 文本比 `<if>` 多一级 Tab；已有单行 `<![CDATA[ AND ... ]]>` 保持其特殊布局，不因统一缩进额外右移。SELECT 列、WHERE/ORDER BY 对齐保持一致。
- 值绑定使用 `#{}`，不拼接用户值。动态列名/排序方向不能通过 `#{}` 代替标识符；应使用固定映射/白名单或 `<choose>`。现有日志 Mapper 的 `${sort} ${dir}` 是需要特别审查的点，非空检查和 ASC/DESC 文本转换并不能防止注入。
- 模糊查询沿用 `WildcardTypeHandler` 的约定，核对转义与前后通配符语义；IN 集合处理空值/空集合，避免 SQL 语法错误或删除范围意外扩大。
- 手写 SQL 显式保留 `is_deleted`、用户/企业范围、状态及 join 过滤条件。检索后再更新不能替代更新语句中的数据范围限制。
- 日期边界、时区、排序稳定性、分页重复/遗漏均属于行为；优化应验证。性能问题先检查调用次数、索引和执行计划，再选择批量查询、索引、分页或缓存，避免无证据重写。
- 每个自定义 Mapper 方法应有对应 XML/注解/注入器映射，检查 namespace、id、参数名、type alias、resultType 和资源打包位置。仅 mock Mapper 的测试无法发现 SQL 未绑定。

### 数据库变更的交付要求（新增开发规范）

当前仓库没有完整的版本化 DDL 或 Flyway/Liquibase 配置。涉及表结构、字段重命名、唯一索引、默认值时：

1. 根据实际目标数据库版本及现有结构编写迁移 SQL，建议放 `sql/migrations/YYYYMMDD_description.sql`；首次需要时创建目录。
2. 说明执行前提、数据回填、兼容窗口、锁表/索引影响和回退办法。区别“查询生成 ALTER 文本”与“实际执行 ALTER”。
3. 同步 Java、Mapper、序列化、测试及受影响消费者，评估滚动发布时新旧代码共存。
4. 在隔离数据库验证；未获真实环境操作授权不得执行迁移。代码编译通过不能证明线上表结构已更新。

## 核对实际映射

[BaseDO](../demo-dao/src/main/java/com/example/demo/framework/dataobject/BaseDO.java) 的 deleted 映射 is_deleted，审计字段按下划线映射为 create_by/update_by。主键与 TableName 在各业务 DO 中定义，不是 BaseDO 自动定义所有主键。

### BaseDO 插入与更新策略

2026-10-22 核对，当前继承字段策略如下。表中“跟随全局”表示该方向没有在字段上显式配置，不代表必定写入或必定忽略 null。

| Java 属性 / 数据库列 | insertStrategy | updateStrategy | 对实体自动生成 SQL 的影响 |
| --- | --- | --- | --- |
| deleted / is_deleted | 跟随全局 | 跟随全局 | 本次未增加写入策略，逻辑删除另受应用配置及具体方法影响 |
| createBy / create_by | 跟随全局 | NEVER | 创建时按全局规则写入；实体更新不覆盖创建人 |
| createTime / create_time | NEVER | NEVER | 插入和更新均省略该字段，即使 DO 中已赋值 |
| updateBy / update_by | NEVER | 跟随全局 | 插入不写更新人；更新按全局规则处理，调用方仍需提供可信审计人 |
| updateTime / update_time | NEVER | NEVER | 插入和更新均省略该字段，即使 DO 中已赋值 |

使用范围与约束：

- NEVER 表示从对应方向的实体字段 SQL 中排除，不是“仅在值为 null 时忽略”。当前 BaseDO 没有配置 FieldFill，也未发现项目 MetaObjectHandler，不能宣称由 MyBatis-Plus 自动补齐时间或审计人。
- 通常的 save/saveBatch、updateById，以及 update(entity, wrapper) 中实体生成的 SET 部分按字段策略处理。不同方向彼此独立，insertStrategy 不限制查询或更新，updateStrategy 不限制插入或查询。
- 手写 XML/注解 SQL 和 Wrapper 显式 set/setSql 不自动受这些实体字段策略限制。例如 ChatMapper.xml 仍显式设置 update_time=NOW()、update_by。策略不是数据库权限或不可变约束，新增显式写入仍需审查。
- create_time 的默认时间、update_time 的初始值和自动更新时间需要数据库默认值、ON UPDATE 或其他已确认机制提供；NEVER 本身不生成 DDL，也不会自动把数据库时间回填 DO/BO。实际 DDL 未验证时不能宣称时间已正确维护。
- 插入即使设置 updateBy 也不会经实体策略写入 update_by。创建人须由可信参数设置，更新人须由可信更新上下文设置，不能把字段策略当作参数授权校验。
- BaseDO 已移除 createBy/updateBy 的 JSONField(serialize=false)。TableField 写入策略不控制 JSON 序列化；DO 被直接序列化或通过 BeanUtil 的 JSON 路径转换时，应按目标类型和其他序列化规则重新核对字段暴露。现有 BaseBO 未定义 createBy/updateBy，不代表所有目标对象都不会接收这些字段。

验证时分别检查生成 INSERT 的列、实体 UPDATE 的 SET、Wrapper 显式 SET 和自定义 XML。设置非空审计时间也应验证 NEVER 字段被省略；真实时间默认值、自动更新与保存后重新查询的结果需在隔离 MySQL 验证。Mapper mock 或只断言 DO 的字段值不能证明生成 SQL 遵守策略。


[BaseQuery](../demo-dao/src/main/java/com/example/demo/framework/query/BaseQuery.java) 将查询附加字段标为 exist=false；offset 由 getter 计算，不能通过设置 offset 取代页码规则。当前 getter 不验证负值、上限或乘法溢出，公开输入约束应在相应业务入口实现。

MyBatis 配置位于 [application.yml](../start/src/main/resources/application.yml)：mapper-locations 为 classpath:mappers/*.xml，业务别名包由配置声明。Standalone 测试创建 MybatisConfiguration 不等于自动加载此配置。

[RoleMapper](../demo-dao/src/main/resources/mappers/RoleMapper.xml) 的 queryCondition 共享部分筛选；count 还存在单独 id 条件。提取过滤不能机械要求 count/list 节点完全相同，应逐项核对业务含义。Role、DictData 可选 LIMIT 的条件只验证正值，不提供页大小上限。

[WildcardTypeHandler](../demo-dao/src/main/java/com/example/demo/framework/mybatis/type/WildcardTypeHandler.java) 转义 % 与 _ 并添加前后 %，null 参数变为 %；它不等价于任意用户字符串转义器。SQL 模式与 ESCAPE 语义需在 MySQL 核对。日期过滤的结束日采用小于次日的边界，时区和日期格式是查询行为的一部分。

## 保存与批量的验收

[BannerServiceImpl](../demo-service/src/main/java/com/example/demo/banner/service/BannerServiceImpl.java) 保存后从 DO 回填 ID，更新失败抛业务异常，删除设置逻辑标志及审计。它是局部参考，Banner 的自定义 count/list SQL 绑定仍需单独核对。

[UserRoleServiceImpl](../demo-service/src/main/java/com/example/demo/user/service/UserRoleServiceImpl.java) 构造 DO 列表再 saveBatch；其中部分私有批量路径未回填每个 BO ID。因此不能宣称 saveBatch 会自动同步原 BO。是否需要回填按返回及消费者契约决定，并检查列表顺序与生成键映射。

[ChatServiceImpl](../demo-service/src/main/java/com/example/demo/chat/service/ChatServiceImpl.java) 的业务键更新后插入路径不等价于主键 upsert。唯一约束、并发幂等和影响行数必须由数据库与业务验收共同验证。

## 事务提交后操作

[TransactionUtil](../demo-util/src/main/java/com/example/demo/framework/util/TransactionUtil.java) 加入外层事务时等待其提交；rollback 不执行回调。回调异常可能向调用方传播并阻止后续回调；带 onFailure 的重载由处理器决定异常是否传播。回调再写库应通过另一 Bean 使用合适的新事务，不沿用已提交事务。

[ChatDetailServiceImpl](../demo-service/src/main/java/com/example/demo/chat/service/ChatDetailServiceImpl.java) 当前事务方法内有发送消息操作。若调整时序，明确消费者可见性、提交失败、发送失败与重复执行。afterCommit 只能解决部分时序问题，不能替代可靠投递和补偿设计。

## 验证范围

单元测试通过 captor 检查 ID、归属、审计、状态及参数，代理测试验证事务 advice。XML 解析、绑定与动态 SQL 需要实际 MyBatis 配置；默认值、逻辑删除、唯一约束、回滚及并发需要隔离 MySQL。项目尚无完整集成设施时明确未验证项，不以 mock Mapper 的成功作替代。
