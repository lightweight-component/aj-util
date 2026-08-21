# aj-ftp API and Maintenance Reference

## Public entry point

`com.ajaxjs.net.ftp.SimpleFtpClient` is the high-level client. It inherits the low-level FTP implementation and can be
used with try-with-resources through the closeable network-client hierarchy.

Common operations:

- `new SimpleFtpClient(host, port)` opens a control connection.
- `login(username, password)` authenticates with explicit credentials.
- `anonymousLogin()` performs anonymous authentication.
- `enableUtf8()` negotiates UTF-8 command and path handling.
- `upload(localFile, remotePath)` uploads in binary mode.
- `getFile(remotePath, localFile)` downloads in binary mode.
- `close()` releases control and data connection resources.

Downloads use a sibling temporary file. The implementation closes the data stream, verifies the final FTP reply, and
then moves the completed file into place so an interrupted download does not look complete.

`ProgressListener.copy(...)` copies and flushes data, reports progress, and propagates I/O failures. It does not close
streams owned by its caller.

## Low-level client

`com.ajaxjs.net.ftp.sun.FtpClient` exposes connection, authentication, transfer mode, file-system command, data-stream,
reply-state, and pending-command operations.

When using a method that returns a data stream:

1. Consume or produce all data.
2. Close the returned stream.
3. Call `completePendingCommand()`.
4. Treat a negative final reply as a failed transfer.

The low-level API is appropriate for uncommon FTP commands or protocol-level tests. Prefer `SimpleFtpClient` for
application code.

## Control and data connections

`NetworkClient` owns the control socket, explicit control-channel encoding, timeouts, and close behavior. The default
control encoding is stable rather than derived from the host's `file.encoding`; UTF-8 is enabled through explicit server
negotiation.

Passive data connections prefer EPSV and then PASV. The PASV port is used with the control connection's peer address.
Active-mode EPRT or PORT behavior is a fallback when passive setup cannot be used.

## Testing map

The test suite includes embedded-server integration tests and focused protocol tests. Look first for these test areas
before adding a new class:

- embedded FTP upload and download behavior;
- FTP reply and pending-command state;
- high-level `SimpleFtpClient` behavior;
- network-client resource and encoding behavior;
- Telnet stream parsing and escaping.

Use JUnit 5 and Apache FtpServer already declared by the module. Create temporary files and directories through the
JUnit temporary-directory facilities so tests work on Windows, macOS, and Linux.

## Project scope

- Lightweight plain FTP client; it is not an FTPS or SFTP implementation.
- Java 8 is the compatibility target.
- Client instances are not designed for concurrent use.
- Lombok is used for routine accessors and SLF4J logger generation where configured by the source.
