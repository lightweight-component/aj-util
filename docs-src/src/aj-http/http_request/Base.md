---
title: Request Lifecycle and Responses
layout: layouts/aj-util.njk
---

# Request lifecycle

Install [AJ HTTP](/aj-http/), not just AJ Util. `Request` is in `com.ajaxjs.util.httpremote`; `Response`, `HttpMethod`, `HttpConstant` and `PayloadType` are in `com.ajaxjs.util.httpremote.model`.

1. Construct `new Request(HttpMethod.GET, url)` (or a method class's `(HttpMethod, String)` constructor). Construction does **not** send.
2. Set content type, body and timeouts before `init()`. Defaults are 10,000 ms connect / 15,000 ms read.
3. `init(Consumer<HttpURLConnection>)` creates/configures the connection. The callback should only configure headers, timeouts, redirects, etc., not start I/O. Only HTTP/HTTPS URLs are accepted.
4. For a body, call `initData()` after `init()` and before `connect()`.
5. `connect()` reads status and response. Consume the response, then disconnect the connection in `finally` when managing this lifecycle yourself.

`Get(String, Consumer)`, `Delete(String, Consumer)` and the data-taking `Post`/`Put` constructors send immediately. In contrast, `Head(String)` only configures an object: call `init()` and `connect()`. Do not send a second time after an auto-sending constructor. Requests are mutable; do not share across concurrent operations.

## JSON POST with explicit lifecycle

```java
import com.ajaxjs.util.httpremote.Request;
import com.ajaxjs.util.httpremote.model.HttpConstant;
import com.ajaxjs.util.httpremote.model.HttpMethod;
import com.ajaxjs.util.httpremote.model.Response;
import java.net.HttpURLConnection;
import java.util.Collections;

Request request = new Request(HttpMethod.POST, "https://example.com/api");
request.setContentType(HttpConstant.CONTENT_TYPE_JSON);
request.setData(Collections.<String, Object>singletonMap("name", "Ada"));
HttpURLConnection connection = request.init();
try {
    request.initData();
    Response response = request.connect();
    if (!response.isOk()) {
        throw new IllegalStateException("HTTP request failed: " + response.getHttpCode(), response.getEx());
    }
    // Parse only if this endpoint promises a JSON object.
    java.util.Map<String, Object> result = response.responseAsJson();
} finally {
    connection.disconnect();
}
```

### Body conversion

- `setData(byte[])` sends exact bytes without conversion. Set an appropriate content type separately; use bytes for arbitrary text, XML or binary data.
- `setData(String)` accepts JSON-looking text (a leading `{` or `[` after trimming, not full validation). For JSON it sends UTF-8 unchanged; for form data it parses an object then joins fields.
- `setDataStr(String)` accepts an already formed `a=1&b=2` string. With form content type it sends UTF-8 unchanged; with JSON it converts via `MapTool.toMap`.
- `setData(Map<String,Object>)` / `setData(Object)` serialize JSON or form fields. Form conversion encodes **values**, not keys. Use simple field names; this is not a general query builder.
- Set content type first for non-byte overloads. Multipart Map conversion here throws `UnsupportedOperationException`; use `MultipartPost` instead.
- Auto-sending `BasePost` constructors dispatch String bodies only when they look like JSON without surrounding whitespace or contain `=`. Other strings can silently produce no body. Prefer an explicit Map or UTF-8 byte array.

## Responses and failures

`Response.isOk()` means 200–299. Other statuses use the error stream, if any; a 3xx with redirects disabled may have no text. Normal `HttpURLConnection` redirect behavior applies unless configured otherwise. `Response.isOk(Map)` is a different, AJ-Spring-specific helper checking whether `status.toString()` equals `"1"`.

- `connect()` catches checked `IOException`, sets `isOk=false` and `ex`; HTTP errors usually have no exception. `httpCode` can be null if no status was received.
- Initialization errors, write failures (`UncheckedIOException`) and callback/runtime/JSON parsing errors can propagate. The local `DataReader` also wraps read failures as `UncheckedIOException`, outside `connect()`'s checked-I/O catch. Do not assume every failure becomes `Response.ex`.
- `responseAsJson()` returns a Map or null for empty text, without checking status or content type. `responseAsJsonList()` checks content type using a case-sensitive header-map lookup. `responseAsBean(Class)` returns null for non-success; `responseAsXML()` parses nonempty text regardless of status. Malformed data can throw.
- Default text reading uses `DataReader` (UTF-8), normalizes line endings and appends a final platform separator. Current `Request` does **not** trim the stored text. It is not byte-preserving and does not automatically decompress gzip.
- `setInputStreamConsumer(...)` bypasses text conversion; read synchronously inside the callback. The input is closed after the callback, including error streams. `Response.in` is not populated by `Request`. `initData()` flushes/closes its output; `Request.connect()` does not disconnect.

Source: `Request.java`, `BasePost.java`, `model/Response.java` and AJ Util `io/DataReader.java`. Some `TestMultipartPost` assertions expect text without a trailing newline; that disagrees with the local reader. The HTTP POM resolves a versioned AJ Util dependency, so check the resolved artifact before assuming identical text behavior.

Continue with [method helpers](/aj-http/http_request/Get/) and [streaming/security](/aj-http/http_request/advanced-usage/).
