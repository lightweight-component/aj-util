---
title: 请求生命周期与响应
layout: layouts/aj-util-cn.njk
---

# 请求生命周期

请安装 [AJ HTTP](/aj-http/cn/)，而不只是 AJ Util。`Request` 位于 `com.ajaxjs.util.httpremote`；`Response`、`HttpMethod`、`HttpConstant` 和 `PayloadType` 位于 `com.ajaxjs.util.httpremote.model`。

1. 创建 `new Request(HttpMethod.GET, url)`（或方法类的 `(HttpMethod, String)` 构造函数），此时**不会**发送请求。
2. 在 `init()` 前设置内容类型、正文与超时。默认连接超时 10,000 ms，读取超时 15,000 ms。
3. `init(Consumer<HttpURLConnection>)` 创建并配置连接。回调仅用于设置请求头、超时、重定向等，不应开始 I/O。只接受 HTTP/HTTPS URL。
4. 有正文时，在 `init()` 后、`connect()` 前调用 `initData()`。
5. `connect()` 读取状态和响应。自行管理生命周期时，处理响应后在 `finally` 中断开连接。

`Get(String, Consumer)`、`Delete(String, Consumer)` 以及带数据的 `Post`/`Put` 构造函数会立即发送。`Head(String)` 则仅配置对象，需要调用 `init()` 和 `connect()`。不要在自动发送的构造函数之后再次发送。请求对象可变，不要在并发操作间共享。

## 显式生命周期的 JSON POST

```java
import com.ajaxjs.util.httpremote.Request;
import com.ajaxjs.util.httpremote.model.HttpConstant;
import com.ajaxjs.util.httpremote.model.HttpMethod;
import com.ajaxjs.util.httpremote.model.Response;
import java.net.HttpURLConnection;
import java.util.Collections;

Request request = new Request(HttpMethod.POST, "https://example.com/api");
request.setContentType(HttpConstant.CONTENT_TYPE_JSON);
request.setData(Collections.<String, Object>singletonMap("name", "Ada"));
HttpURLConnection connection = request.init();
try {
    request.initData();
    Response response = request.connect();
    if (!response.isOk()) {
        throw new IllegalStateException("HTTP request failed: " + response.getHttpCode(), response.getEx());
    }
    // 仅在接口约定返回 JSON 对象时解析。
    java.util.Map<String, Object> result = response.responseAsJson();
} finally {
    connection.disconnect();
}
```

### 正文转换

- `setData(byte[])` 不转换内容，直接发送字节。单独设置正确的内容类型；任意文本、XML 或二进制内容优先使用此方法。
- `setData(String)` 接受类似 JSON 的文本（去除首尾空白后以 `{` 或 `[` 开头，不是完整校验）。JSON 类型原样以 UTF-8 发送；表单类型先解析为对象再连接字段。
- `setDataStr(String)` 接受已构造好的 `a=1&b=2`。表单类型原样以 UTF-8 发送；JSON 类型经 `MapTool.toMap` 转换。
- `setData(Map<String,Object>)` / `setData(Object)` 序列化 JSON 或表单。表单转换仅编码**值**，不编码键。使用简单字段名，不要将其视为通用查询串构造器。
- 非字节重载须先设置内容类型。此处的 multipart Map 转换会抛出 `UnsupportedOperationException`，请改用 `MultipartPost`。
- 自动发送的 `BasePost` 构造函数只处理无外围空白的 JSON 形状字符串或含 `=` 的字符串。其他字符串可能不生成正文。优先使用明确的 Map 或 UTF-8 字节数组。

## 响应与失败

`Response.isOk()` 表示 200–299。其他状态读取错误流（如果有）；禁止重定向时，3xx 可能没有文本。除非另行配置，否则采用 `HttpURLConnection` 的重定向行为。`Response.isOk(Map)` 是不同的 AJ-Spring 业务辅助方法，检查 `status.toString()` 是否为 `"1"`。

- `connect()` 捕获受检 `IOException`，设置 `isOk=false` 和 `ex`；HTTP 错误通常没有异常。未取得状态时 `httpCode` 可能为 null。
- 初始化错误、写入失败（`UncheckedIOException`）、回调/运行时/JSON 解析错误可能直接抛出。本地 `DataReader` 也将读取错误包装为 `UncheckedIOException`，不在 `connect()` 的受检 I/O 捕获范围内。并非所有错误都进入 `Response.ex`。
- `responseAsJson()` 返回 Map，空文本返回 null，不检查状态或内容类型。`responseAsJsonList()` 使用大小写敏感的响应头 Map 查找来检查类型。`responseAsBean(Class)` 在非成功状态返回 null；`responseAsXML()` 不论状态如何都会解析非空文本。格式错误可能抛异常。
- 默认通过 `DataReader` 以 UTF-8 读取，规范化换行并追加末尾平台换行符。当前 `Request` **不会** trim 保存的文本。该路径不保留原始字节，也不自动解压 gzip。
- `setInputStreamConsumer(...)` 跳过文本转换，请在回调内同步读取。回调后输入流会关闭，错误流也一样。`Request` 不填充 `Response.in`。`initData()` 刷新并关闭输出流；`Request.connect()` 不断开连接。

依据：`Request.java`、`BasePost.java`、`model/Response.java` 和 AJ Util `io/DataReader.java`。`TestMultipartPost` 的部分断言期望没有末尾换行，与本地 reader 不一致。HTTP POM 解析的是指定版本的 AJ Util 依赖，使用前应核对实际解析到的制品，不能假定文本行为完全相同。

继续阅读[方法辅助类](/aj-http/http_request/Get-cn/)及[流式处理与安全](/aj-http/http_request/advanced-usage-cn/)。
