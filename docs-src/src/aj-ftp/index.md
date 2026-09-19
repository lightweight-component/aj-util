---
title: aj-ftp
layout: layouts/aj-util.njk
---

# aj-ftp

`aj-ftp` is a small Java 8+ client for plain FTP file transfers. It provides a concise API for connecting, authenticating, uploading, and downloading files, while retaining access to the lower-level FTP client when an application needs it.

> aj-ftp implements FTP only. It does not provide FTPS or SFTP support.

## Add the dependency

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-ftp</artifactId>
    <version>1.5</version>
</dependency>
```

## Quick start

Create `SimpleFtpClient` with the FTP host and port, authenticate, perform the required operation, then let try-with-resources close the control connection. Port `21` is the usual FTP control port.

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;

try (SimpleFtpClient ftp = new SimpleFtpClient("ftp.example.com", 21)) {
    ftp.setConnectTimeout(10_000);
    ftp.setReadTimeout(60_000);
    ftp.login("username", "password");
    ftp.enableUtf8(); // Only when the server supports UTF-8 paths.

    ftp.upload("C:/exports/report.zip", "/incoming/report.zip");
    ftp.getFile("/releases/client.zip", "C:/downloads/client.zip");
}
```

The constructor opens the control connection. `login` completes user authentication; both authentication and transfers report failures as `IOException`. Do not log credentials or place them directly in source code.

## Choose an operation

| Task | High-level API | Notes |
| --- | --- | --- |
| Upload a local file | `upload(localPath, remotePath)` | Sends the file in binary mode and validates the final server reply. |
| Download a remote file | `getFile(remotePath, localPath)` | Writes a temporary file before replacing the destination. |
| Anonymous FTP | `anonymousLogin()` | Use only when the server explicitly permits anonymous access. |
| Non-ASCII paths | `enableUtf8()` | Call after connecting, before sending non-ASCII credentials or paths. |

Continue with [uploading files](/aj-ftp/upload/) or [downloading files](/aj-ftp/download/).

## Connection and transfer notes

- Use `anonymousLogin()` for FTP servers that permit anonymous access.
- Call `enableUtf8()` after login when file names contain non-ASCII characters and the server supports UTF-8.
- `setConnectTimeout(milliseconds)` and `setReadTimeout(milliseconds)` configure the client connection.
- Transfers use binary mode. The built-in `ProgressListener` writes byte-count progress to the configured SLF4J logger.
- A `SimpleFtpClient` instance is intended for one caller at a time; do not share it between concurrent threads.

## Low-level API

`SimpleFtpClient` extends `com.ajaxjs.net.ftp.sun.ftp.FtpClient`. Prefer the high-level `upload` and `getFile` methods for normal application code. When using a low-level method that returns a data stream, consume or write the stream, close it, and then call `completePendingCommand()` to check the server's final reply.

## Resources

- [Source code](https://github.com/lightweight-component/aj-util/tree/main/aj-ftp)
- [Maven Central](https://central.sonatype.com/artifact/com.ajaxjs/aj-ftp)
- [Javadoc](https://javadoc.io/doc/com.ajaxjs/ajaxjs-ftp)
