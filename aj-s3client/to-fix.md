# aj-s3client 待修复事项

最后审查日期：2026-08-12

审查范围：`src/main/java/com/ajaxjs/s3client`、`pom.xml` 和现有测试。

已经完成的修复包括：迁移到 `ajaxjs-util 1.3.6`、统一 HTTPS Endpoint
格式、基础配置校验、对象键编码、SigV2 GET 结果判断、Cloudflare R2 的
GET/DELETE、`Content-MD5` Base64 编码、ETag 基础兼容、SHA-256 异常处理，
以及移除无用的 Servlet 依赖。以下是当前仍需处理的问题。

## P0——安全与签名正确性

- [ ] **防止密钥通过 `toString()`、`equals()` 和 `hashCode()` 泄露。**
  `Config`、`AwsCredentials`、`SignBuilder`、`BaseS3Client` 及厂商客户端仍使用
  Lombok `@Data`。生成的方法会包含 `secretKey` 或持有凭据的对象，日志输出、
  调试信息和异常插值可能泄露密钥。应改为显式 getter，或至少使用
  `@ToString.Exclude` 和 `@EqualsAndHashCode.Exclude` 排除所有敏感字段；增加测试
  确认任何相关对象的字符串表示均不包含密钥。

- [ ] **所有 SigV4 请求必须签署真实的 `Host`。**
  当前只有 `isSetHost=true` 时才把 Host 加入 Canonical Headers，默认仅
  `Backblaze` 开启。SigV4 要求 Host 参与签名，Cloudflare R2 和 Scaleway
  仍可能生成无效签名。应删除该可选开关，始终从最终请求 URI 的
  `rawAuthority` 提取 Host（包括非默认端口），并保证签名值与实际请求头一致。

- [x] **验证 SigV4 Canonical Query 的协议正确性。**
  当前查询参数解析先搜索 `=`，再搜索后续 `&`，会把 `foo&bar=qux` 解析成一个
  参数；已经百分号编码的查询值也可能被再次编码。应对照 AWS 官方 SigV4 测试
  向量重写解析：保留重复参数和空值，按编码后的名称和值排序，并明确 raw query
  的单次编码规则。

- [x] **加固 Canonical Headers。**
  Header 名称使用默认 Locale 转小写，在土耳其语环境可能导致签名错误；值只压缩
  普通空格，没有完整处理 Tab 等线性空白，也没有拒绝 CR/LF。应使用
  `Locale.ROOT`，严格验证名称和值，按 SigV4 规则折叠空白，并防止请求头注入。

## P1——功能与可靠性

- [ ] **提供真正的对象下载 API。**
  现有 `boolean getObject(...)` 只确认 GET 请求是否返回 `2xx`，aj-util 会读取响应
  后丢弃内容，调用者得不到对象。应新增流式接口，例如接收 `OutputStream` 或
  `Path`，或返回受控的输入流；旧布尔方法可以保留兼容，但必须明确其仅做可访问性
  检查。下载实现还需处理流所有权、部分文件清理、Content-Length 和大文件。

- [ ] **增加流式上传，避免所有对象必须使用 `byte[]`。**
  当前 API 要求完整文件驻留堆内存，大文件和并发上传可能造成 OOM。应增加
  `InputStream + contentLength` 和 `Path` 重载，并使 HTTP 层直接流式传输；
  `byte[]` 仅保留为小对象便利入口。后续可增加 multipart upload。

- [ ] **用结构化结果替代单一布尔返回值。**
  当前 API 丢失 HTTP 状态、服务端错误码、Request ID、ETag 和网络异常，调用者
  无法区分远端拒绝、签名错误与连接失败。应引入 `S3Response<T>`，旧布尔方法委托
  `isSuccess()` 保持兼容，异常和日志不得包含凭据。

- [x] **重新定义 ETag 校验能力。**
  当前已兼容引号和全部 `2xx`，但分片上传、服务端加密或部分兼容服务的 ETag
  不等于对象 MD5。应仅在明确为单段、未加密上传时比较 MD5；其他情况把 ETag
  作为响应元数据返回，不应把 ETag 不等于 MD5 普遍判定为上传失败。

- [ ] **确认网易 NOS 的签名协议或移除该实现。**
  `Content-MD5` 已改为 Base64，而 ETag 使用十六进制比较，但网易 NOS 已属于旧式
  服务，当前无法通过离线测试证明其 canonical string 和 HMAC-SHA256 规则仍正确。
  若已停服，应移除或标记为 deprecated；若仍使用，应以服务商协议和真实集成测试
  验证 Content-Type、Content-MD5、签名串及 ETag。

- [ ] **完善配置模型和厂商约束。**
  已有公共边界校验，但 `Config` 仍是可变 Bean，配置可在请求签名期间被其他线程
  修改；bucket 校验也只是拒绝分隔符和控制字符，没有实现 DNS 风格命名规则。
  应改为不可变 Builder，在构造客户端时完成一次性验证，并由各厂商声明 region、
  path-style/virtual-hosted-style、bucket 规则和 endpoint 特性。

- [ ] **使用单一 URI 构建器统一签名路径和请求路径。**
  当前 SigV2 使用 virtual-hosted-style，SigV4 使用 path-style，逻辑分散在多个类。
  对象路径已编码，但仍应由统一 `S3UriBuilder` 同时生成实际 URL、Canonical URI
  和 Host，避免未来修改时再次出现签名路径与网络请求路径不一致。

## P2——设计、一致性与测试

- [x] **将签名模型改成不可变对象。**
  `SignBuilder`、`CanonicalRequest` 和 `CanonicalHeaders` 使用 `@Data` 并暴露 setter；
  `getS3Signature()` 还会修改传入请求的 `service`。并发复用或重复签名可能受到残留
  状态影响。应防御性复制集合，移除 setter，并让签名操作不修改调用方对象。

- [ ] **用类型化 DTO 表达 XML 列表结果。**
  `Map<String, String>` 无法可靠表示多个 `Bucket` 或 `Contents` 元素，重复节点可能
  被覆盖。应增加 `ListBucketsResult`、`ListObjectsResult` 等 DTO；raw XML 方法可
  保留兼容，并继续使用 aj-util 的安全 XML 入口。

- [ ] **整理 Provider 包和命名。**
  `factory` 包中的类实际上是客户端实现而不是工厂，应迁移到 `provider` 或
  `client`。可以先新增正确包并保留 deprecated 转发类，避免直接破坏二进制兼容。

- [x] **清理工具类与重复实现。**
  `S3Utils.isEmpty()`、`Throwables`、Base16 和部分 HMAC 逻辑与 JDK/aj-util 能力重叠。
  应评估删除重复工具，缩小维护面；安全关键摘要应只有一套实现和一致的异常策略。

- [ ] **修订 README 和遗留源码注释。**
  README 仍展示可泄露 `secretKey` 的 `@Data Config`，生产源码也残留中文或过时注释。
  应同步当前 Endpoint 约定、HTTPS 限制、对象键规则、异常行为和安全注意事项。

- [ ] **隔离并重写真实云服务测试。**
  旧测试依赖 `application.yml`、真实账号和 Windows 绝对路径。默认 Maven 目前只执行
  `*UnitTest`，这是安全的，但仍应把真实服务测试移动到 `integration-test` profile，
  凭据从环境变量注入，并增加本地 HTTP Server 测试 GET/PUT/DELETE 方法、编码后的
  URL、请求头、Content-MD5 和错误响应。

- [ ] **增加官方签名固定向量覆盖。**
  现有离线测试覆盖了哈希、HMAC 参数顺序、基础路径和响应判断，但没有完整验证
  AWS SigV4 Authorization 最终值，也没有覆盖 SigV2 厂商公开向量。应加入 AWS
  官方 canonical request、string-to-sign、derived signing key 和 signature 向量，
  以及 Host 带端口、重复查询参数、Unicode 和预编码百分号案例。

## 建议实施顺序

1. 密钥脱敏与强制 SigV4 Host 签名。
2. Canonical Query/Header 官方向量修复。
3. 真正的流式下载与上传 API。
4. 结构化响应和不可变配置。
5. 统一 URI 构建器与 ETag 能力模型。
6. XML DTO、Provider 包整理、README 和集成测试。

## 当前验证状态

- `mvn test`：13 个离线单元测试全部通过。
- `mvn -DskipTests package`：构建成功。
- `javadoc -Xdoclint:all`：通过，零警告。
- 尚未使用真实 Cloudflare R2、Backblaze、Scaleway、Aliyun OSS 或网易 NOS 凭据
  执行集成测试，因此不能据此宣称各服务商协议已完成端到端验证。
