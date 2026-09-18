---
title: 上传、下载与代理限制
layout: layouts/aj-util-cn.njk
---

# Multipart 上传

```java
import com.ajaxjs.util.httpremote.MultipartPost;
import com.ajaxjs.util.httpremote.model.Response;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

Map<String, Object> parts = new LinkedHashMap<>();
parts.put("name", "Ada");
parts.put("document", new File("document.pdf"));
Response response = MultipartPost.upload("https://example.com/upload", parts,
        connection -> connection.setReadTimeout(30_000));
if (!response.isOk()) {
    throw new IllegalStateException("Upload failed: " + response.getHttpCode(), response.getEx());
}
```

`MultipartPost.uploadFile` 接受 `(url, fieldName, File, callback)` 或 `(url, fieldName, fileName, byte[]/InputStream, callback)`。**调用方负责关闭传入的输入流**。内部打开的文件流会关闭。上传流程管理并关闭请求输出流，读取并关闭响应输入流，在 `finally` 中断开连接。init 回调仅配置连接。

每次上传生成 boundary，回调后重新设置匹配的内容类型，使用 8192 字节分块传输并禁止重定向，避免自动重放上传。非 2xx 返回失败的 `Response`；校验、回调或写入错误可能抛出。若需重定向，请明确制定重放和目标校验策略。

`MultipartWriter` 仅负责编码：传入 `OutputStream` 和可选 boundary，然后**只调用一次** `write(...)` 写完整正文。每次调用都输出结束 boundary，多字段/文件须放在一个 Map 中，不能多次追加。HTTP 头使用 `getContentType()` 的值。它不关闭也不刷新调用方输出流，不关闭调用方输入流。boundary 须匹配 `[A-Za-z0-9_-]{1,70}`。头部/文本使用 UTF-8；null 值变为空文本。名称中的 CR/LF 被拒绝，引号/反斜杠会转义。字节/流使用专用重载，不要依赖内部 Map 键元数据。I/O 错误包装为 `UncheckedIOException`。

## 文件下载

```java
import com.ajaxjs.util.httpremote.HttpFileDownload;

new HttpFileDownload("https://example.com/file.pdf", "downloads/report.pdf").download();
new HttpFileDownload(new String[] {
        "https://example.com/a.pdf", "https://example.com/b.pdf"
}, "downloads").downloadAllAsync().join();
```

- `download()` / `downloadAsync()` 处理单个 URL；`downloadAll()` 顺序下载；`downloadAllAsync()` 返回代表全部任务的 `CompletableFuture<Void>`。必须 join 或处理 future 才能观察异步失败（join 抛 `CompletionException`）。
- 异步任务使用共享的四线程守护线程池和无界队列，不是每个 URL 一个线程。没有公开的执行器/取消策略或结果文件名列表。构造函数保留传入的 URL 数组，使用期间不要修改它。
- 显式目标文件优先。目录目标的文件名顺序为 `Content-Disposition`（优先 UTF-8 `filename*`）、URL 路径、生成的后备名称。服务端/路径名称仅保留 basename 并移除控制字符。
- String 目标以 `/` 或 `\` 结尾，或目标已是目录时按目录处理。尚不存在的 `Path` 目标按文件处理；批量目标始终是目录。
- 只接受 HTTP/HTTPS 和 2xx。连接/读取超时固定为 10/30 秒，启用重定向。数据流式写入同目录临时文件，再移动并替换目标。流会关闭，连接会断开，并尝试清理临时文件。**不保证原子移动**。
- 没有下载大小限制、目标沙箱或重名策略。已有文件可能被替换；并发同名下载可能互相覆盖。使用可信目标目录，并校验不可信 URL/重定向以防 SSRF。文件名清理不是完整的文件系统安全策略。

## 注解代理：仅支持已实现的子集

```java
import com.ajaxjs.util.httpremote.call.CallHandler;
import com.ajaxjs.util.httpremote.call.annotation.GET;
import com.ajaxjs.util.httpremote.call.annotation.Url;

public class ApiExample {
    @Url("https://example.com")
    public interface Api {
        @GET("/health")
        String health();
    }

    public static String health() {
        return CallHandler.create(Api.class).health();
    }
}
```

当前 `CallHandler` 分派 `@GET`、`@POST`、`@PUT`、`@DELETE`，返回 String、Map 或 Bean。POST/PUT 选择**第一个 Map 参数**作为正文；`@POST` 默认 FORM，`@PUT` 默认 JSON_BODY。类级 `@Url.initConnection` 在方法级初始化器之前执行；初始化器会缓存，必须可以安全复用，并提供 public 无参构造函数。

路径替换依赖 Java 参数名（编译时使用 `-parameters`），直接使用 `toString()`，不做 URL 编码。URL 拼接只是简单连接，不是 URI 解析；请自行构造并校验编码后的值。

**不能**仅因源码存在注解/接口就认为已支持：

- 分派器不处理 `@HEAD`、`@Header`、`@RawBody`、`@FormData` 和 `@Url.config`。
- `PayloadType.FILE_UPLOAD` 走尚不支持的普通 multipart Map 路径，请直接使用 `MultipartPost`。
- `create2()` 调用 `BaseCall.init()`，但处理器没有该生命周期/默认方法的分派实现，不要作为可用的初始化 API。Object 方法和泛型集合返回类型也没有专门处理。
- 代理调用不暴露带原始状态的响应或显式断开连接能力。关键错误/资源处理优先使用直接请求生命周期。

## 流、TLS 与日志

二进制响应应在连接前安装 `Request.setInputStreamConsumer`，在回调内读取，之后流会关闭。`Head.gzip(connection, in)` 只包装单个、不区分大小写的 `gzip` 编码 token（允许外围空白）；无编码或组合编码会返回原流。构造 `GZIPInputStream` 时的 `IOException` 会包装为 `UncheckedIOException`；之后读取返回的流仍可能抛出受检异常 `IOException`，包括数据损坏或截断的情况。不会自动解压。

保持正常的 TLS 信任和主机名检查。`SkipSSL.init()` 修改 JVM 全局默认值，信任所有证书和主机名；`setSSL_Ignore` 对单个连接关闭两种校验。`getSocketFactory` 即便配置了客户端 key manager 也仍信任全部服务端，失败时可能返回 null。`loadCert` 为包级可见，不是应用可调用的公开 API。这些不是生产环境信任库配置工具。

`Request.printLog` 在 info 级别记录 URL、UTF-8 请求正文和最多 460 字符的响应，并将格式化文本放入 MDC。截断不等于脱敏。防止凭据/个人信息泄漏，不要通过信任所有 TLS 证书来“解决”生产证书错误。

依据：`MultipartPost`、`MultipartWriter`、`HttpFileDownload`、`CallHandler`、`Head`、`SkipSSL`；本地测试 `TestMultipartPost`、`TestMultipartWriter`、`TestHttpIoHelpers`。现有下载和代理测试包含网络/环境假设，不构成安全证明。
