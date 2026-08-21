---
name: aj-ftp
description: Use and maintain the aj-ftp lightweight Java 8 FTP client. Trigger for implementation, debugging, review, tests, documentation, or usage involving com.ajaxjs.net.ftp.SimpleFtpClient or the low-level com.ajaxjs.net.ftp.sun FTP and Telnet APIs, including uploads, downloads, passive data connections, progress reporting, UTF-8 paths, transfer completion, and embedded FTP tests.
---

# AJ FTP

## Work from the source

Work from the repository containing this skill rather than remembered or published APIs. Read the relevant
implementation and tests before changing behavior because this project maintains its own fork of older JDK FTP code.

Target Java 8. Select a JDK 8 installation explicitly when the machine's default JDK is newer:

```bash
JAVA_HOME=/path/to/jdk8 mvn clean test
```

Preserve unrelated worktree changes.

## Choose the appropriate API

Use `SimpleFtpClient` for ordinary upload and download operations. Use the low-level `FtpClient` only when direct FTP
commands, data streams, or protocol state are required.

Read [references/api.md](references/api.md) when implementing or reviewing client code.

Prefer try-with-resources for the client:

```java
try (SimpleFtpClient ftp = new SimpleFtpClient(host, port)) {
    ftp.login(username, password);
    ftp.upload(localFile, remotePath);
    ftp.getFile(remotePath, localFile);
}
```

Call `enableUtf8()` after connecting and before using non-ASCII remote paths, and only when the server supports
`OPTS UTF8 ON`.

## Preserve protocol invariants

- Obtain the remote size before issuing `RETR` when download progress needs the total size.
- Close the data stream, then call `completePendingCommand()`, and validate the final transfer reply.
- Reject both carriage return and line feed in command arguments.
- Represent remote file sizes as `long` and obtain local sizes with `Files.size`.
- Propagate I/O failures instead of reporting a successful transfer.
- Keep copy helpers responsible for copying and flushing, not for closing caller-owned streams.
- Download to a sibling temporary file and publish it only after a successful final reply.
- For passive mode, connect to the control connection's peer address rather than trusting an arbitrary PASV reply
  address.
- Treat clients as not thread-safe unless the implementation explicitly adds synchronization.

## Test changes

- Use Apache FtpServer for end-to-end upload, download, UTF-8, and transfer-completion tests.
- Use scripted or fake connections for reply sequences and failure paths that an embedded server cannot reproduce
  reliably.
- Do not make tests depend on a public FTP service or platform-specific directories.
- Name test classes with a `Test` prefix and keep test classes and methods package-private.
- Add a normal-path test for each changed method; add exception tests only when they verify meaningful behavior.

After changes, run `mvn clean package` with Java 8. For JavaDoc work, run the Maven JavaDoc goal with the same JDK so
doclint matches the supported runtime.
