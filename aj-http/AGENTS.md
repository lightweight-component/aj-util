# AJ HTTP module guidance

This file supplements the repository-level `AGENTS.md` for work in `aj-http`.

## Module identity

- Maven artifact: `com.ajaxjs:aj-net`; module directory and display name: `aj-http`.
- Production package: `com.ajaxjs.util.httpremote`.
- Target Java 8. Do not introduce APIs added after Java 8.
- Main request classes are `Request`, `Get`, `Post`, `Put`, `Delete`, `Head`, `MultipartPost`, and `HttpFileDownload`.

## Request lifecycle and resources

- For a manually managed request, configure it before `init()`, call `initData()` for a body, then call `connect()`.
- Disconnect the `HttpURLConnection` in a `finally` block when using the explicit lifecycle.
- `Request.connect()` records checked I/O failures in `Response`, but configuration errors, callback failures, JSON conversion errors, and `UncheckedIOException` can still propagate. Do not document all failures as `Response.ex`.
- `setInputStreamConsumer(...)` must consume the response synchronously; the request closes the stream after the callback returns.
- Do not share mutable `Request` instances between concurrent operations.

## Payloads, uploads, and downloads

- Set the content type before calling non-byte `Request.setData(...)` overloads.
- Use `MultipartPost` for `multipart/form-data`; regular `Request.setData(Map)` intentionally does not implement multipart conversion.
- `MultipartWriter.write(...)` produces one complete multipart body, including its closing boundary. Do not use repeated calls to append parts.
- Caller-supplied upload `InputStream`s remain caller-owned; close them with try-with-resources.
- Treat downloaded filenames, redirects, and destination paths as untrusted input. The downloader can replace existing files and has no size-limit or destination-sandbox policy.

## Security and logging

- Keep ordinary TLS certificate and hostname verification enabled in production. `SkipSSL` is for controlled diagnostics only; `SkipSSL.init()` changes JVM-wide defaults.
- `Request.printLog` can include request bodies and response text. Never add secrets, tokens, cookies, or personal data to logs or examples.
- Annotation proxy support is intentionally limited. Check `CallHandler` before documenting an annotation as supported; use direct `Request` for status-aware or security-sensitive integrations.

## Tests and documentation

- Tests are under `aj-http/src/test/java`. Prefer local/in-memory tests for new behavior; existing public-service tests are not reliable offline checks.
- Run `mvn -pl aj-http test` for module changes when practical, and report any network-dependent limitation.
- Keep `docs-src/src/aj-http` English and Chinese pages aligned. Use the direct lifecycle in examples when showing status/error handling or connection ownership.
- Build documentation from `docs-src` with `npm test` after documentation, navigation, layout, or style changes.
