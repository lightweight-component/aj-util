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
