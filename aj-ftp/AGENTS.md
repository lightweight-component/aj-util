# aj-ftp module guidance

## Scope and API choice

- aj-ftp is a lightweight, plain FTP client for Java 8. It does not implement FTPS or SFTP.
- Use `SimpleFtpClient` for application-level upload and download operations. Use the `sun.ftp.FtpClient` API only for direct FTP commands, custom streams, or protocol-level work.
- Client instances are not thread-safe; do not share an instance across concurrent operations.
- Use try-with-resources so the control connection is always closed.

## Transfer invariants

- Use binary mode for file transfers.
- For low-level `get(...)` and `put(...)`, finish using the returned data stream, close it, then call `completePendingCommand()` and treat a negative reply as failure.
- Propagate `IOException`; do not report a transfer as successful merely because local bytes were read or written.
- `ProgressListener` copies and flushes streams, but must not close streams owned by the caller.
- Downloads must use a sibling temporary file and publish it only after the final FTP reply succeeds. Preserve an existing target file on failure.
- Use `long` for file sizes and `Files.size(...)` for local files.

## Protocol and security

- Call `enableUtf8()` only after connection and only when the server accepts `OPTS UTF8 ON`; do so before non-ASCII remote paths are sent.
- Reject carriage-return and line-feed characters in FTP command arguments.
- Passive-mode data connections must use the control connection peer address, not an arbitrary address supplied by a PASV response.
- Do not add examples, tests, or production behavior that rely on public FTP servers or embed credentials.

## Tests and documentation

- Use the embedded Apache FtpServer tests for end-to-end upload/download behavior; use focused protocol tests for reply and failure paths.
- Keep tests portable: use temporary directories and avoid platform-specific paths.
- When public behavior changes, update both `docs-src/src/aj-ftp/index.md` and `cn.md`, plus the relevant upload/download pages in both languages.
- Run the module's relevant Maven tests with Java 8 when changing Java code, and run `npm test` from `docs-src` when changing its documentation or navigation.
