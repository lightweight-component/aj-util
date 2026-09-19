---
title: aj-ftp
layout: layouts/aj-util-cn.njk
---

# aj-ftp

`aj-ftp` 是一个面向 Java 8 及以上环境的轻量级纯 FTP 客户端，用于连接、认证、上传和下载文件；如有特殊协议需求，也可使用其底层 FTP API。

> aj-ftp 仅实现 FTP，不支持 FTPS 或 SFTP。

## 添加依赖

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-ftp</artifactId>
    <version>1.5</version>
</dependency>
```

## 快速开始

使用 FTP 主机和端口创建 `SimpleFtpClient`，认证后执行所需操作，并让 try-with-resources 关闭控制连接。FTP 控制连接通常使用 `21` 端口。

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;

try (SimpleFtpClient ftp = new SimpleFtpClient("ftp.example.com", 21)) {
    ftp.setConnectTimeout(10_000);
    ftp.setReadTimeout(60_000);
    ftp.login("username", "password");
    ftp.enableUtf8(); // 仅在服务端支持 UTF-8 路径时启用

    ftp.upload("C:/exports/report.zip", "/incoming/report.zip");
    ftp.getFile("/releases/client.zip", "C:/downloads/client.zip");
}
```

构造函数会建立控制连接，`login` 完成用户认证；认证和文件传输失败都会抛出 `IOException`。不要记录密码，也不要将凭据直接写入源码。

## 选择操作

| 任务 | 高层 API | 说明 |
| --- | --- | --- |
| 上传本地文件 | `upload(本地路径, 远端路径)` | 以二进制模式传输，并校验服务端最终响应。 |
| 下载远端文件 | `getFile(远端路径, 本地路径)` | 先写临时文件，再替换目标文件。 |
| 匿名 FTP | `anonymousLogin()` | 仅在服务端明确允许匿名访问时使用。 |
| 非 ASCII 路径 | `enableUtf8()` | 连接后、发送非 ASCII 凭据或路径前调用。 |

继续阅读[上传文件](/aj-ftp/upload-cn/)或[下载文件](/aj-ftp/download-cn/)。

## 连接与传输说明

- 对允许匿名访问的服务器使用 `anonymousLogin()`。
- 文件名包含非 ASCII 字符且服务端支持 UTF-8 时，应在登录后调用 `enableUtf8()`。
- 使用 `setConnectTimeout(毫秒)` 和 `setReadTimeout(毫秒)` 配置连接与读取超时。
- 文件传输使用二进制模式；内置 `ProgressListener` 会通过已配置的 SLF4J 日志输出已传输字节数。
- 一个 `SimpleFtpClient` 实例应只由一个调用方使用，不要在多个线程之间共享。

## 底层 API

`SimpleFtpClient` 继承自 `com.ajaxjs.net.ftp.sun.ftp.FtpClient`。常规业务优先使用 `upload` 和 `getFile`。如果调用返回数据流的底层方法，必须完成读写并关闭数据流，再调用 `completePendingCommand()` 检查服务端最终响应。

## 相关资源

- [源代码](https://github.com/lightweight-component/aj-util/tree/main/aj-ftp)
- [Maven Central](https://central.sonatype.com/artifact/com.ajaxjs/aj-ftp)
- [Javadoc](https://javadoc.io/doc/com.ajaxjs/ajaxjs-ftp)
