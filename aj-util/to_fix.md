# aj-util 待修复问题

本文记录已经确认、但尚未处理的问题。已修复问题不在此重复列出。

## 审查范围

- 本轮检查了 `com.ajaxjs.util` 直属类以及 `cryptography`、`date`、`httpremote`、`io`、
  `json`、`log` 包。
- `reflect` 包沿用此前的审查结果，本轮没有重新检查。
- 下列优先级按“安全/数据损坏 > 错误结果/资源失控 > API 一致性和可诊断性”划分。

## `com.ajaxjs.util` 直属类

### 高优先级

1. `HashHelper.getMac()` 在没有设置 HMAC 密钥时临时生成随机密钥，但既不保存也不返回该密钥。
   调用者拿到的 MAC 无法复算或验证；同一实例连续调用也会得到不同结果。更容易混淆的是，
   `hash()` 在没有密钥时走普通摘要，而直接调用 `getMac()` 却使用不可见的随机密钥。
   修复方案：要求调用者显式提供密钥；或者将生成的密钥保存到字段并提供安全的导出 API，同时统一
   `hash()` 与 `getMac()` 的状态语义。
   单测：`TestHashHelper.macRequiresAnExplicitKey()`。

### 中优先级

2. `ConvertBasicValue` 的整数转换使用 `Number.intValue()`、`longValue()` 等截断式转换。
   超出目标类型范围或把小数转换为整数时不会报错，可能静默产生溢出或精度丢失。
   修复方案：默认使用精确、带范围检查的转换；如需 Java 强制转换语义，另提供名称明确的方法。

3. `UrlEncode.encodeSafe()` 的名称暗示 RFC 3986 URL 编码，但实现基于表单编码
   `URLEncoder`，仍会编码 `~`，并且不能区分 query、path segment 等不同 URL 组件。
   修复方案：明确它只处理 `application/x-www-form-urlencoded`，或实现真正的 RFC 3986
   component encoder。

### 低优先级

4. `RegExpUtils.isMatch(Pattern, String)` 内部调用 `Matcher.find()`，而同类方法
   `match()` 才调用 `matches()`。“isMatch” 很容易被理解成整串匹配。
   修复方案：统一命名与语义，保留旧方法时标记弃用并在文档中明确是“查找子串”。

5. `StringBytes` 在未指定 charset 时存在回退到平台默认字符集的入口，跨机器结果不稳定。
   修复方案：文本与字节互转默认固定为 UTF-8，其他字符集必须显式指定。

## `com.ajaxjs.util.httpremote`

### 高优先级

1. `Request.connect()` 的日志包含完整 URL、完整请求体和响应体前 460 个字符。
   密码、token、签名、Cookie 或查询参数中的敏感信息可能直接进入日志和 MDC。
   修复方案：默认不记录 body，对 URL query、认证头和常见敏感字段做结构化脱敏；详细报文日志必须
   显式启用，并设置独立长度上限。

2. `SkipSSL.init()` 全局替换 JVM 的默认 HTTPS socket factory 和 hostname verifier，
   使进程内之后的所有 HTTPS 请求都信任任意证书与主机名。该修改没有恢复机制，初始化状态也没有
   安全发布。
   修复方案：删除生产 API 或明确限制为测试用途；返回作用于单个连接/客户端的 `SSLContext`，
   不修改 JVM 全局默认值。

3. `BatchDownload` 的下载线程即使收到 HTTP 错误或写文件抛出 `IOException`，仍可能返回目标路径。
   调用者会把失败或不完整文件当作成功结果。
   修复方案：先检查 `Response.isOk()` 和状态码，写入临时文件并在成功后原子移动；将下载异常通过
   `Future` 汇总给调用者。

4. `BatchDownload.start()` 为每个 URL 创建一个原生线程，固定等待 20 秒后不检查
   `awaitTermination()` 的返回值。方法可能在后台任务尚未结束时返回，大批 URL 还会导致线程资源
   失控。
   修复方案：使用有界线程池，允许配置总超时，取消超时任务，并返回每项成功/失败的完整结果。

### 中优先级

5. `Request.connect()` 要求调用者预先执行 `init()`，但没有状态校验，直接解引用 `conn`。
   调用顺序错误只会得到不明确的 `NullPointerException`；`initData()` 也有同样问题。
   修复方案：`connect()` 自动初始化，或引入显式状态机并抛出带操作提示的
   `IllegalStateException`。

6. HTTP 状态码小于 400 一律设置为成功，因此 3xx 重定向也被视为成功；这与 `Response` 中
   200—299 的成功说明不一致。
   修复方案：明确成功策略，默认仅接受 2xx；重定向是否自动跟随或作为独立结果处理应可配置。

7. `Request.connect()` 使用 `DataReader.readAsString()` 后再 `trim()`，会修改响应正文的换行和
   首尾空白。签名文本、模板、CSV 或纯文本下载可能因此损坏。
   修复方案：按响应 charset 原样读取，不做 trim；展示日志时才对副本截断或格式化。

8. `Response.responseAsJson()` 注释掉了 JSON Content-Type 检查，而
   `responseAsJsonList()` 仍会检查，两个入口行为不一致。
   修复方案：统一校验策略；若允许服务器错误标注 Content-Type，应提供显式的宽松模式。

9. `Request.setData*()` 遇到不支持的 Content-Type 时可能什么也不做，保留之前设置的旧请求体；
   multipart 分支还是空的 TODO。
   修复方案：不支持的类型立即抛出异常，任何一次 set 调用都不能静默沿用旧数据。

## `com.ajaxjs.util.io`

### 高优先级

1. `DataReader.readAsString()` 使用 `readLine()` 后统一追加系统换行符，既改变原有 LF/CRLF，又会给
   没有结尾换行的内容强行增加换行。它被 HTTP 和文件读取 API 复用，会静默修改数据。
   修复方案：用 `Reader` 的字符缓冲区直接复制，保留原始字符序列。
   单测：`TestDataReader.testReadAsString()`、
   `TestDataReader.readAsStringPreservesOriginalLineEndingsAndTrailingNewline()` 和
   `TestResources.readsResourceText()`。

2. `DataWriter.write(InputStream)` 会通过 `DataReader` 关闭调用者传入的输入流，但注释只强调输出流
   不会关闭，所有权约定不清晰且容易导致后续读取失败。
   修复方案：复制方法默认不关闭任一外部流；需要托管生命周期时提供名称明确的独立入口。
   单测：`TestDataWriter.writeDoesNotCloseCallerInputOrOutput()`。

### 中优先级

3. `DataWriter.write(byte[], off, length)` 把 `(0, 0)` 当作“写入整个数组”。这违反
   `OutputStream.write` 的常规约定：length 为 0 应写入零字节。
   修复方案：让三参数重载严格遵守 offset/length 语义；单参数重载直接写完整数组。
   单测：`TestDataWriter.zeroLengthWritesNoBytes()`。

4. `FileHelper.writeFileContent()` 使用平台默认字符集，而读取文本默认 UTF-8，同一 API 往返可能
   乱码。
   修复方案：默认统一 UTF-8，并提供接受 `Charset` 的重载。

5. `FileHelper.getFileContent()` 按行读取后重新拼接，会统一换行并丢失文件最后是否有换行的信息。
   修复方案：复用修正后的原样文本读取实现。
   单测：`TestFileHelper.readingTextPreservesLineEndingsAndTrailingNewline()`。

### 低优先级

6. `DataReader.readStreamAsBytes()` 把同一个复用缓冲区交给回调。回调若异步处理或保存数组引用，
   后续读取会覆盖之前的数据。
   修复方案：在契约中明确数组只在回调期间有效，或提供交付独立 byte[] 的安全重载。

## `com.ajaxjs.util.date`

### 单元测试确认

2026-07-30 使用 JDK 17 定向执行 `com.ajaxjs.util.date.Test*`：共 66 个测试，63 个通过、
2 个失败、1 个错误。失败对应下列 3 个问题。测试已作为回归用例保留，生产源码尚未修改。

### 中优先级

1. `DateTools` 的日期正则允许一位月、日、时，但之后选择的严格 formatter 使用
   `MM/dd/HH/mm/ss`。例如 `2024-1-1` 通过格式识别后仍解析失败。
   修复方案：正则与 formatter 接受范围保持一致；若要支持一位字段，为解析器单独构造可变宽度
   formatter。
   单测：`TestDateTools.object2DateSupportsWidthsAcceptedByItsFormatDetection()`。

2. `DateTypeConvert` 将 `LocalDate` 转换为 instant 类目标时使用 `atStartOfDay(zone)`。
   某些时区在当天零点发生 DST/规则切换时，该调用会把时间静默推进到下一个有效时刻。
   修复方案：明确“当天开始”的冲突策略；严格模式检查实际得到的 local date/time 是否仍符合输入。
   单测：`TestDateTypeConvert.testLocalDateRejectsSkippedStartOfDay()`。

3. `DateTypeConvert.to(null, zoneId)` 没有先校验目标类型，最终在拼接错误信息时调用
   `clz.getName()`，抛出缺少上下文的 `NullPointerException`。
   修复方案：在任何输入分支之前校验 `clz`，为空时抛出带明确消息的
   `IllegalArgumentException`。
   单测：`TestDateTypeConvert.testNullTargetTypeIsRejectedClearly()`。

## `com.ajaxjs.util.json`

### 高优先级

1. `Jackson2Engine` 在多处解析失败日志中直接输出完整 JSON。输入可能包含密码、访问令牌、个人信息
   或大体积正文，造成敏感数据泄露和日志放大。
   修复方案：日志只记录目标类型、输入长度、摘要/trace id 和异常；禁止默认记录原始 JSON。

### 中优先级

2. JSON 转换方法大量采用“记录日志并返回 null”的错误模型，无法区分合法 JSON `null`、输入为空和
   解析失败，也容易让错误延迟到后续空指针。
   修复方案：核心转换 API 抛出带 cause 的统一异常；如需容错，另提供名称明确的 `tryXxx` API。

## `com.ajaxjs.util.cryptography`

### 单元测试确认

2026-08-17 使用 JDK 8 定向执行 `com.ajaxjs.util.cryptography.**`：共 27 个测试，全部通过。
转换名与密钥算法混用、cipher 状态校验、敏感字段输出、GCM 参数校验、证书字段去引号和未生成
密钥对时的错误提示已经修复，不再列为待处理问题。

### 高优先级

1. `AES_encode()`、`DES_encode()` 和 Triple DES 便捷入口只传入算法名，没有显式指定 mode 和
   padding。在常见 JDK provider 中会落到 ECB/PKCS5Padding；ECB 会暴露重复明文块，并且这些入口
   都不提供密文完整性认证。
   修复方案：新增带随机 12 字节 nonce 和认证标签的 AES-GCM API；旧 AES/DES/3DES 方法只保留
   历史数据兼容，标记弃用，不直接改变已有密文格式。

2. `AES_encode(data, key)` 和 DES 便捷入口把口令字符串设置为 `SHA1PRNG` 的种子，再通过
   `KeyGenerator` 生成密钥。这不是标准口令派生函数，结果还可能依赖 provider 行为。
   修复方案：口令派生统一使用带随机 salt 和成本参数的 PBKDF2；真实随机 AES key 使用
   `SecretKey` 或固定长度字节数组传入。

3. RSA 加解密入口把 transformation 写成 `"RSA"`，具体 padding 由 provider 默认值决定，通常落到
   PKCS#1 v1.5；同一数据在不同 provider 下的行为也可能不同。
   修复方案：要求显式 transformation，新加密默认使用 OAEP（建议 SHA-256/MGF1），旧 PKCS#1
   仅保留兼容解密入口。

4. `KeyMgr.privateKeyEncrypt()` 和 `publicKeyDecrypt()` 把私钥运算包装成普通“加密/解密”，容易被
   错当成数字签名，但它不具备标准签名协议的安全属性。
   修复方案：这两个入口标记弃用，文档和新代码统一使用 `DoSignature`、`DoVerify`。

5. `Constant` 仍公开 `DES`、`MD5withRSA`、`PBEWITHMD5andDES`、OAEP-SHA1 等弱算法常量，部分还有
   便捷加解密方法，容易在新代码中误用。
   修复方案：弱算法 API 标记弃用并集中到 legacy 命名空间；文档明确仅允许兼容历史数据，默认示例
   使用 AES-GCM、SHA-256 以上签名和现代 OAEP。

### 中优先级

6. 当前 PBE 密文只包含 `nonce + ciphertext + tag`，salt、迭代次数、KDF 和格式版本依赖调用方
   在外部保存。以后调整迭代次数或算法时无法从密文本身判断版本。
   修复方案：设计带 magic、版本、KDF、迭代次数、salt 和 nonce 的自描述密文格式；旧格式保留
   独立解密入口。

7. PBE API 使用不可主动清除的 `String` 接收口令。内部虽然会清理 `PBEKeySpec`，调用方原始
   `String` 仍会留在堆中等待 GC。
   修复方案：增加接受 `char[]` 的重载并在完成后由调用方清理；旧 `String` 方法保留兼容。

8. `Cryptography`、签名和验证对象是可变对象，数组 setter/getter 直接保存或返回引用。调用方在
   设置后修改原数组，可能改变待加密、签名或验证的数据；对象也不适合跨线程或跨请求复用。
   修复方案：字节数组使用防御性复制，文档明确实例非线程安全；长期考虑不可变参数对象。

9. 为避免敏感字段进入 Lombok 生成的 `toString()`，key/data 等字段也从 `equals/hashCode` 排除。
   因此拥有不同密钥和数据、但配置相同的执行对象可能被判断相等。
   修复方案：加密执行对象不要定义值对象式相等语义，或者通过
   `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` 明确选择真正构成身份的字段。

10. `KeyMgr.action()` 一次把全部数据传给 RSA `Cipher.doFinal()`，超过单个 RSA block 的输入会直接
    失败，但 API 没有预检或说明长度限制。
    修复方案：RSA 只用于封装随机对称密钥并提供混合加密 API；至少应按 key/padding 预检最大长度并
    给出明确错误。

11. `CertificateUtils.getCert()` 只解析 X.509 证书并检查有效期，没有验证证书链、信任锚、签名、
    Key Usage、吊销状态、主机名或预期主体。调用者可能误以为返回的证书已经可信。
    修复方案：把解析和信任验证拆成两个 API；验证入口基于显式 `TrustAnchor` 和
    `CertPathValidator`，具体协议再校验主体及用途。

12. `CertificateUtils.deserializeToCerts()` 对远端 Map 结构进行多次未经校验的强制转换。响应结构
    缺失或类型变化时仍会产生难以定位的 `ClassCastException` 或空指针。
    修复方案：逐层校验 `data`、`encrypt_certificate` 及其字段类型，在异常中包含字段路径但不包含
    完整密文或密钥。

13. `CertificateUtils.getCert(InputStream)` 会关闭调用方传入的流。虽然当前 JavaDoc 已说明，但这与
    常见的“谁创建谁关闭”约定不同，复用流的调用方容易意外失败。
    修复方案：增加不关闭调用方流的解析入口；原方法为兼容保留现有行为。

14. `DoSignature` 和 `DoVerify` 完全接受调用方提供的算法字符串，没有安全算法白名单，新代码仍可
    选择 MD5withRSA 等弱算法。
    修复方案：增加推荐算法枚举或受限工厂，默认允许 SHA-256/384/512 with RSA 及 RSA-PSS；原始
    字符串构造器只作为高级兼容入口。

### 低优先级

15. `DoVerify.verify()` 对普通签名不匹配返回 `false`，但部分 provider 遇到畸形签名字节时会抛出
    `SignatureException` 并被包装成 `RuntimeException`。调用者难以稳定区分“不匹配”和“格式损坏”。
    修复方案：定义并测试统一契约，明确畸形签名应返回 `false` 还是抛出专用参数异常。

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

4. `Methods.findCompatibleMethod()` 不支持 Java 调用转换的完整规则。
   当前不支持数值基本类型扩宽（如 `Integer` 调用 `long`）和 varargs 展开。
   修复方案：在匹配评分中加入合法的 primitive widening 和可变参数阶段，并让固定参数方法优先。

5. 按名称执行时，找不到方法最终只抛出 `Method must not be null.`。
   异常没有目标类、方法名和参数类型，难以诊断。
   修复方案：在名称查找入口检测 `null`，抛出包含目标签名的 `IllegalArgumentException`。

## 已接受的边界

- `ReflectMethod` 已删除，方法查找和调用统一使用 `Methods`。
- `NewInstance` 只支持 public 构造器，不考虑 private 或其他非 public 构造器。
- `Methods.findDeclaredMethodByTypes()` 和 `findDeclaredMethod()` 的层次遍历不包含 `Object`。
- 日期年份只允许 1900—2099，不作为待修复问题。
