# aj-util 待修复问题

本文记录已经确认、但尚未处理的问题。已修复问题不在此重复列出。

## `com.ajaxjs.util.reflect`

### 高优先级

1. `ReflectMethod.findCompatibleMethod()` 没有处理重载歧义。
   多个候选方法得到相同分数时，当前结果依赖 `Class.getMethods()` 的返回顺序。尤其是传入
   `null` 时，`method(String)` 与 `method(Integer)` 等互不相关的重载可能被任意选择。
   修复方案：比较候选参数的具体程度；无法确定唯一最佳方法时抛出明确的歧义异常。

2. `NewInstance` 仍按参数运行时类型精确查找 public 构造器。
   接口、父类、基本类型/包装类型和 `null` 参数不能兼容匹配，例如参数为 `String` 时无法调用
   `Constructor(CharSequence)`。
   修复方案：提取可复用的兼容成员匹配器，在 public 构造器集合中选择唯一最佳候选。

3. `ReflectMethod.executeDefault()` 依赖 Java 8 的私有
   `MethodHandles.Lookup(Class, int)` 构造器，在现代 JDK 上可能因构造器不存在或模块访问限制而失败。
   修复方案：保持 Java 8 编译基线，通过反射在 Java 9+ 调用 `MethodHandles.privateLookupIn()`，
   Java 8 再回退到旧实现。

### 中优先级

4. `ReflectMethod.getMethod()` 将 `SecurityException` 当作“方法不存在”并返回 `null`。
   修复方案：只捕获 `NoSuchMethodException`，让安全访问异常保留原始语义。

5. `Types.type2class()` 只处理 `Class` 和 `ParameterizedType`。
   `TypeVariable`、`WildcardType` 和 `GenericArrayType` 返回 `null`，导致部分泛型声明无法解析。
   修复方案：为各种 `Type` 子类型定义明确解析规则；无法唯一解析时保留显式失败语义。

6. `ReflectMethod.executeStatic()` 在检查 `method == null` 之前调用 `getModifiers()`，会产生不明确的
   `NullPointerException`；调用静态方法时还传入了无意义的 `new Object()` 接收者。
   修复方案：先做参数校验，并直接使用 `Method.invoke(null, args)`。

### 低优先级

7. `Clazz.getClassByName(String)` 包装 `ClassNotFoundException` 时没有保留 cause。
   修复方案：将原异常作为 `RuntimeException` 的 cause。

8. `Clazz.args2class()` 遇到数组内的 `null` 元素会抛出不明确的 `NullPointerException`。
   修复方案：明确拒绝并报告参数索引，或仅在兼容匹配入口中允许 `null`。

9. 若干公开入口的 null 校验仍不一致，例如 `NewInstance(Class<T>)` 会在验证前调用
   `clz.isInterface()`。
   修复方案：统一在公开边界抛出带参数名称的 `IllegalArgumentException`。

## 已接受的边界

- `Methods` 已放弃，不再作为待修复 API；文档和 skill 使用 `ReflectMethod`。
- `NewInstance` 只支持 public 构造器，不考虑 private 或其他非 public 构造器。
- 日期年份只允许 1900—2099，不作为待修复问题。
