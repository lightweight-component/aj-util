---
title: UrlCodec
description: UrlCodec
layout: layouts/aj-util.njk
lang: en
alternate: /common/UrlCodec-cn/
---

# UrlCodec

`com.ajaxjs.util.UrlCodec` operates on **individual raw parameter values**, not complete URLs. Constructors accept `String` or `String, Charset` (UTF-8 by default); arguments must be non-null.

| Operation | Space | Literal plus |
| --- | --- | --- |
| `encodeForm()` | `+` | `%2B` |
| `encodeQueryValue()` | `%20` | `%2B` |
| `decodeForm()` | decodes `+` to space | use `%2B` |
| `decodeQueryValue()` | decodes `%20` | preserves `+` |

Query encoding preserves `~` and escapes `*`. Existing percent escapes are encoded again. Malformed percent escapes fail with `IllegalArgumentException`.

```java
import com.ajaxjs.util.UrlCodec;
import com.ajaxjs.util.MapTool;
import java.util.Map;

String form = new UrlCodec("a b+c").encodeForm(); // a+b%2Bc
String query = new UrlCodec("a b+c").encodeQueryValue(); // a%20b%2Bc
String value = new UrlCodec("a%20b+c").decodeQueryValue(); // a b+c
Map<String, String> params = MapTool.toMap("name=a+b&token=x%3D%3D");
String url = UrlCodec.concatUrl("https://example.com/", "/search") + "?q=" + query;
```

## Other operations

`concatUrl(String, String)` only joins at the slash boundary. It is not URI resolution, validation or normalization and does not specially handle query strings/fragments.

`simpleGET(String)` opens a URL stream without explicit timeouts or headers, reads UTF-8 and normalizes line endings (including a trailing separator). It closes the stream. URL/open failures return null; read failures may throw `UncheckedIOException`. Use the [AJ HTTP API](/aj-http/http_request/Base/) for controlled requests. Query parsing is `MapTool.toMap(String)`.

## Encode values, then assemble the URL

Encode each key and value separately. Do not pass `?`, `&`, `=` or an already-built URL to `encodeQueryValue()`; those
characters are data in this API and will be escaped. This prevents ambiguous parameters and avoids accidental
double-encoding.

```java
String q = new UrlCodec("Java 8 + streams").encodeQueryValue();
String page = new UrlCodec("2").encodeQueryValue();
String url = UrlCodec.concatUrl("https://example.com/api", "search")
        + "?q=" + q + "&page=" + page;
// https://example.com/api/search?q=Java%208%20%2B%20streams&page=2
```

The class intentionally does not parse or build a multi-value query map. If a complete URL must be validated,
resolved against a base URI, or composed from structured components, use `java.net.URI` (or a dedicated HTTP client)
at that boundary.

## Form semantics versus query-value semantics

HTML form bodies using `application/x-www-form-urlencoded` use `+` for spaces; `URLEncoder` and `URLDecoder` follow
that convention. RFC 3986-style query values conventionally use `%20` for spaces, and a literal plus is preserved.
This distinction is especially important for signed URLs and tokens.

```java
String form = new UrlCodec("a b+c").encodeForm();             // a+b%2Bc
String rawQuery = new UrlCodec("a b+c").encodeQueryValue();  // a%20b%2Bc

new UrlCodec("a+b%2Bc").decodeForm();          // "a b+c"
new UrlCodec("a%20b+c").decodeQueryValue();    // "a b+c"
```

Percent escapes represent bytes in the codec charset (UTF-8 unless explicitly changed). A literal `%` is not treated
as an escape during encoding: `new UrlCodec("%2F").encodeQueryValue()` produces `%252F`. Decode only text you know is
encoded once; malformed `%` sequences fail fast with `IllegalArgumentException`.

## Path joining and simple reads

`concatUrl` removes at most one trailing slash from the first part and uses the leading slash of the second part when
present. It is predictable for normal path fragments, but deliberately leaves repeated internal slashes, queries, and
fragments alone.

```java
UrlCodec.concatUrl("https://host/api/", "/users"); // https://host/api/users
UrlCodec.concatUrl("https://host/api", "users");  // https://host/api/users
```

`simpleGET` is suitable only for a small, trusted, best-effort read. It has no timeout, headers, status handling,
redirect policy, authentication, or response-charset detection. A failure while constructing or opening the URL
returns `null`; failure during content reading can still throw `UncheckedIOException`. Use the AJ HTTP API for normal
network calls.

## Implementation notes

Form methods delegate to the JDK's `URLEncoder`/`URLDecoder`. Query-value encoding begins with the form result, then
converts `+` to `%20`, `*` to `%2A`, and `%7E` back to `~`. Query-value decoding protects literal plus signs by first
rewriting them as `%2B` before invoking `URLDecoder`. This small adaptation preserves the JDK's robust percent-byte
handling while providing RFC 3986-style value semantics.
