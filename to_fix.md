# aj-util 待修复问题

本文记录已经确认、但尚未处理的问题。已修复问题不在此重复列出。

## `com.ajaxjs.util.reflect`

### 高优先级

1. `Methods.findCompatibleMethod()` 没有处理重载歧义。
   多个候选方法得到相同分数时，结果依赖 `Class.getMethods()` 的返回顺序。传入 `null`、存在互不相关
   的接口重载或 bridge 方法时，可能任意选择候选项。
   修复方案：比较候选参数的具体程度，过滤 bridge/synthetic 方法；无法确定唯一最佳方法时抛出明确
   的歧义异常。

2. `NewInstance` 按参数运行时类型精确查找 public 构造器。
   接口、父类、基本类型/包装类型和 `null` 参数不能兼容匹配，例如参数为 `String` 时无法调用
   `Constructor(CharSequence)`。
   修复方案：提取可复用的兼容成员匹配器，在 public 构造器集合中选择唯一最佳候选。

3. `Methods.executeDefault()` 依赖 Java 8 的私有
   `MethodHandles.Lookup(Class, int)` 构造器，在现代 JDK 上可能因构造器不存在或模块访问限制而失败。
   修复方案：保持 Java 8 编译基线，通过反射在 Java 9+ 调用 `MethodHandles.privateLookupIn()`，
   Java 8 再回退到旧实现。

### 中优先级

4. `Methods.findDeclaredMethod(String, Object...)` 通过 `Clazz.args2class()` 做精确类型查找。
   参数元素为 `null` 时空指针，包装类型不能匹配基本类型，子类或实现类也不能匹配父类或接口参数。
   修复方案：明确将此重载定位为精确查找并改善校验，或统一委托给兼容匹配器。

5. `Methods.findCompatibleMethod()` 不支持 Java 调用转换的完整规则。
   当前不支持数值基本类型扩宽（如 `Integer` 调用 `long`）和 varargs 展开。
   修复方案：在匹配评分中加入合法的 primitive widening 和可变参数阶段，并让固定参数方法优先。

6. 按名称执行时，找不到方法最终只抛出 `Method must not be null.`。
   异常没有目标类、方法名和参数类型，难以诊断。
   修复方案：在名称查找入口检测 `null`，抛出包含目标签名的 `IllegalArgumentException`。

7. `Methods.executeStatic()` 在检查 `method == null` 之前调用 `getModifiers()`，会产生不明确的
   `NullPointerException`；调用静态方法时还传入无意义的 `new Object()` 接收者。
   修复方案：先做参数校验，并直接使用 `Method.invoke(null, args)`。

8. `Types.type2class()` 只处理 `Class` 和 `ParameterizedType`。
   `TypeVariable`、`WildcardType` 和 `GenericArrayType` 返回 `null`，导致部分泛型声明无法解析。
   修复方案：为各种 `Type` 子类型定义明确解析规则；无法唯一解析时保留显式失败语义。

9. `Clazz.getClassByInterface(Type)` 通过清洗 `Type.toString()` 再调用 `Class.forName()`。
   类型变量、通配符、泛型数组和复杂 owner type 可能生成无法加载或错误的类名。
   修复方案：直接按 `Type` 子类型解析，复用 `Types.type2class()`，不要解析展示字符串。

### 低优先级

10. `Types.getGenericFirstReturnType()` 未检查实际类型参数数组是否为空。
    异常的自定义 `ParameterizedType` 返回空数组时会触发 `ArrayIndexOutOfBoundsException`。
    修复方案：检查长度并返回 `null` 或抛出明确的 `IllegalArgumentException`。

11. `Clazz.getClassByName(String)` 包装 `ClassNotFoundException` 时没有保留 cause。
    修复方案：将原异常作为 `RuntimeException` 的 cause。

12. `Clazz.args2class()` 遇到数组内的 `null` 元素会抛出不明确的 `NullPointerException`。
    修复方案：明确拒绝并报告参数索引，或仅在兼容匹配入口中允许 `null`。

13. 公开入口的 null 校验不一致。
    `getAllSuperClass(null)` 和 `NewInstance(null)` 会空指针，`getDeclaredInterface(null)` 却返回空数组；
    `NewInstance.newInstance(null, ...)` 也没有明确校验。
    修复方案：统一公开边界的 null 策略，并抛出带参数名称的 `IllegalArgumentException`。

14. `Fields.getUnderLayerErrMsg()` 对没有 detail message 的异常不会移除异常类名。
    修复方案：不要依赖冒号正则处理 `Throwable.toString()`；优先使用 `getMessage()`，并明确空消息策略。

## 已接受的边界

- `ReflectMethod` 已删除，方法查找和调用统一使用 `Methods`。
- `NewInstance` 只支持 public 构造器，不考虑 private 或其他非 public 构造器。
- `Methods.findDeclaredMethod()` 的层次遍历不包含 `Object`。
- 日期年份只允许 1900—2099，不作为待修复问题。
