---
title: Download files
layout: layouts/aj-util.njk
alternate: /download-cn/
---

# Download files

`SimpleFtpClient.getFile(remotePath, localPath)` retrieves one remote file with the FTP `RETR` command. It transfers in binary mode and protects an existing local target from a failed transfer.

## Basic download

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;

try (SimpleFtpClient ftp = new SimpleFtpClient("ftp.example.com", 21)) {
    ftp.login("reader", System.getenv("FTP_PASSWORD"));
    ftp.getFile("/releases/client-1.5.zip", "C:/downloads/client-1.5.zip");
}
```

The remote path is interpreted by the FTP server. The local target must have an existing parent directory because aj-ftp creates its temporary file beside the target. Create the directory before downloading:

```java
Path downloads = Paths.get("C:/downloads");
Files.createDirectories(downloads);
ftp.getFile("/releases/client.zip", downloads.resolve("client.zip").toString());
```

## Safe local publication

aj-ftp does not write directly to the final target. It first asks the server for the file size, creates a temporary `.part` file in the target's parent directory, and streams the remote content there. Once the data stream is closed and the final FTP reply is successful, it moves the temporary file to the requested target.

This means an interrupted or rejected transfer does not replace a pre-existing target file. The temporary file is removed on failure. The final move prefers an atomic replacement but falls back to a regular replacement when the file system does not support atomic moves.

The target itself is still replaced after a successful transfer. Choose a versioned target name, or download to a staging directory and validate the file before making it available to other processes.

## Missing files and unknown sizes

`getFile` throws `IOException` if the server rejects retrieval, the connection fails, the local temporary file cannot be created, or the completed file cannot be published. Handle the exception at the job boundary and preserve the previous target until a retry succeeds.

The client queries `SIZE` to report transfer progress. Some FTP servers do not provide a usable size; the transfer can still proceed, but the logged total is unknown. Do not use progress output as an integrity check. FTP itself does not verify checksums; obtain a trusted digest separately when file integrity matters.

## UTF-8 names and timeouts

When a server supports UTF-8 path names, enable it after login before issuing a request with non-ASCII text:

```java
ftp.login(user, password);
ftp.enableUtf8();
ftp.getFile("/发布/客户端.zip", "C:/downloads/客户端.zip");
```

Configure connection and read timeouts before the transfer. A read timeout that is too short can interrupt large files on slow links; an unlimited timeout can leave a job waiting indefinitely after a network failure.

```java
ftp.setConnectTimeout(10_000);
ftp.setReadTimeout(120_000);
```

## Low-level retrieval streams

Use `get(remotePath)` for custom consumption such as parsing a remote text file without persisting it. The returned stream must be fully consumed or closed, followed by `completePendingCommand()`.

```java
ftp.binary();
try (InputStream remote = ftp.get("/data/items.csv")) {
    // Consume remote data here.
}
ftp.completePendingCommand();
```

The high-level `getFile` method already performs these steps and is the preferred choice for file downloads.

Return to the [aj-ftp overview](/aj-ftp/) or continue to [upload files](/aj-ftp/upload/).
