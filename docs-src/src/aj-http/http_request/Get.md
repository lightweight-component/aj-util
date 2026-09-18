---
title: HTTP Method Helpers
layout: layouts/aj-util.njk
---

# GET, POST, PUT, DELETE and HEAD

All classes below belong to `com.ajaxjs.util.httpremote`. See [lifecycle and errors](/aj-http/http_request/Base/) before using shortcuts: text/Map helpers do not enforce HTTP success, and do not expose a connection for explicit cleanup. Use `Request` when status, streaming or deterministic disconnection matters.

## GET shortcuts

```java
import com.ajaxjs.util.httpremote.Get;
import java.util.Map;

String text = Get.text("https://example.com/");
Map<String, Object> json = Get.api("https://example.com/api", connection -> {
    connection.setConnectTimeout(5_000);
    connection.setReadTimeout(10_000);
    connection.setRequestProperty("Accept", "application/json");
});
Map<String, String> xml = Get.apiXml("https://example.com/data.xml",
        connection -> connection.setRequestProperty("Accept", "application/xml"));
```

`Get.api(url, token)` adds `Authorization: Bearer <token>`. Bean variants are `Get.api(url, MyBean.class)` and overloads with a token or callback. Supply your actual bean class; non-success returns null. `new Get(url, callback)` sends immediately; there is no one-argument `Get(String)` constructor. For manual setup use `new Get(HttpMethod.GET, url)`.

## POST and PUT

```java
import com.ajaxjs.util.httpremote.Post;
import com.ajaxjs.util.httpremote.Put;
import com.ajaxjs.util.httpremote.model.Response;
import java.util.Collections;
import java.util.Map;

Map<String, Object> body = Collections.<String, Object>singletonMap("name", "Ada");
Map<String, Object> posted = Post.api("https://example.com/api", body);
Map<String, Object> form = Post.form("https://example.com/form", body, null);
Response updated = Put.api("https://example.com/api/1", body);
```

`Post.api` returns a parsed Map; `Put.api` returns `Response`. Both have callback overloads. `new Post(url, data, contentType, callback)` and the equivalent `Put` constructor send immediately; a null content type defaults to JSON. Omit the callback with the three-argument constructor. Use `HttpConstant.CONTENT_TYPE_FORM` for form bodies. For uploads use [MultipartPost](/aj-http/http_request/advanced-usage/), not the regular Map body converter.

## DELETE

`Delete.api(url, token)` and `Delete.api(url, callback)` return a Map. Bean overloads take `(url, Class<T>, token/callback)`. There is no `Delete.api(url)` overload. For status/error handling use a manual `Request(HttpMethod.DELETE, url)` or `Delete(HttpMethod.DELETE, url)` lifecycle.

## HEAD and connection helpers

```java
import com.ajaxjs.util.httpremote.Head;
import com.ajaxjs.util.httpremote.model.Response;
import java.net.HttpURLConnection;

Head head = new Head("https://example.com/file.pdf");
HttpURLConnection connection = head.init();
try {
    Response response = head.connect();
    if (response.getEx() != null) {
        throw new IllegalStateException("HEAD failed", response.getEx());
    }
    boolean missing = head.is404();
    long size = head.getFileSize();
    String location = head.get302redirect();
} finally {
    connection.disconnect();
}
```

`Head` disables automatic redirects unless the init callback overrides it. `get302redirect()` reads `Location` without checking for status 302. `getFileSize()` returns `long` but calls the int-based `getContentLength()`; unknown/too-large lengths can be `-1`. `is404()` can throw on I/O failure. These are **instance** methods, not static URL utilities.

`Head.map2header(map)`, `Head.json` and `Head.setBearerToken(token)` supply connection callbacks. Header values in `map2header` must be non-null. `HttpConstant.SET_USER_AGENT_DEFAULT` is another callback; set cookies or Referer with normal `setRequestProperty` rather than a nonexistent cookie utility.

Source/tests: `Get`, `Post`, `Put`, `Delete`, `Head` and their `Test*` classes. Public-service tests illustrate calls, not guaranteed service availability or every edge case.
