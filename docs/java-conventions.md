# Java 开发约定

状态：项目规范与已核查现状。维护于 2026-10-13。适用于本仓库 Java 主代码和测试，第三方来源工具修改须单独评估。格式见 [代码风格](code-style.md)，Web 契约见 [API 约定](api-conventions.md)，数据库与事务见 [持久化](persistence.md)。

## 对象、类型和 API 使用

- 主包为 `com.example.demo`，沿用现有业务包布局、类命名和 import 风格。Java 通常为 4 空格缩进，POM 为 4 空格，Mapper XML 采用现有 Tab 布局。
- 适当使用现有 Lombok `@Getter/@Setter/@Slf4j` 等；不要为风格统一给所有对象加 `@Data`，避免无意改变 equals/hashCode/toString。
- `Serializable` 类使用自身稳定的 `serialVersionUID` 和 `@Serial`。不要引用 Spring Security 内部序列化版本常量；已有值无兼容性理由不变更。
- `CollectionUtils.isEmpty/isNotEmpty` 可简化集合 null/empty 判断；数组用数组判断或对应工具，字符串按 blank/empty 的实际语义处理。`List.of()` 不可修改，不能用于后续需要追加元素的集合。
- 延续已迁移 API：MyBatis-Plus `com.baomidou.mybatisplus.spring.service.impl.ServiceImpl`；静态空值契约采用 `org.jspecify.annotations.NonNull`；Redis 时长使用 `Duration`；字符串前缀相关操作采用合适的 `Strings.CS/CI`；URL 构造使用 `URI`；反射实例化使用 `getDeclaredConstructor().newInstance()`。修改时检查异常与大小写语义，不能仅机械换 import。
- 不全局替换 Java `javax.*`：Servlet 等使用 Jakarta，JDK 自身的 `javax.crypto` 等仍是有效包。
- `BeanUtil.copy(source, Class)` 基于 JSON 转换，`BeanUtil.copy(source, Supplier)` 基于 BeanCopier；两者在类型转换、嵌套对象及 JSON 注解上的语义不同，不能仅为性能互换。

### 参数校验的特殊情况

项目 `com.example.demo.framework.annotation.NotNull/NotBlank/NotEmpty` 是自定义注解，由 `ValidateAspect` 对 `com.example.demo.*.service.*` 的 Spring AOP 调用执行：

- 自定义 `@NotNull` 还拒绝空 List 和空数组；`@NotEmpty` 当前主要针对 null 和空字符串；`@NotBlank` 针对 null 和空白字符串。
- 它们不是 Jakarta Bean Validation，也不是 JSpecify 静态空值注解；不得按名称相同直接替换。
- 直接 `new`、私有方法、自调用等路径不能假设经过代理校验。注解变动需要代理层测试，必要的业务校验不能只依赖注解。
- 新增校验要考虑现有调用者；例如“空列表表示清空关联”与“空列表为非法参数”必须依据业务契约区分。

## 选择转换方式

对象/集合转存为 JSON 字符串字段时，先判断源值：源为 null 时保留 Java null，非 null 时再调用 JSON.toJSONString；空集合保留 `"[]"`。直接序列化 null 会得到字符串 `"null"`，与 BeanUtil.copy 到 String 属性的结果不等价，还可能通过非空判断后覆盖数据库原值。原值已是 String 时也不能无条件再次 JSON 序列化，否则可能增加引号。SDK 在缺字段时初始化的空列表应按实际 getter 结果处理，不强制改成 null。新增转换测试覆盖缺字段、显式 null、空集合及正常对象；“忽略更新”和“显式清空 SQL NULL”仍按持久化契约分别处理。

[BeanUtil](../demo-util/src/main/java/com/example/demo/framework/util/BeanUtil.java) 的 Class 重载经 JSON 序列化后反序列化；Supplier 重载通过 BeanCopier 复制属性，不启用 converter。JSON 注解、属性类型不一致、嵌套引用与空值都可能影响结果。转换方式改变时用对应回归用例验证，而非仅比较耗时。

BO 表达业务/API 数据，DO 表达持久化数据；它们可能都包含基础查询字段。不要因为同名字段便假定可直接将客户端对象作为更新实体。审计及数据归属由可信参数设置。

数字 ID、计数、页码分别遵循现有 BigInteger、long、Long 契约。不能仅为简化改为 int；可空包装类型用于表达未提供值，primitive 的默认 0 不等价。

## 注解与代理的验证

[ValidateAspect](../demo-service/src/main/java/com/example/demo/framework/aspectj/ValidateAspect.java) 的切点按包匹配。不要假定 Manager 中的同名注解也受 Service 切点保护。改变方法调用路径时核对自调用、接口/实现方法注解及代理方式，使用 [ValidateAspectTest](../demo-service/src/test/java/com/example/demo/role/service/ValidateAspectTest.java) 的代理模式验证。

JSpecify 表达静态空值契约，不能替代运行期拒绝非法业务输入。添加更严格约束前核对所有调用者，尤其空列表是否表示清空关联。

## 异常、日志与资源（新增开发约定）

选择异常处理位置应依据契约：普通数据库异常交统一处理，业务拒绝沿用项目异常，Manager 在 SDK 边界按接口要求转换。保留必要原因链，不吞掉失败返回成功；日志不能输出 token、secret、支付原始敏感数据或整个用户对象。

文件、输入流、HTTP 资源等优先使用 try-with-resources，确认资源所有权；调用方传入的资源不能未经契约约定擅自关闭。明确编码、超时和异常传播。构造 URI 或反射实例化后核对受检异常、URL 来源和构造器可访问性。

共享 Map、静态状态和单例 Bean 字段需检查并发；事务上下文不自动跨线程。新增并发逻辑明确生命周期与失败路径，时间单位用类型或命名表达。稳定基础设施能力再抽象，避免为了复用创建无实际消费者的 framework 类。

## 修改后的验证

工具转换验证 JSON 与嵌套属性；集合验证可修改性；注解验证代理；异常验证调用方可见结果和副作用。公共对象、序列化或接口变化编译全部消费者，遵循 [测试选择矩阵](testing.md#按改动选择验证)。规则不是自动检查配置，不能以文档存在宣称已执行门禁。
