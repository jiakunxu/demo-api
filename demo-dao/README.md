# demo-dao：持久化模型与 SQL

维护于 2026-10-20。事实来源为本模块 POM、DO、Mapper 和 XML。

## 职责与边界

定义数据库对象、Mapper 接口和自定义 SQL。
业务编排、权限决策和外部平台调用应由 Service/Manager 承担；SQL 必须落实调用方要求的数据范围。
仓库内无其他模块直接依赖项；直接消费者为 demo-manager、demo-service。

## 目录与关键入口

| 位置 | 用途 |
| --- | --- |
| `src/main/java/com/example/demo/<业务>/dao/dataobject/` | DO 与表字段映射 |
| `src/main/java/com/example/demo/<业务>/dao/mapper/` | BaseMapper 扩展及自定义方法 |
| [mappers](src/main/resources/mappers) | 自定义 SQL，namespace 和 id 对应 Mapper |
| [BaseDO](src/main/java/com/example/demo/framework/dataobject/BaseDO.java) | 删除标志、审计人和时间 |
| [BaseQuery](src/main/java/com/example/demo/framework/query/BaseQuery.java) | 非表查询字段、分页 offset 计算 |
| [DictDataMapper.xml](src/main/resources/mappers/DictDataMapper.xml) | 动态条件及查询参考 |
| [ChatMapper.xml](src/main/resources/mappers/ChatMapper.xml) | 会话业务键更新与未读数累加 |

Mapper 扫描由 start 的 Application 声明；实际运行映射还依赖应用装配配置。
DO 不能完整证明实际字段长度、默认值、唯一键和索引；这些需结合真实 DDL。

## 修改注意事项

- 新增自定义 Mapper 方法时核对 XML/注解绑定、参数名、resultType 和资源打包。
- count 返回 long，COUNT resultType 使用 java.lang.Long；更新影响行数仍为 int。
- 自定义 SQL 显式保留软删除与用户/企业范围，不能假定自动追加。
- 审计字段使用 createBy/updateBy 对应 create_by/update_by；排序列使用 sort_order。
- BaseQuery.getOffset 在分页参数缺失时返回 null，不负责校验非法页码和溢出。
- count/list 可以共享过滤片段；不共用 list/get 的 SELECT 投影。
- 参数使用绑定，动态排序采用允许列名和方向映射；CDATA、缩进规则见持久化文档。
- 简单查询迁移至 MyBatis-Plus 时保留归属条件、重复结果行为、排序和 LIMIT。

## 验证

从仓库根目录执行：

```bash
./mvnw -DskipTests test-compile
./mvnw -pl demo-service -am test
```

第一条检查全部消费者编译；第二条包含上游测试，需要时按测试指南添加临时 JVM 参数。
本模块当前未发现独立测试。Service Mapper mock 不能证明 SQL 已绑定或正确执行；
SQL、逻辑删除、默认值、索引和事务需在隔离 MySQL 环境验证，当前没有完整集成测试设施。

## 相关文档

[数据库](../docs/database.md) · [持久化](../docs/persistence.md) · [测试](../docs/testing.md) · [工程缺口](../docs/engineering-roadmap.md)
