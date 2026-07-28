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

2. `MapTool.deepCopy()` 实际只执行 `new HashMap<>(map)`，嵌套的 Map、Collection、数组和对象仍与
   原 Map 共享引用，与方法名和注释中的“深复制”不符。
   修复方案：实现明确支持范围内的递归复制，或将方法改名为 `shallowCopy()` 并修正文档。

### 中优先级

3. `ConvertBasicValue` 的整数转换使用 `Number.intValue()`、`longValue()` 等截断式转换。
   超出目标类型范围或把小数转换为整数时不会报错，可能静默产生溢出或精度丢失。
   修复方案：默认使用精确、带范围检查的转换；如需 Java 强制转换语义，另提供名称明确的方法。

4. `MapTool.mapToXml()` 直接把 Map key 当作 XML 元素名。空 key、以数字开头或含空格等非法名称会
   触发不明确的 `DOMException`；value 还会被 `trim()`，改变原始数据。
   修复方案：创建节点前校验 XML 名称并报告对应 key，且默认保留 value 的前后空白。

5. `StrUtil.simpleTpl(String, Object)` 假定每个属性描述符都有 getter。遇到 write-only JavaBean
   属性时，`getReadMethod()` 返回 `null`，随后调用会空指针。
   修复方案：跳过没有 read method 的属性，并为 getter 执行失败保留属性名和 cause。

6. `UrlEncode.encodeSafe()` 的名称暗示 RFC 3986 URL 编码，但实现基于表单编码
   `URLEncoder`，仍会编码 `~`，并且不能区分 query、path segment 等不同 URL 组件。
   修复方案：明确它只处理 `application/x-www-form-urlencoded`，或实现真正的 RFC 3986
   component encoder。

### 低优先级

7. `RegExpUtils.isMatch(Pattern, String)` 内部调用 `Matcher.find()`，而同类方法
   `match()` 才调用 `matches()`。“isMatch” 很容易被理解成整串匹配。
   修复方案：统一命名与语义，保留旧方法时标记弃用并在文档中明确是“查找子串”。

8. `StrUtil.join(T[])` 遇到 `null` 元素会空指针，而 List 重载会把它追加成字符串 `"null"`，
   两个同名 API 的行为不一致。
   修复方案：统一 null 元素策略，建议允许调用者指定空值文本或默认输出空串。

9. `StringBytes` 在未指定 charset 时存在回退到平台默认字符集的入口，跨机器结果不稳定。
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

10. `FileUpload.uploadFile()` 接受连接初始化回调 `fn`，但没有使用它，调用者设置请求头或其他连接
    参数不会生效。
    修复方案：将回调传给底层 `Request.init(fn)`，并增加验证自定义请求头的测试。

11. `FileUpload` 使用固定 boundary 和平台默认字符集组装 multipart，并把整个文件读入内存。
    空参数依赖 `assert` 检查，断言关闭时可能空指针；null value 也会直接调用 `toString()`。
    修复方案：生成随机 boundary，固定 UTF-8，流式写文件内容，并使用普通参数校验。

12. `BatchDownload` 从路径取文件名时只按反斜杠拆分，在 Unix 路径上会得到错误名称；它还会原地
    改写调用者传入的 URL 数组。
    修复方案：使用 `Path.getFileName()`，并在内部创建独立结果集合。

### 低优先级

13. `Head.gzip()` 对非 gzip 输入返回 `null`，解压失败也吞掉 `IOException` 后返回 `null`，
    无法区分“不需要解压”和“解压失败”。
    修复方案：非 gzip 原样返回输入流；格式损坏则抛出带 cause 的异常。

## `com.ajaxjs.util.io`

### 高优先级

1. `DataReader.readAsString()` 使用 `readLine()` 后统一追加系统换行符，既改变原有 LF/CRLF，又会给
   没有结尾换行的内容强行增加换行。它被 HTTP 和文件读取 API 复用，会静默修改数据。
   修复方案：用 `Reader` 的字符缓冲区直接复制，保留原始字符序列。

2. `DataWriter.write(InputStream)` 会通过 `DataReader` 关闭调用者传入的输入流，但注释只强调输出流
   不会关闭，所有权约定不清晰且容易导致后续读取失败。
   修复方案：复制方法默认不关闭任一外部流；需要托管生命周期时提供名称明确的独立入口。

### 中优先级

3. `DataWriter.write(byte[], off, length)` 把 `(0, 0)` 当作“写入整个数组”。这违反
   `OutputStream.write` 的常规约定：length 为 0 应写入零字节。
   修复方案：让三参数重载严格遵守 offset/length 语义；单参数重载直接写完整数组。

4. `FileHelper.writeFileContent()` 使用平台默认字符集，而读取文本默认 UTF-8，同一 API 往返可能
   乱码。
   修复方案：默认统一 UTF-8，并提供接受 `Charset` 的重载。

5. `FileHelper.getFileContent()` 按行读取后重新拼接，会统一换行并丢失文件最后是否有换行的信息。
   修复方案：复用修正后的原样文本读取实现。

6. `FileHelper.getFileSize()` 文档声称可取得文件或目录大小，但对目录只返回目录项自身的文件系统
   元数据大小，不是目录内容总大小。
   修复方案：改正文档为“单个路径元数据大小”，或另行实现带符号链接策略的递归目录大小。

7. `ZipHelper.isZipFile()` 只识别 `PK\003\004` 的 local-file-header 签名，合法的空 ZIP
   （以 `PK\005\006` 开始）会被判断为非 ZIP。
   修复方案：优先尝试打开 `ZipFile`，或完整识别 ZIP 规范允许的签名并校验结构。

8. `CmdHelper.exec(String)` 依赖 `Runtime.exec(String)` 的平台相关分词规则，没有超时，也没有检查
   进程退出码。带空格/引号的参数容易执行错误，子进程还可能无限运行。
   修复方案：核心入口接受参数列表并使用 `ProcessBuilder`，支持超时、取消和退出码检查。

9. `Resources` 对“资源不存在”的处理不一致：部分方法返回 `null`，部分方法抛异常，还有方法直接向
   `stderr` 输出；调用者难以建立稳定错误处理逻辑。
   修复方案：统一为明确异常或 `Optional`，且不直接写标准错误。

### 低优先级

10. `DataReader.readStreamAsBytes()` 把同一个复用缓冲区交给回调。回调若异步处理或保存数组引用，
    后续读取会覆盖之前的数据。
    修复方案：在契约中明确数组只在回调期间有效，或提供交付独立 byte[] 的安全重载。

11. `Resources.url2path()` 把 classpath URL 直接转成文件系统路径；资源位于 JAR 内时并不存在可用的
    普通 `Path`。
    修复方案：文件资源才返回 `Path`，JAR 资源提供 `InputStream`/URL API 或显式挂载文件系统。

## `com.ajaxjs.util.date`

### 中优先级

1. `DateTools` 的日期正则允许一位月、日、时、分、秒，但之后选择的严格 formatter 使用
   `MM/dd/HH/mm/ss`。例如 `2024-1-1` 通过格式识别后仍解析失败。
   修复方案：正则与 formatter 接受范围保持一致；若要支持一位字段，为解析器单独构造可变宽度
   formatter。

2. `DateTypeConvert` 将 `LocalDate` 转换为 instant 类目标时使用 `atStartOfDay(zone)`。
   某些时区在当天零点发生 DST/规则切换时，该调用会把时间静默推进到下一个有效时刻。
   修复方案：明确“当天开始”的冲突策略；严格模式检查实际得到的 local date/time 是否仍符合输入。

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

### 高优先级

1. RSA 加解密入口把 transformation 写成 `"RSA"`，具体 padding 由 provider 默认值决定，通常落到
   PKCS#1 v1.5；同一数据在不同 provider 下的行为也可能不同。
   修复方案：要求显式 transformation，新加密默认使用 OAEP（建议 SHA-256/MGF1），旧 PKCS#1
   仅保留兼容解密入口。

### 中优先级

2. `KeyMgr.action()` 一次把全部数据传给 RSA `Cipher.doFinal()`，超过单个 RSA block 的输入会直接
   失败，但 API 没有预检或说明长度限制。
   修复方案：RSA 只用于封装随机对称密钥并提供混合加密 API；至少应按 key/padding 预检最大长度并
   给出明确错误。

3. `SecretKeyMgr.getRandom()` 用调用者提供的字符串对 `SecureRandom.setSeed()`，但 setSeed 只是
   混入熵，并不保证可复现；方法注释容易让人把它当作确定性密钥派生。
   修复方案：密码派生统一使用 PBKDF2/Argon2 等 KDF；随机数 API 不接受“密钥字符串”作为种子。

4. `Constant` 仍公开 `DES`、`MD5withRSA`、`PBEWITHMD5andDES`、OAEP-SHA1 等弱算法常量，部分还有
   便捷加解密方法，容易在新代码中误用。
   修复方案：弱算法 API 标记弃用并集中到 legacy 命名空间；文档明确仅允许兼容历史数据，默认示例
   使用 AES-GCM、SHA-256 以上签名和现代 OAEP。

### 低优先级

5. `CertificateUtils.deserializeToCerts()` 对远端 Map 结构进行多次未经校验的强制转换，并对缺失值
   直接 `toString()`。响应结构稍有变化就会产生难以定位的 `ClassCastException` 或空指针。
   修复方案：逐层校验字段类型和必填字段，并在异常中包含字段路径。

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
