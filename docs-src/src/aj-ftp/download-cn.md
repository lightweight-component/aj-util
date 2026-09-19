---
title: 下载文件
layout: layouts/aj-util-cn.njk
alternate: /download/
---

# 下载文件

`SimpleFtpClient.getFile(远端路径, 本地路径)` 使用 FTP `RETR` 命令获取一个远端文件。它以二进制模式传输，并避免失败传输破坏已有的本地目标文件。

## 基本下载

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;

try (SimpleFtpClient ftp = new SimpleFtpClient("ftp.example.com", 21)) {
    ftp.login("reader", System.getenv("FTP_PASSWORD"));
    ftp.getFile("/releases/client-1.5.zip", "C:/downloads/client-1.5.zip");
}
```

远端路径由 FTP 服务端解释。本地目标文件的父目录必须已经存在，因为 aj-ftp 会在目标文件旁创建临时文件。请先创建目录：

```java
Path downloads = Paths.get("C:/downloads");
Files.createDirectories(downloads);
ftp.getFile("/releases/client.zip", downloads.resolve("client.zip").toString());
```

## 安全发布本地文件

aj-ftp 不会直接写入最终目标。它会先向服务端查询文件大小，在目标文件的父目录中创建临时 `.part` 文件，再将远端内容流式写入临时文件。只有在数据流关闭且最终 FTP 响应成功后，临时文件才会移动到请求的目标路径。

因此，中断或被拒绝的传输不会替换已有目标文件；失败时临时文件会被清理。最终移动会优先使用原子替换；当文件系统不支持原子移动时，则回退为普通替换。

成功传输后，目标文件仍会被替换。建议使用带版本的目标文件名，或先下载至暂存目录，完成校验后再让其他进程使用。

## 文件不存在与未知大小

如果服务端拒绝获取文件、连接失败、无法创建本地临时文件，或不能发布已完成文件，`getFile` 会抛出 `IOException`。请在任务边界处理该异常，保留旧目标文件，等待重试成功后再更新。

客户端会使用 `SIZE` 查询文件大小以记录传输进度。一些 FTP 服务端不会提供有效大小，传输仍可继续，但日志中的总大小未知。不要将进度日志视为完整性校验；FTP 本身不校验文件摘要，需要完整性保证时应从可信来源取得校验值。

## UTF-8 文件名与超时

服务端支持 UTF-8 路径时，应在登录后、发送含非 ASCII 字符的请求前启用 UTF-8：

```java
ftp.login(user, password);
ftp.enableUtf8();
ftp.getFile("/发布/客户端.zip", "C:/downloads/客户端.zip");
```

开始传输前配置连接和读取超时。读取超时过短可能在慢速网络上中断大文件，完全不设置超时则可能在网络故障后无限等待。

```java
ftp.setConnectTimeout(10_000);
ftp.setReadTimeout(120_000);
```

## 底层获取流

仅在需要自定义消费远端内容（例如解析远端文本文件）时使用 `get(远端路径)`。必须完整读取或关闭返回流，再调用 `completePendingCommand()`。

```java
ftp.binary();
try (InputStream remote = ftp.get("/data/items.csv")) {
    // 在此消费远端数据。
}
ftp.completePendingCommand();
```

高层 `getFile` 已处理这些步骤，是文件下载的首选。

返回 [aj-ftp 概览](/aj-ftp/cn/) 或继续阅读[上传文件](/aj-ftp/upload-cn/)。
