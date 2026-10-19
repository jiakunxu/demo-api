# API 与 Web 契约

状态：现状与开发规范。维护于 2026-10-13。面向 Controller、BO、响应、序列化和消费者修改。公开行为的变更需按 [需求模板](requirements/template.md) 表述，并验证消费者。

## 保留的契约

- 保留 API 的路径、请求方法、参数、响应结构、日期格式、枚举值、null/空列表语义及错误码；确需改变时同步调用方、测试和兼容说明。
- HTTP 响应沿用 `ObjectResponse/ListResponse/PageResponse` 等。`AbstractResponse.code` 是响应体字段，不应与真实 HTTP status 混为一谈；现有 `BaseExceptionHandler` 没有统一设置同值 HTTP status。
- `JsonConfigurer` 使用 Fastjson2，日期格式为 `yyyy-MM-dd HH:mm:ss`，排除 `codes` 字段。修改序列化配置属于契约变更；支付 SDK 的 snake_case 字段、注解映射也须验证。
- 对外输入应验证类型、长度、范围、枚举和数据归属。不要直接信任客户端传入的审计人、企业 ID、角色权限或可写字段；必要时显式映射允许修改的字段。

## 入口与请求解析现状

[BannerController](../demo-web/src/main/java/com/example/demo/banner/web/BannerController.java) 是常规入口样例：list/get 使用 GET，save/update/delete 使用 POST，通过 BaseController 获取参数，用方法级 authority 保护。这里记录现状，不要求将现有接口批量改成另一种 URL 或 HTTP 方法风格。

[BaseController](../demo-web/src/main/java/com/example/demo/framework/web/BaseController.java) 有不同解析路径：

| 路径 | 当前行为 | 修改时须验证 |
| --- | --- | --- |
| GET/表单与 BO 实例 | ServletRequestDataBinder 绑定，startDate/endDate 映射到 start/end；数字字符串转换 pageNo/pageSize | 绑定错误、数值范围、越界和允许修改字段 |
| JSON 与 Class | Content-Type 合适时读取 UTF-8 JSON；BaseBO 的二项 dateRange 映射日期；解析失败通常记录日志后返回空对象 | 坏 JSON 与真正空请求的区别，不能宣称统一返回 400 |
| JSON 数组 | 非 JSON 返回空数组；读取路径主要捕获 IOException | 语法错误与空列表的异常行为不同于 Class 路径 |
| 排序字段（GET/表单与 BO 路径） | sort 经 SqlUtil.escapeOrderBySql 检查字符及最大 500 字符长度 | 字符限制不等于业务字段白名单 |
| 排序方向（GET/表单与 BO 路径） | ascending/descending 映射 ASC/DESC，其他值（含缺失）清空 sort 和 dir | 排序字段先校验，非法 sort 不会因随后清空而免于报错 |

2026-10-18 核对：[SqlUtil](../demo-util/src/main/java/com/example/demo/framework/util/SqlUtil.java) 的排序检查允许字母、数字、下划线、空格、逗号和点；null/空字符串原样返回，非法字符或超长值抛出 RuntimeException。该路径没有调用 filterKeyword，也不验证字段是否属于业务允许范围。JSON 等其他解析入口不能直接套用上述表单路径的限制。

`getRemoteAddr` 优先取 X-Forwarded-For，不能假定它一定可信或只有一个 IP；受信代理策略属于入口部署契约。新增输入校验需显式实施并回归，不能写文档即认为现有解析已安全。

## 响应对象

| 类型 | 当前核心字段 | 语义 |
| --- | --- | --- |
| [AbstractResponse](../demo-web/src/main/java/com/example/demo/framework/response/AbstractResponse.java) | code、msg、subCode、subMsg、body、params | 响应体公共字段，null 字段输出以序列化配置为准 |
| [ObjectResponse](../demo-web/src/main/java/com/example/demo/framework/response/ObjectResponse.java) | data | 对象或 null，构造器设置 code=200 |
| [ListResponse](../demo-web/src/main/java/com/example/demo/framework/response/ListResponse.java) | total、rows、extra | total 可未设置；计数构造器使用 long |
| [PageResponse](../demo-web/src/main/java/com/example/demo/framework/response/PageResponse.java) | pageNo、pageSize、pageCount、totalCount、list | null 数字转 0；页大小或总数为 0 时 pageCount=1 |
| [ExceptionResponse](../demo-web/src/main/java/com/example/demo/framework/response/ExceptionResponse.java) | code、msg | 业务异常的响应包装，不自动设置真实 HTTP 状态 |

BannerController 的零计数分支返回 rows=null；部分 Service 返回 List.of()。不能宣称所有空列表统一为 []。将 null 改成空数组或改变 pageCount 的边界属于契约变化。

[JsonConfigurer](../demo-web/src/main/java/com/example/demo/framework/config/JsonConfigurer.java) 使用 Fastjson2，日期为 yyyy-MM-dd HH:mm:ss，过滤 codes，WriteNullListAsEmpty 未启用。不要凭通用 Spring/Jackson 经验推断实际输出；日期格式没有独立定义时区，时区需结合环境核对。大整数 ID 的客户端精度、snake_case SDK 映射及 null 行为需做实际序列化验证。

## 异常与 HTTP 状态

[BaseExceptionHandler](../demo-web/src/main/java/com/example/demo/framework/handler/BaseExceptionHandler.java) 在响应体映射拒绝访问、认证失败、数据库异常及业务异常。它未统一设置相同 HTTP status；过滤器的认证失败处理需独立读取，不能以 body code=401 推断网络状态为 401。

现有通用异常处理可能直接采用异常消息，修改时检查敏感详情是否对外暴露；期望统一安全提示时实现并回归。新增接口明确正常/拒绝/缺失/非法输入/依赖失败的 body 和 HTTP 状态，不默默改变全部旧接口。

## 身份、权限与数据范围

[WebSecurityConfig](../demo-web/src/main/java/com/example/demo/framework/config/WebSecurityConfig.java) 使用 stateless 模式，公开回调、登录、验证码与 /ws 等路径，其他请求要求认证，同时启用方法安全。permitAll 只代表不要求登录，不代表验签、通道归属或业务参数检查已完成。

[TokenAuthenticationFilter](../demo-web/src/main/java/com/example/demo/framework/filter/TokenAuthenticationFilter.java) 从 Authorization 获取 token，调用安全 TokenService 查用户，建立 SecurityContext 并刷新 TTL。TokenService 的接口前缀为 Bearer；身份仍依赖 Redis 与刷新逻辑，不将 JWT 理解为完全独立的无状态用户权限快照。

[安全 TokenService](../demo-service/src/main/java/com/example/demo/security/service/TokenServiceImpl.java) 使用 JWT ID 关联随机缓存键，Redis/加载用户失败不能绕过认证。按 ID 重载用户尚存在占位实现，涉及刷新需核查 [工程现状](engineering-roadmap.md)。审计人使用可信上下文；企业/用户/角色归属必须在业务与 SQL 范围中验证，不只检查登录。

## 分页、排序与兼容验收

菜单、角色、字典数据的排序属性统一为 `sortOrder`（Integer），数据库列为 `sort_order`。菜单/角色旧接口字段 `order` 已重命名，调用端必须同步输入输出；未保留旧字段别名。字典数据新增同名排序属性。发布时同步数据库迁移与业务缓存失效，见 [数据库指南](database.md#161-排序列重命名尚未执行)。

页码从 1 开始，输入缺失由 Long 的 null 表示；可选分页仅按目标接口使用，非法值不能成为公开无界查询的后门。count/list 使用相同业务筛选，但各自 projection 保持独立。具体规则见 [持久化](persistence.md)。

接口修改至少核对路径和方法、参数绑定、权限与数据范围、序列化、零结果、非法输入和失败分支。当前未建立完整 Web 回归设施时明确验证限制；Service mock 测试不证明 HTTP 状态或实际 JSON 输出正确。
