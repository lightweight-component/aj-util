---
title: Annotation Proxy, Streams and TLS
layout: layouts/aj-util.njk
alternate: /http_request/ProxySecurity-cn/
---

# Annotation proxy, streams and TLS

The annotation proxy is a small convenience layer over the direct HTTP API. It is useful for simple JSON/form endpoints, but it does not expose the full request lifecycle, raw status handling, or every annotation declared in the package. Prefer `Request` for critical integrations.

## A supported proxy interface

```java
import com.ajaxjs.util.httpremote.call.CallHandler;
import com.ajaxjs.util.httpremote.call.annotation.GET;
import com.ajaxjs.util.httpremote.call.annotation.POST;
import com.ajaxjs.util.httpremote.call.annotation.Url;
import java.util.Map;

@Url("https://example.com/api")
public interface ExampleApi {
    @GET("/health")
    String health();

    @POST("/users")
    Map<String, Object> createUser(Map<String, Object> body);
}

ExampleApi api = CallHandler.create(ExampleApi.class);
String health = api.health();
Map<String, Object> created = api.createUser(user);
```

`CallHandler` currently dispatches `@GET`, `@POST`, `@PUT`, and `@DELETE`. Return types are limited to `String`, raw `Map`, or a bean class. For POST/PUT it selects the **first `Map` argument** as the request body. POST defaults to form data; PUT defaults to JSON. A method-level connection initializer runs after the class-level `@Url.initConnection` initializer. Initializer instances are cached, so they need a public no-argument constructor and must be safe to reuse.

## Path variables and configuration limits

```java
@GET("/users/{id}")
Map<String, Object> find(String id);
```

Path replacement uses the compiled Java parameter name and `toString()` directly. Compile the interface with `-parameters`; otherwise the runtime parameter name can be `arg0`, which will not match `{id}`. Values are **not URL-encoded**, and joining is only simple string concatenation. Encode and validate path data yourself before invoking the proxy.

The package contains more annotations/interfaces than the dispatcher currently implements. Do not infer support from their presence:

| Present API | Current behavior |
| --- | --- |
| `@HEAD`, `@Header`, `@RawBody`, `@FormData` | Not dispatched by `CallHandler`. |
| `@Url.config` / `HttpApiConfig` | Not consumed by the current dispatcher. |
| `PayloadType.FILE_UPLOAD` | Reaches the unsupported regular multipart Map path; use `MultipartPost`. |
| `create2()` / `BaseCall.init()` | Not a reliable initialization lifecycle; default-method dispatch is absent. |
| Collection/generic returns and Object methods | No dedicated proxy behavior. |

Proxy calls do not return a status-aware raw `Response` or an explicit connection handle. Use the [direct lifecycle](/aj-http/http_request/Base/) when status, retry policy, request headers, or resource cleanup are part of correctness.

## Streaming and gzip

For binary responses, install a stream consumer before connecting. Read the stream synchronously within the callback: `Request` closes it when the callback returns.

```java
Request request = new Request(HttpMethod.GET, downloadUrl);
request.setInputStreamConsumer(in -> {
    try (OutputStream out = Files.newOutputStream(Paths.get("download.bin"))) {
        byte[] buffer = new byte[8192];
        for (int size; (size = in.read(buffer)) != -1; )
            out.write(buffer, 0, size);
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
});

HttpURLConnection connection = request.init();
try {
    Response response = request.connect();
    if (!response.isOk())
        throw new IllegalStateException("Download failed: " + response.getHttpCode(), response.getEx());
} finally {
    connection.disconnect();
}
```

`Head.gzip(connection, in)` wraps only one, case-insensitive `gzip` content-encoding token; absent or chained encodings return the original stream. Gzip is not automatic. An error while creating `GZIPInputStream` becomes `UncheckedIOException`; later reads may still throw `IOException` for corrupt/truncated data.

## TLS and logging boundaries

Keep the JDK's normal certificate and hostname validation in production. `SkipSSL.init()` changes JVM-wide defaults to trust every server certificate and hostname. `SkipSSL.setSSL_Ignore(...)` disables both checks for one HTTPS connection. These methods are suitable only for controlled local diagnostics, never as a production certificate workaround. `getSocketFactory(...)` also trusts all server certificates even if client key managers are supplied; `loadCert` is package-private, not a public client-certificate API.

`Request.printLog` writes the URL, UTF-8 request body, HTTP status, and up to 460 response characters at info level, and stores formatted text in MDC. Response truncation is not redaction. Avoid logging credentials, bearer tokens, cookies, personal information, signed URLs, or private response fields.

## Implementation notes

This module intentionally stays on Java 8's `HttpURLConnection`. The convenience proxy favors a compact API over a full declarative-client feature set; direct request objects remain the escape hatch for streaming, status-aware decisions, connection configuration, and safer integration policy.

See [uploads and downloads](/aj-http/http_request/Transfer/) for file transfer, or [method helpers](/aj-http/http_request/Get/) for normal API calls.

