---
title: 上传与下载
layout: layouts/aj-util-cn.njk
alternate: /http_request/Transfer/
---

# 上传与下载

`MultipartPost` 用于 `multipart/form-data` 上传，`HttpFileDownload` 用于接收 HTTP 服务器提供的文件。两者都以流方式传输，不会先把整个文件收集到 Java 字节数组中。

## Multipart 上传

`MultipartPost.upload(...)` 接收一个同时包含文本和 `File` 值的 Map，且 Map 不能为空。回调只用于配置连接；不要在其中连接、读取或写入。

```java
import com.ajaxjs.util.httpremote.MultipartPost;
import com.ajaxjs.util.httpremote.model.Response;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

Map<String, Object> parts = new LinkedHashMap<>();
parts.put("title", "季度报告");
parts.put("document", new File("reports/q1.pdf"));

Response response = MultipartPost.upload("https://example.com/upload", parts, connection -> {
    connection.setConnectTimeout(5_000);
    connection.setReadTimeout(30_000);
    connection.setRequestProperty("Authorization", "Bearer " + token);
});

if (!response.isOk())
    throw new IllegalStateException("Upload failed: " + response.getHttpCode(), response.getEx());
```

单个文件可使用 `uploadFile`，支持 `File`、`byte[]` 与 `InputStream`：

```java
Response response = MultipartPost.uploadFile(
        "https://example.com/avatar", "avatar", "me.png", imageBytes, null);

try (InputStream content = Files.newInputStream(Paths.get("large-video.mp4"))) {
    // MultipartPost 不关闭传入的流；由这个 try 块负责关闭。
    response = MultipartPost.uploadFile(
            "https://example.com/video", "video", "large-video.mp4", content, null);
}
```

该工具会创建新的 boundary，在回调之后重新写入匹配的 `Content-Type`，启用 8 KiB 分块传输，关闭请求输出，读取并关闭响应流，最后在 `finally` 断开连接。它有意禁用重定向：把可能很大的上传自动重放到未经审核的地址并不安全。非 2xx 状态返回失败的 `Response`；校验、回调、正文写入和运行时错误仍可能直接抛出。

## 由既有连接持有输出流时：MultipartWriter

`MultipartWriter` 只序列化完整的 multipart 正文，不创建 HTTP 连接、不刷新/关闭输出流，也不关闭调用方传入的输入流。请求头应使用 `getContentType()`。

```java
String boundary = "upload-2026";
MultipartWriter writer = new MultipartWriter(connection.getOutputStream(), boundary);
connection.setRequestProperty("Content-Type", writer.getContentType());

Map<String, Object> parts = new LinkedHashMap<>();
parts.put("comment", "hello");
parts.put("file", new File("notes.txt"));
writer.write(parts);             // 一次调用会写入结束 boundary
connection.getOutputStream().flush();
```

每次 `write(...)` 都会以结束 boundary 收尾。所有字段和文件应放在一个 Map 中并只调用一次；多次调用不能再追加合法条目。boundary 必须匹配 `[A-Za-z0-9_-]{1,70}`。头部和文本使用 UTF-8；null 文本值会成为空文本，名称中的 CR/LF 会被拒绝，引号和反斜杠会转义。需要文件名的字节数组/流请使用专用重载；内部使用的 Map key 编码不是公开协议。

## 下载文件

```java
import com.ajaxjs.util.httpremote.HttpFileDownload;

new HttpFileDownload("https://example.com/files/report.pdf", "downloads/report.pdf")
        .download();

new HttpFileDownload(new String[] {
        "https://example.com/a.pdf",
        "https://example.com/b.pdf"
}, "downloads").downloadAllAsync().join();
```

| API | 行为 |
| --- | --- |
| `download()` | 在当前线程下载一个 URL。 |
| `downloadAsync()` | 返回一个下载任务的 future。 |
| `downloadAll()` | 按顺序下载传入的 URL。 |
| `downloadAllAsync()` | 返回代表所有排队下载任务的 `CompletableFuture<Void>`。 |

异步任务使用共享的四线程守护线程池与无界队列。必须 `join`、`get` 或以其他方式处理返回的 future；`join()` 会以 `CompletionException` 报告任务失败。没有公开执行器、取消策略或结果文件名列表。批量构造器保留传入的 URL 数组，使用期间不要修改它。

## 目标名称、覆盖与信任边界

显式指定文件目标时该目标优先。目录目标的文件名顺序为 `Content-Disposition`（优先 `filename*`）、URL 路径、生成的后备名。服务端/路径名称会被压缩为 basename 并移除控制字符。以 `/` 或 `\\` 结尾的 String，或一个已存在的目录，视为目录目标；尚不存在的 `Path` 视为文件；批量目标总是目录。

仅接受 HTTP/HTTPS URL 和 2xx 响应。下载器使用固定的 10 秒连接、30 秒读取超时，跟随重定向，流式写入同目录临时文件后再替换目标。它会关闭流、断开连接，并尝试清理临时文件；这降低了半成品概率，但**不保证原子移动**。

没有下载大小上限、目标沙箱或重名策略。已有文件可能被覆盖；并发下载得到相同名称时可能竞争。请使用可信目标目录，并校验不可信 URL/重定向，避免 SSRF 或访问不希望暴露的内网地址。

## 实现原理与取舍

普通 `Request.setData(Map)` 有意不实现 multipart 转换，面对 multipart 内容类型会抛出异常。将正文编码放在 `MultipartWriter`、传输生命周期放在 `MultipartPost`，让流所有权清晰可见，并避免隐藏的全量内存缓冲。

继续阅读[注解代理、流与 TLS](/aj-http/http_request/ProxySecurity-cn/)，或返回[请求生命周期](/aj-http/http_request/Base-cn/)。

