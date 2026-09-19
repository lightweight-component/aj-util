---
title: 上传文件
layout: layouts/aj-util-cn.njk
alternate: /upload/
---

# 上传文件

`SimpleFtpClient.upload(本地路径, 远端路径)` 使用 FTP `STOR` 命令上传一个本地文件。它会切换到二进制模式，将文件流式写入数据连接，关闭数据连接，并在报告成功前检查最终 FTP 响应。

## 基本上传

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;

try (SimpleFtpClient ftp = new SimpleFtpClient("ftp.example.com", 21)) {
    ftp.login("deploy", System.getenv("FTP_PASSWORD"));
    ftp.upload("C:/build/app.zip", "/releases/app-1.5.zip");
}
```

第一个参数必须是可读取的本地文件；第二个参数是 FTP 服务端理解的远端文件名和路径。请确保目标目录已存在，且账户具有写权限；`upload` 不会创建远端目录。

## 路径、文件名与编码

除非目标服务端另有约定，远端路径应使用 `/`。服务端支持 UTF-8 时，请在登录后、发送非 ASCII 路径前调用 `enableUtf8()`：

```java
ftp.login("deploy", password);
ftp.enableUtf8();
ftp.upload("C:/exports/季度报告.zip", "/incoming/季度报告.zip");
```

如果服务端拒绝 `OPTS UTF8 ON`，`enableUtf8()` 会抛出 `IOException`。未确认服务端配置时，不应擅自假定其他编码可用。

## 完成确认与失败处理

FTP 传输同时包含数据连接和控制连接的最终响应。`upload` 会关闭数据流，并在内部调用 `completePendingCommand()`。因此，`upload` 正常返回代表客户端已经收到成功的最终响应，而不只是本地文件已经读完。

`IOException` 可能表示本地文件不可读、数据连接失败、权限不足、连接中断或服务端返回失败。应将其视为不完整上传。服务端可能留下部分远端文件；若下游消费者不能看到半成品，可先上传到临时名称，成功后再重命名：

```java
String finalName = "/releases/app.zip";
String temporaryName = finalName + ".part";

try (SimpleFtpClient ftp = new SimpleFtpClient(host, 21)) {
    ftp.login(user, password);
    ftp.upload(localFile, temporaryName);
    ftp.rename(temporaryName, finalName);
}
```

`rename` 需要服务端允许，且并非每种 FTP 服务端都保证原子操作。失败的临时文件应按服务端保留策略清理。

## 进度和超时

内置 `ProgressListener` 会在复制数据时通过 SLF4J 记录字节数，它不是业务回调 API。需要保存或显示传输进度时，请配置相应的 SLF4J 日志后端。

长时间传输前应设置合适的超时：`setConnectTimeout` 控制新建 Socket 连接，`setReadTimeout` 控制 Socket 读取。超时应覆盖预期文件大小和网络速度：

```java
ftp.setConnectTimeout(10_000);
ftp.setReadTimeout(120_000);
```

## 底层上传流

仅在需要自定义流写入时使用 `put(远端路径)`。调用方负责关闭其返回的流，并调用 `completePendingCommand()`；否则，即使字节已写入，也可能误以为上传已经完成。

```java
ftp.binary();
try (OutputStream remote = ftp.put("/incoming/data.bin")) {
    // 将业务数据写入 remote。
}
ftp.completePendingCommand();
```

返回 [aj-ftp 概览](/aj-ftp/cn/) 或继续阅读[下载文件](/aj-ftp/download-cn/)。
