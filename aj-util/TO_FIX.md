# Utils 包
只统计 `com.ajaxjs.util` 根包、排除子包和已修复问题后，目前发现 **14 个明确或值得修复的问题**：

DebugTools 类加载时会修改整个 JVM 的默认时区。
RandomTools.uuid() 使用可预测的 java.util.Random，与安全随机数文档不符。
MapTool.deepCopy() 实际只是浅复制，与名称和文档不符。
HashHelper.getMac() 未设置密钥时随机生成且不返回密钥，生成的 MAC 无法再次验证。
UrlHelper.simpleGET() 没有连接/读取超时，并将 IO 异常静默转换为 null。

高优先级 3 个：

1. `DebugTools` 类加载时会擅自修改整个 JVM 默认时区。
2. `RandomTools.uuid()` 声称使用安全随机数，实际使用可预测的 `java.util.Random`。
3. `XmlHelper` 解析失败时会把完整 XML 写入日志和异常，可能泄露密码、令牌等敏感数据。

中优先级 8 个：

4. `JsonUtil` 双重检查初始化的 `engine` 未声明为 `volatile`，存在并发安全发布问题。
5. `BytesHelper.parseHexStr2Byte()` 静默截断奇数长度十六进制字符串；空输入返回 `null`。
6. `ConvertBasicValue` 按 ordinal 转换枚举时返回整数，而不是枚举实例。
7. `ConvertBasicValue.toJavaValue()` 无法识别负小数。
8. `MapTool.toMap()` 会截断值中第二个及后续的 `=`。
9. `MapTool.mapToXml()` 对 null value 先调用 `toString()`，直接空指针。
10. `MapTool.deepCopy()` 实际只是浅复制，与方法名称和文档不符。
11. `StrUtil.leftPad()` 会替换原字符串内已有的空白；填充值含 `$` 或 `\` 也可能产生异常或错误结果。

低优先级 3 个：

12. `HashHelper.getMac()` 未设置密钥时随机生成密钥但不返回，生成的 MAC 无法再次验证。
13. `UrlHelper.simpleGET()` 没有连接和读取超时，并将所有 IO 错误静默转换成 `null`。
14. `XmlHelper.getNodeAttribute()` 对没有 attributes 的节点可能空指针。

其中建议优先处理 `DebugTools`、UUID 随机源、XML 敏感信息泄露和 `JsonUtil` 并发发布问题。

# 反射
反射工具包目前已经修复接口层次遍历 NPE 和接口递归去重，剩余问题主要如下。

## 高优先级

1. 方法和构造器只支持参数类型精确匹配

涉及：

- `Clazz.newInstance()`
- `Clazz.args2class()`
- `Methods.getMethod()`
- `Methods.executeMethod()`
- `NewInstance`

目前通过 `arg.getClass()` 查找成员，因此这些情况都会失败：

```java
void setValue(Number value);
executeMethod(obj, "setValue", Integer.valueOf(1)); // Integer != Number

void setCount(int count);
executeMethod(obj, "setCount", Integer.valueOf(1)); // Integer != int

Foo(CharSequence value);
newInstance(Foo.class, "text"); // String != CharSequence
```

传入 `null` 参数还会在 `args2class()` 中 NPE。

建议实现统一的成员匹配器，支持：

- `isAssignableFrom()`
- 基本类型与包装类型转换
- `null` 匹配非基本类型
- 多参数
- 接口和父类距离
- 重载最具体候选选择
- 多个同等候选时报告歧义

2. 按方法名查找使用字符串包含匹配

位置：`Methods.getSuperClassDeclaredMethod(Class, String)`

当前：

```java
if (m.toString().contains(method))
```

可能在返回类型、参数类型或其他方法名中误命中，并且重载结果依赖 JVM 返回顺序。

建议至少改为：

```java
m.getName().equals(method)
```

如果存在多个同名重载，应返回候选集合或抛出歧义异常，不能任意选择第一个。

3. `Types.type2class()` 无法处理 ParameterizedType

当前只处理：

```java
type instanceof Class
```

所以：

```java
List<String>
```

会返回 `null`，而不是 `List.class`。

建议支持：

- `Class`
- `ParameterizedType`
- `GenericArrayType`
- `WildcardType`
- `TypeVariable`

其中 `ParameterizedType` 可递归转换 `getRawType()`。

4. `Types.getActualClass()` 可能 NPE

非泛型类调用：

```java
Types.getActualClass(String.class)
```

`getActualType()` 返回 `null`，随后直接访问：

```java
actualType[0]
```

建议检查 `null` 和空数组，并明确返回 `null`、`Optional<Class<?>>` 或抛出特定异常。

## 中优先级

5. 带目标类型的 `getClassByName()` 没有真正校验类型

```java
Clazz.getClassByName("java.lang.String", Number.class);
```

当前只做未经检查的泛型强转，参数 `Number.class` 没有参与验证。

建议：

```java
loadedClass.asSubclass(expectedType)
```

实例转换使用：

```java
expectedType.cast(instance)
```

6. 构造器可见性处理不一致

无参路径使用：

```java
getDeclaredConstructor()
```

带参数路径使用：

```java
getConstructor()
```

前者能找到非 public 构造器，但没有设置可访问性；后者只查 public 构造器。

建议明确策略：

- public-only：统一使用 `getConstructor()`。
- 允许非 public：统一使用 `getDeclaredConstructor()`，并由显式参数决定是否 `setAccessible(true)`。

7. `NewInstance` 使用废弃的 `Class.newInstance()`

```java
clz.newInstance()
```

会丢失部分异常语义，也无法正确处理构造器抛出的异常。

Java 8 下也可以改为：

```java
clz.getDeclaredConstructor().newInstance()
```

8. `Clazz` 和 `NewInstance` 功能高度重复

两套代码都实现：

- `args2class`
- `getConstructor`
- `newInstance`

行为和异常语义容易逐渐不一致。

建议保留 `Clazz` 作为底层实现，`NewInstance` 仅作为轻量包装，或者直接废弃 `NewInstance`。

9. `getMethodByUpCastingSearch()` 仍不是真正的兼容匹配

它只支持单参数，只遍历参数对象的父类，不完整处理：

- 接口
- 多层父接口
- 基本类型和包装类型
- 多参数方法
- `null`
- 重载优先级

建议由统一的方法匹配器替代，而不是继续扩展这个专用方法。

10. `getDeclaredMethodByInterface()` 通过 Type 字符串还原接口类

它先把 `Type.toString()` 做正则替换，再调用 `Class.forName()`。对复杂泛型、内部类、通配符和类型变量都不稳健。

建议直接处理 Type 类型：

```java
if (type instanceof Class)
if (type instanceof ParameterizedType)
```

不要解析 `Type.toString()`。

11. `getUnderLayerErr()` 对空 cause 不安全

如果包装异常没有 cause：

```java
new InvocationTargetException(null)
```

下一轮调用 `e.getClass()` 会 NPE。

建议：

```java
while (isWrapper(e)
        && e.getCause() != null
        && e.getCause() != e) {
    e = e.getCause();
}
```

12. 静默执行 API 无法区分失败和正常返回 null

`Methods.executeMethod()` 捕获所有异常后返回 `null`。因此无法区分：

- 方法正常返回 `null`
- 方法不存在
- 访问失败
- 参数不匹配
- 目标方法抛异常

建议拆成：

```java
invoke(...)          // 失败抛异常
invokeQuietly(...)   // 明确吞异常
```

## 低优先级和 API 设计

13. “找不到”的语义不统一

当前有的返回 `null`，有的抛 `RuntimeException`：

- `getClassByName()`：抛异常
- `getMethod()`：返回 `null`
- `getDeclaredMethod()`：抛异常
- `findField()`：返回 `null`

建议统一为：

```java
findXxx()        // Optional 或 null
getRequiredXxx() // 找不到时抛异常
```

14. `executeStaticMethod()` 传入无意义的 `new Object()`

静态方法调用时 receiver 会被忽略，因此当前一般能工作，但表达不清晰：

```java
executeMethod_Throwable(new Object(), method, args)
```

建议让底层调用支持静态方法的 `null` receiver，并显式校验 `Modifier.isStatic()`。

15. 默认接口方法调用只适合 Java 8

`MethodHandles.Lookup(Class, int)` 是 Java 8 常见方案，但在 Java 9+ 模块封装下容易失败。

项目定位 Java 8，所以目前不算直接 Bug，但建议：

- 校验 `method.isDefault()`。
- 校验 proxy 实现了声明接口。
- 零参数时把 `null` 参数归一化为空数组。
- 在文档中明确 Java 8 限制。

16. 工具类都可以阻止实例化

`Clazz`、`Fields`、`Methods`、`Types` 都只有静态方法，但目前可以被实例化。

建议定义为：

```java
public final class Methods {
    private Methods() {
    }
}
```

## 测试套件仍有明显问题

现有反射测试本身尚未整理：

- `getClassByName()` 找不到类时，测试期待 `null`，实现实际抛异常。
- 在没有 `m1()` 的测试类上断言能找到 `m1()`。
- `TestTypes` 使用 `getMethods()[0]`，方法顺序没有保证。
- 查找不存在的 `getList()`，实际方法名是 `getList2()`。
- 非泛型测试类被当作泛型父类测试。
- `type2class()` 测试期待 `ParameterizedType` 返回 raw type，但实现返回 `null`。
- 部分测试使用 Java `assert`，未启用 `-ea` 时不会执行。

建议下一轮优先处理：

1. 整理并修复测试夹具。
2. 建立统一的方法/构造器兼容匹配器。
3. 修复 `Types` 的类型转换。
4. 修复模糊方法名匹配。
5. 统一异常和“找不到”语义。

# Date

日期工具包在本轮修复后，主要还剩这些问题。

## 优先建议继续修复

1. `LocalTime` 输入完全无法转换

虽然存在：

```java
new DateTypeConvert(LocalTime)
```

但 `to()` 没有处理 `localTime` 字段，最终会抛出“没有输入”。

建议：

- `LocalTime -> LocalTime` 直接返回。
- 转换为 `Instant/Date/LocalDateTime` 时要求调用方提供锚点日期。
- 不要自动使用“今天”。

2. 日期解析仍不够严格

默认 `DateTimeFormatter.ofPattern("yyyy-MM-dd")` 使用 SMART 解析，可能把非法日期静默修正：

```text
2023-02-29 -> 2023-02-28
2023-04-31 -> 2023-04-30
```

建议使用：

```java
ResolverStyle.STRICT
```

并将年份格式从 `yyyy` 改成 `uuuu`。

3. 正则允许的格式与 formatter 不一致

日期正则允许：

```text
2023-4-5
```

但 `yyyy-MM-dd` formatter 通常要求：

```text
2023-04-05
```

正则还只支持 1900–2099 年。

建议取消正则预判，按明确的 formatter 列表依次严格解析。

4. `object2Date()` 的错误语义与文档不一致

Javadoc 表示转换失败返回 `null`，但非法字符串实际上会抛 `DateTimeParseException`。

建议拆成：

```java
parseDate(...)       // 失败抛异常
tryParseDate(...)    // 失败返回 null 或 Optional<Date>
```

5. Integer 时间戳被隐式认定为秒

```java
new DateTypeConvert(int)
```

通过字符串补三个零转换为毫秒：

```java
Long.parseLong(timestamp + "000")
```

这不仅语义隐蔽，还受 2038 年 `int` 秒时间戳范围限制。

建议改成明确工厂方法：

```java
fromEpochSeconds(long)
fromEpochMillis(long)
```

并逐步废弃 `int` 构造器。

## 时区语义仍需完善

6. `Calendar` 输入仍会丢失原始时区

`DateTypeConvert(Calendar)` 目前只保存 `Instant`，没有保存 Calendar 的时区。如果转换时不传 `ZoneId`，会使用系统默认时区。

建议同时保存：

```java
calendar.getTimeZone().toZoneId()
```

未指定目标时区时优先保留输入 Calendar 的时区。

7. `LocalDate.atStartOfDay(zone)` 仍可能静默处理日期边界

某些地区会在午夜发生时区切换，甚至跳过整个日期。`atStartOfDay(zone)` 会自动选择该日期最早的合法时间，而不一定是 `00:00`。

建议明确语义：

- 如果需要“该日期最早合法瞬间”，保留当前行为并写入文档。
- 如果要求严格午夜，则检查返回时间是否为 `00:00`，否则抛异常。

8. `OffsetTime` 尚未提供锚点日期转换 API

现在已经不会偷偷使用今天，但调用方也没有办法通过现有 `to()` 正确转换为 `Instant`。

建议新增：

```java
to(Class<T> target, LocalDate anchorDate, ZoneId zone)
```

或者更明确：

```java
toInstant(LocalDate anchorDate)
```

## Formatter 问题

9. formatter 缓存无限增长

任意格式字符串都会永久存入静态 `ConcurrentHashMap`。如果 pattern 来自外部输入，可能持续占用内存。

建议只缓存内置格式，或改为有限容量缓存。

10. 默认 Locale 隐式且缓存不区分 Locale

```java
DateTimeFormatter.ofPattern(format)
```

使用创建时的默认 Locale，但缓存 key 只有 format。运行期间 Locale 改变后，缓存行为会不一致。

建议：

- 默认使用 `Locale.ROOT`。
- 需要本地化时显式传入 Locale。
- 缓存 key 同时包含 pattern 和 Locale。

11. `Formatter(Date)` 隐式使用系统默认时区

它先把 `Date` 转为系统时区下的 `LocalDateTime`。同一个 `Date` 在不同服务器上格式化结果不同。

建议增加：

```java
Formatter(Date date, ZoneId zone)
```

或直接使用带时区的 formatter 格式化 `date.toInstant()`。

12. ISO 8601 formatter 是手工格式

当前：

```java
yyyy-MM-dd'T'HH:mm:ss'Z'
```

存在 `yyyy` 纪年语义问题，也不支持小数秒。

建议根据协议改用：

```java
DateTimeFormatter.ISO_INSTANT
```

如果必须固定到秒，则先截断 `Instant`，再使用 `uuuu` 格式。

## 测试仍需补充

还缺少这些关键测试：

- `LocalTime` 各目标类型。
- 2 月 29 日、4 月 31 日等严格解析。
- 单数字月日是否支持。
- 1900 年以前、2100 年以后的日期。
- Calendar 输入时区保留。
- 跨午夜和跳过整日的极端时区。
- `object2Date()` 非法输入的统一行为。
- 固定 Locale 下的格式化。
- epoch seconds 的 2038 年边界。

建议下一轮优先修复：`LocalTime`、严格日期解析、解析异常语义和 Calendar 输入时区。这四项对正确性的影响最大。


# 加密

加密工具包在上一轮修复后，剩余问题主要如下。

## 高优先级

1. `AES_encode/AES_decode` 仍使用 Provider 默认模式

当前使用：

```java
Cipher.getInstance("AES")
```

通常会落到 ECB 模式。ECB 不使用随机 nonce，相同明文块会产生相同密文块，也不验证密文完整性。

建议：

- 新增明确的 `AES/GCM/NoPadding` API。
- 每次加密生成新的 12 字节随机 nonce。
- 密文携带版本、nonce 和认证标签。
- 原 AES API 标记为 legacy，保留旧数据解密能力。

2. AES 密钥仍通过固定种子的 `SHA1PRNG` 生成

`AES_encode(data, key)` 把字符串 key 用作 `SHA1PRNG` 种子，再通过 `KeyGenerator` 生成 AES 密钥。

这不是标准口令派生方式：

- 没有随机 salt。
- 没有计算成本。
- Provider 之间可能不一致。
- 相同口令永远产生相同密钥。

虽然 PBE API 已改成 PBKDF2，但普通 AES API 尚未迁移。

建议：

- 字符串口令统一走 PBKDF2。
- 真正的 AES key 使用 `byte[]` 或 Base64 输入。
- 不再使用 `SHA1PRNG` 派生确定性密钥。

3. DES、3DES、MD5-PBE、MD5withRSA 仍是公开能力

涉及：

- `DES_encode/decode`
- `tripleDES_encode/decode`
- `PBE_LEGACY`
- `MD5_RSA`

上一轮已经让旧 PBE 只保留 legacy 解密，但 DES、3DES 和 MD5 签名仍容易被新代码调用。

建议：

- 所有旧算法加 `@Deprecated`。
- 类名或方法名显式增加 `legacy`。
- 文档注明仅用于解密或验证历史数据。
- 禁止使用旧算法生成新密文和新签名。

4. RSA 加密仍使用不完整的 `"RSA"` transformation

当前：

```java
Cipher.getInstance("RSA")
```

Provider 通常会采用 PKCS#1 v1.5 padding，但具体行为没有被 API 明确限定。

建议：

- 新加密接口使用 RSA-OAEP。
- 最好显式配置 OAEP-SHA-256 和 `MGF1ParameterSpec`。
- 保留旧 PKCS#1 v1.5 解密入口用于迁移。
- 不要直接替换旧方法内部算法，否则历史密文无法解密。

5. RSA 没有分块，也没有混合加密

当前把全部数据一次传给：

```java
cipher.doFinal(data)
```

RSA 只能处理比模数短很多的数据。稍长字符串就会失败。

建议不要简单实现 RSA 多块拼接，而是采用混合加密：

1. 随机生成 AES-GCM 数据密钥。
2. 用 AES-GCM 加密正文。
3. 用 RSA-OAEP 加密 AES 密钥。
4. 输出版本化数据结构。

6. “私钥加密、公钥解密”仍可能被误当作数字签名

仍存在：

```java
privateKeyEncrypt()
publicKeyDecrypt()
```

这不是标准数字签名 API，容易被误用。

建议：

- 标记废弃。
- 签名统一使用 `Signature`。
- 加密仅保留公钥加密、私钥解密。
- 如果历史协议确实使用反向 RSA，方法名增加 `legacyRawRsaOperation`。

## 中优先级

7. `setKeyData()` 使用完整 transformation 作为密钥算法

例如对象的算法为：

```text
AES/GCM/NoPadding
```

当前可能创建：

```java
new SecretKeySpec(keyData, "AES/GCM/NoPadding")
```

但密钥算法应该是 `"AES"`。

建议把下面两个概念分开：

```java
keyAlgorithm = "AES"
transformation = "AES/GCM/NoPadding"
```

8. `setSecretKey()` 不必要地重新包装密钥

当前从已有 `SecretKey` 取 encoded，再创建新的 `SecretKeySpec`。这会：

- 丢失原密钥实现及参数。
- 对不可导出密钥造成问题。
- 使用 transformation 作为 key algorithm。

建议直接：

```java
this.secretKey = secretKey;
this.key = secretKey;
```

9. `Cryptography.doCipher()` 仍缺少执行前状态校验

签名和验签已经补了状态校验，但通用加密器还没有检查：

- `algorithmName`
- mode 是否为合法 Cipher mode
- key
- data
- GCM nonce/spec
- AAD 是否只用于 AEAD 模式

缺少状态时可能产生 NPE 或 Provider 相关异常。

建议在创建 `Cipher` 前统一执行 `validateState()`。

10. 证书加载只检查有效期，不验证信任链

`CertificateUtils.getCert()` 调用：

```java
cert.checkValidity()
```

只检查证书是否过期或尚未生效，并不能证明：

- 由可信 CA 签发。
- 签名链有效。
- KeyUsage/ExtendedKeyUsage 合适。
- 证书没有被吊销。

建议拆分：

```java
parseCertificate()
checkCertificateValidity()
validateCertificateChain()
```

完整验证使用 PKIX、可信根和可选吊销检查。

11. 证书解析会主动关闭调用方传入的 InputStream

当前 `getCert(InputStream)` 使用 try-with-resources。虽然 Javadoc 已说明会关闭，但工具方法关闭调用方资源仍容易产生组合问题。

建议：

- 默认不关闭调用方传入的流。
- 文件路径重载负责关闭自己创建的流。
- 或提供明确的 `closeInput` 参数。

12. `deserializeToCerts()` 对 Map 结构进行不安全强转

代码直接假定：

```java
pMap.get("data") instanceof List
map.get("encrypt_certificate") instanceof Map
```

字段缺失或类型不匹配会产生 NPE/ClassCastException。

`remove()` 还调用：

```java
v.toString().replace("\"", "")
```

会无差别删除内容中的双引号。

建议：

- 校验每一层数据结构。
- JSON 反序列化为明确 DTO。
- 不要通过删除引号“清洗”已经解析的字符串。

13. PEM 格式支持范围不准确

当前实际支持：

- X.509 SubjectPublicKeyInfo：`BEGIN PUBLIC KEY`
- 未加密 PKCS#8：`BEGIN PRIVATE KEY`

但正则也可能移除：

- `RSA PRIVATE KEY`
- `RSA PUBLIC KEY`
- `ENCRYPTED PRIVATE KEY`

这些格式的底层编码不同，移除头尾后仍不能用当前 KeySpec 解析。

建议根据 PEM label 严格分派；不支持的格式直接报告具体错误。

14. 密钥恢复固定使用 RSA

`KeyMgr` 实例包含 `algorithmName`，但静态 `restoreKey()` 固定调用：

```java
KeyFactory.getInstance("RSA")
```

如果类未来用于其他非对称算法，行为会与实例配置不一致。

建议：

- RSA 工具明确只做 RSA，并从命名上体现；或
- 让 restore API 显式接收算法。

## API 和敏感数据管理

15. Lombok `@Data` 暴露密钥和明文状态

`Cryptography`、`KeyMgr`、`DoSignature`、`DoVerify` 自动生成大量 getter/setter，包括：

- 私钥、公钥
- 对称密钥
- 明文 byte[]
- 签名数据
- 中间状态

数组还会直接暴露内部引用。

建议：

- 去掉安全类上的 `@Data`。
- 只保留必要的链式 setter。
- 输入/输出数组防御性复制。
- 不提供私钥和明文的通用 getter。

16. 敏感字节没有及时清理

PBE 的 `PBEKeySpec` 已经清理，但其他地方仍长期保留：

- `data`
- `keyData`
- `associatedData`
- 解码后的私钥字节
- Base64 私钥字符串

建议：

- 操作完成后对临时 byte[] 执行 `Arrays.fill(..., (byte) 0)`。
- 尽量避免用不可清理的 `String` 保存口令和私钥。
- 口令优先使用 `char[]`。

17. 签名算法仍完全由字符串控制

调用方可以选择：

```java
MD5withRSA
SHA1withRSA
```

状态校验只能确认算法非空，无法确认算法安全。

建议：

- 新 API 使用受控枚举或白名单。
- 默认 `SHA256withRSA` 或 RSA-PSS。
- 弱算法仅在 legacy 验签入口允许。

18. 签名对象状态仍可能不一致

`DoSignature` 同时保存：

```java
strData
data
privateKeyStr
privateKey
```

通过 Lombok setter 修改其中一个字段后，另一字段可能保留旧值。

建议只保存真正参与运算的字段，例如 `byte[] data` 和 `PrivateKey`；字符串 setter 只负责转换，不再保存重复字符串状态。

## 测试仍不足

上一轮已经增加了安全回归测试，但还缺少：

- 新 AES-GCM 通用 API 的测试。
- AES 密文随机性和篡改检测。
- RSA-OAEP 测试向量。
- RSA 超长数据和混合加密。
- 2048/3072/4096 位实际生成与签名。
- legacy DES、PBE、RSA 数据兼容测试。
- PKIX 可信链和不可信链。
- PEM 的 PKCS#1、PKCS#8、encrypted PKCS#8 分类。
- 敏感信息不进入所有异常和日志的检查。
- Java 8 不同 Provider 下的兼容性测试。

建议下一轮优先修复：普通 AES API、SHA1PRNG 密钥派生、RSA transformation/混合加密、反向 RSA API，以及 `setKeyData/setSecretKey` 的算法处理。


# IO
按照上一轮列出的 13 个问题统计，目前还剩 **6 个**：

1. `DataWriter.write(InputStream)` 会意外关闭调用方传入的输入流。
2. `DataReader.readAsString()` 和 `FileHelper.getFileContent()` 会丢失换行符。
3. `FileHelper.writeFileContent()` 使用系统默认编码，与 UTF-8 读取不一致。
4. `Resources` 将 classpath URL 当作普通文件路径，无法正确支持 `jar:` 等协议。
5. `CmdHelper` 仍然忽略子进程非零退出码。
6. `ZipHelper.isZipFile()` 无法识别合法的空 ZIP。

线程中断状态已经在修复命令死锁时一并处理，因此不再计入。