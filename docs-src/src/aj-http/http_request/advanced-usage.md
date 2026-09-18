---
title: Uploads, Downloads and Proxy Limits
layout: layouts/aj-util.njk
---

# Multipart uploads

```java
import com.ajaxjs.util.httpremote.MultipartPost;
import com.ajaxjs.util.httpremote.model.Response;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

Map<String, Object> parts = new LinkedHashMap<>();
parts.put("name", "Ada");
parts.put("document", new File("document.pdf"));
Response response = MultipartPost.upload("https://example.com/upload", parts,
        connection -> connection.setReadTimeout(30_000));
if (!response.isOk()) {
    throw new IllegalStateException("Upload failed: " + response.getHttpCode(), response.getEx());
}
```

`MultipartPost.uploadFile` accepts `(url, fieldName, File, callback)` or `(url, fieldName, fileName, byte[]/InputStream, callback)`. The **caller closes supplied input streams**. File streams opened internally are closed. The upload manages/closes the request output, reads/closes the response input and disconnects in `finally`. Init callbacks only configure the connection.

Each upload generates a boundary, reapplies the matching content type after the callback, uses 8192-byte chunked streaming and disables redirects to avoid replaying uploads. A non-2xx status returns a failed `Response`; validation, callback or writing errors may throw. Handle redirects explicitly with a replay and destination policy.

`MultipartWriter` only encodes: create it with an `OutputStream` and optionally a boundary, then call **one** `write(...)` for the complete body. Each call emits the closing boundary; use one Map for multiple fields/files, not repeated calls. Set the HTTP header from `getContentType()`. It neither closes nor flushes caller output and does not close caller input streams. Boundaries must match `[A-Za-z0-9_-]{1,70}`. Headers/text use UTF-8; null values become empty text. CR/LF in names are rejected; quotes/backslashes are escaped. Use dedicated overloads for raw bytes/streams, not undocumented Map-key metadata. I/O failures become `UncheckedIOException`.

## File downloads

```java
import com.ajaxjs.util.httpremote.HttpFileDownload;

new HttpFileDownload("https://example.com/file.pdf", "downloads/report.pdf").download();
new HttpFileDownload(new String[] {
        "https://example.com/a.pdf", "https://example.com/b.pdf"
}, "downloads").downloadAllAsync().join();
```

- `download()` / `downloadAsync()` handle a single URL; `downloadAll()` is sequential; `downloadAllAsync()` returns a `CompletableFuture<Void>` for all downloads. Join/handle the future to observe failures (`CompletionException` on join).
- Async work uses a shared four-thread daemon pool with an unbounded queue, not one thread per URL. There is no public executor/cancellation policy or result-filename list. The constructor retains the supplied URL array; do not mutate it during use.
- An explicit target file wins. For a directory, filename priority is `Content-Disposition` (UTF-8 `filename*` first), URL path, then generated fallback. Server/path names are sanitized to a basename with control characters removed.
- A String target ending in `/` or `\`, or an existing directory, is a directory. A nonexisting `Path` target is a file; batch targets are always directories.
- Only HTTP/HTTPS and 2xx are accepted. Connect/read timeouts are fixed at 10/30 seconds; redirects are enabled. Data streams to a temporary sibling file, then moves with replacement. Streams close, connection disconnects and temporary cleanup is attempted. This is **not** an atomic-move guarantee.
- There is no download-size limit, destination sandbox or collision policy. Existing files can be replaced; concurrent same-name downloads can overwrite each other. Use trusted destinations and validate untrusted URLs/redirects to prevent SSRF. Filename sanitization is not a complete filesystem security policy.

## Annotation proxy: implemented subset only

```java
import com.ajaxjs.util.httpremote.call.CallHandler;
import com.ajaxjs.util.httpremote.call.annotation.GET;
import com.ajaxjs.util.httpremote.call.annotation.Url;

public class ApiExample {
    @Url("https://example.com")
    public interface Api {
        @GET("/health")
        String health();
    }

    public static String health() {
        return CallHandler.create(Api.class).health();
    }
}
```

Current `CallHandler` dispatches `@GET`, `@POST`, `@PUT`, `@DELETE` and returns String, Map or a bean. POST/PUT select the **first Map argument** for the body; `@POST` defaults to FORM, `@PUT` to JSON_BODY. Class-level `@Url.initConnection` runs before the method-level initializer; initializer instances are cached and must be safe to reuse with a public no-arg constructor.

Path substitutions use Java parameter names (compile with `-parameters`) and raw `toString()` values, without URL encoding. URL joining is simple concatenation, not URI resolution; build/validate encoded values yourself.

Do **not** infer support merely from annotations/interfaces present in source:

- `@HEAD`, `@Header`, `@RawBody`, `@FormData` and `@Url.config` are not handled by this dispatcher.
- `PayloadType.FILE_UPLOAD` goes through the unsupported regular multipart Map path; use `MultipartPost` directly.
- `create2()` calls `BaseCall.init()`, but the handler has no lifecycle/default-method dispatch for it; do not use it as a working initialization API. Object methods and generic collection return types also lack dedicated handling.
- Proxy calls do not expose a raw status-aware response or explicit disconnection. Prefer the direct lifecycle for critical error/resource handling.

## Streams, TLS and logging

For binary responses install `Request.setInputStreamConsumer` before connecting and consume inside the callback; the stream closes afterwards. `Head.gzip(connection, in)` wraps only a single, case-insensitive `gzip` encoding token (surrounding whitespace allowed); absent or chained encodings return the original stream. An `IOException` while constructing `GZIPInputStream` is wrapped in `UncheckedIOException`; later reads from the returned stream can still throw checked `IOException`, including for corrupt or truncated payloads. Decompression is not automatic.

Keep normal TLS trust and hostname checks. `SkipSSL.init()` changes JVM-wide defaults to trust every certificate and hostname; `setSSL_Ignore` disables both for a connection. `getSocketFactory` trusts all servers even when client key managers are supplied, and can return null on failure. `loadCert` is package-private, not a public application API. These are not production trust-store configuration tools.

`Request.printLog` logs URL, UTF-8 request body and up to 460 characters of response at info level and stores formatted text in MDC. Truncation is not redaction. Prevent secret/personal-data leakage and never enable trust-all TLS to “fix” production certificate errors.

Evidence: `MultipartPost`, `MultipartWriter`, `HttpFileDownload`, `CallHandler`, `Head`, `SkipSSL`; local tests `TestMultipartPost`, `TestMultipartWriter`, `TestHttpIoHelpers`. The download and proxy examples in existing tests include network/environment assumptions; they are not security proofs.
