---
title: Uploads and Downloads
layout: layouts/aj-util.njk
alternate: /http_request/Transfer-cn/
---

# Uploads and downloads

Use `MultipartPost` for `multipart/form-data` and `HttpFileDownload` for files received from an HTTP server. Both APIs stream data: they avoid first collecting an entire file in a Java byte array.

## Multipart upload

`MultipartPost.upload(...)` accepts a single map of text values and `File` values. The map must be non-empty. The callback is for connection configuration only; do not connect, read, or write in it.

```java
import com.ajaxjs.util.httpremote.MultipartPost;
import com.ajaxjs.util.httpremote.model.Response;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

Map<String, Object> parts = new LinkedHashMap<>();
parts.put("title", "Quarterly report");
parts.put("document", new File("reports/q1.pdf"));

Response response = MultipartPost.upload("https://example.com/upload", parts, connection -> {
    connection.setConnectTimeout(5_000);
    connection.setReadTimeout(30_000);
    connection.setRequestProperty("Authorization", "Bearer " + token);
});

if (!response.isOk())
    throw new IllegalStateException("Upload failed: " + response.getHttpCode(), response.getEx());
```

For one file, use `uploadFile`. It supports `File`, `byte[]`, and `InputStream` input:

```java
Response response = MultipartPost.uploadFile(
        "https://example.com/avatar", "avatar", "me.png", imageBytes, null);

try (InputStream content = Files.newInputStream(Paths.get("large-video.mp4"))) {
    // The supplied stream is not closed by MultipartPost; this try block owns it.
    response = MultipartPost.uploadFile(
            "https://example.com/video", "video", "large-video.mp4", content, null);
}
```

The helper creates a new boundary, applies the matching `Content-Type` after the callback, enables 8 KiB chunked streaming, closes the request output, reads/closes the response stream, and disconnects in `finally`. Redirects are deliberately disabled: automatically replaying a potentially large upload to an unreviewed destination is unsafe. A non-2xx status returns a failed `Response`; validation, callback, body-writing, and runtime errors can still throw.

## MultipartWriter when an existing connection owns the stream

`MultipartWriter` only serializes a complete multipart body. It does not create an HTTP connection, flush/close the output stream, or close a caller-supplied input stream. Set the request header from `getContentType()`.

```java
String boundary = "upload-2026";
MultipartWriter writer = new MultipartWriter(connection.getOutputStream(), boundary);
connection.setRequestProperty("Content-Type", writer.getContentType());

Map<String, Object> parts = new LinkedHashMap<>();
parts.put("comment", "hello");
parts.put("file", new File("notes.txt"));
writer.write(parts);             // exactly one call writes the terminating boundary
connection.getOutputStream().flush();
```

Each `write(...)` call ends the multipart body with the closing boundary. Put every field/file into one map and call it once; repeated calls cannot append another valid part. Boundaries must match `[A-Za-z0-9_-]{1,70}`. Text and headers use UTF-8; `null` text values become empty, CR/LF in names are rejected, and quotes/backslashes are escaped. Use dedicated byte-array or stream overloads when a file name is required; the map-key encoding used internally is not a public protocol.

## Downloading a file

```java
import com.ajaxjs.util.httpremote.HttpFileDownload;

new HttpFileDownload("https://example.com/files/report.pdf", "downloads/report.pdf")
        .download();

new HttpFileDownload(new String[] {
        "https://example.com/a.pdf",
        "https://example.com/b.pdf"
}, "downloads").downloadAllAsync().join();
```

| API | Result |
| --- | --- |
| `download()` | Downloads one URL on the calling thread. |
| `downloadAsync()` | Returns a future for one download. |
| `downloadAll()` | Downloads the supplied URLs in order. |
| `downloadAllAsync()` | Returns `CompletableFuture<Void>` for all queued downloads. |

Async work uses a shared four-thread daemon pool with an unbounded queue. Always `join`, `get`, or otherwise handle the returned future; `join()` reports a failed task as `CompletionException`. There is no public executor, cancellation policy, or returned filename list. Do not mutate the URL array passed to the batch constructor during use.

## Target names, replacement, and trust boundaries

An explicit file target wins. For a directory target, the name priority is `Content-Disposition` (`filename*` first), URL path, then a generated fallback. Server/path names are reduced to a basename and control characters are removed. A String ending in `/` or `\\`, or an existing directory, is a directory target. A non-existing `Path` is treated as a file; a batch target is always a directory.

Only HTTP/HTTPS URLs and 2xx results are accepted. The downloader uses fixed 10-second connect and 30-second read timeouts, follows redirects, streams to a temporary sibling file, then replaces the destination. It closes streams, disconnects, and attempts temporary cleanup. This reduces partial files but is not an atomic-move guarantee.

There is no download-size cap, destination sandbox, or collision policy. Existing files can be overwritten and concurrent downloads resolving to the same name can race. Use a trusted destination directory and validate untrusted URLs/redirects to avoid SSRF or unwanted internal-network access.

## Implementation notes

Regular `Request.setData(Map)` intentionally does not implement multipart conversion; it throws for multipart content type. Keeping body encoding in `MultipartWriter` and transport lifecycle in `MultipartPost` makes stream ownership explicit and prevents hidden in-memory buffering.

Continue with [annotation proxy, streams and TLS](/aj-http/http_request/ProxySecurity/), or return to [request lifecycle](/aj-http/http_request/Base/).

