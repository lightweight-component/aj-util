---
title: Upload files
layout: layouts/aj-util.njk
alternate: /upload-cn/
---

# Upload files

`SimpleFtpClient.upload(localPath, remotePath)` sends one local file with the FTP `STOR` command. It selects binary mode, streams the file to the data connection, closes that connection, and checks the final FTP reply before reporting success.

## Basic upload

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;

try (SimpleFtpClient ftp = new SimpleFtpClient("ftp.example.com", 21)) {
    ftp.login("deploy", System.getenv("FTP_PASSWORD"));
    ftp.upload("C:/build/app.zip", "/releases/app-1.5.zip");
}
```

The first argument must identify a readable local file. The second is the server-side filename and path as understood by the FTP server. Ensure the destination directory already exists and that the account has write permission; `upload` does not create remote directories.

## Paths, names, and encoding

Use `/` in remote paths unless the target server documents a different convention. On a server that supports UTF-8, call `enableUtf8()` after login and before sending non-ASCII path names:

```java
ftp.login("deploy", password);
ftp.enableUtf8();
ftp.upload("C:/exports/季度报告.zip", "/incoming/季度报告.zip");
```

If the server rejects `OPTS UTF8 ON`, `enableUtf8()` throws `IOException`; do not assume a different encoding is safe without confirming the server's configuration.

## Completion and failure handling

An FTP transfer has a data connection and a final control-channel reply. `upload` handles both: it closes the data stream and calls `completePendingCommand()` internally. A return from `upload` therefore means the client received a successful final reply, not merely that it finished reading the local file.

`IOException` can indicate an unreadable local file, failed data connection, permission problem, lost connection, or negative FTP reply. Treat it as an incomplete upload. The server may retain a partial remote file, so use a remote temporary name and rename it only after upload succeeds when consumers must never see partial content:

```java
String finalName = "/releases/app.zip";
String temporaryName = finalName + ".part";

try (SimpleFtpClient ftp = new SimpleFtpClient(host, 21)) {
    ftp.login(user, password);
    ftp.upload(localFile, temporaryName);
    ftp.rename(temporaryName, finalName);
}
```

`rename` needs server-side permission and may not be atomic on every FTP server. Clean up failed temporary uploads according to the server's retention policy.

## Progress and timeouts

The built-in `ProgressListener` logs byte counts through SLF4J while copying data. It is not an application callback API. Configure your SLF4J backend if transfer progress should be retained or displayed.

Set appropriate timeouts before long-running work. `setConnectTimeout` controls new socket connections; `setReadTimeout` controls socket reads. A timeout must account for the expected file size and network speed:

```java
ftp.setConnectTimeout(10_000);
ftp.setReadTimeout(120_000);
```

## Low-level upload streams

Use `put(remotePath)` only when custom streaming is required. The caller owns the returned stream and must close it, then call `completePendingCommand()`; otherwise a successful byte write can be mistaken for a completed upload.

```java
ftp.binary();
try (OutputStream remote = ftp.put("/incoming/data.bin")) {
    // Write application data to remote.
}
ftp.completePendingCommand();
```

Return to the [aj-ftp overview](/aj-ftp/) or continue to [download files](/aj-ftp/download/).
