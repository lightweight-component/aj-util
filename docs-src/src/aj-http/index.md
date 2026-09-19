---
title: AJ HTTP
layout: layouts/aj-util.njk
---

# AJ HTTP

`aj-http` owns the HTTP client extracted from AJ Util. The Java namespace remains `com.ajaxjs.util.httpremote`; add the separate dependency rather than relying on `ajaxjs-util` alone. It wraps `HttpURLConnection` with synchronous requests, JSON/form bodies, multipart uploads, streamed downloads and a limited annotation proxy.

## Install

The current `aj-http/pom.xml` declares **`com.ajaxjs:aj-net:2.1`**, despite the directory/project name `aj-http`. These are source POM coordinates, not a claim about the latest published release. The module depends on `com.ajaxjs:ajaxjs-util:1.3.8` and uses Java 8 APIs.

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-net</artifactId>
    <version>2.1</version>
</dependency>
```

For JSON conversion, supply the default engine's Jackson dependencies at runtime: `jackson-databind` and `jackson-datatype-jsr310` (version `2.22.1` in the AJ Util POM, both `provided`). Plain text requests do not require JSON conversion.

## First request

```java
import com.ajaxjs.util.httpremote.Request;
import com.ajaxjs.util.httpremote.model.HttpMethod;
import com.ajaxjs.util.httpremote.model.Response;
import java.net.HttpURLConnection;

Request request = new Request(HttpMethod.GET, "https://example.com/");
request.setConnectTimeout(5_000);
request.setReadTimeout(10_000);
HttpURLConnection connection = request.init(c -> c.setRequestProperty("Accept", "text/plain"));
try {
    Response response = request.connect();
    if (!response.isOk()) {
        throw new IllegalStateException("HTTP request failed: " + response.getHttpCode(), response.getEx());
    }
    String text = response.getResponseText();
} finally {
    connection.disconnect();
}
```

Use one mutable request object per operation. Success means HTTP **200–299**, not business success. Initialization/body-writing/runtime failures can throw; checked I/O failures inside `connect()` are recorded on `Response`. Request logging includes URL, body and response text: review logging before handling credentials or personal data. Keep normal TLS verification enabled.

## Guide

- [Lifecycle, bodies and errors](/aj-http/http_request/Base/)
- [GET, POST, PUT, DELETE and HEAD](/aj-http/http_request/Get/)
- [Uploads and downloads](/aj-http/http_request/Transfer/)
- [Annotation proxy, streams and TLS limits](/aj-http/http_request/ProxySecurity/)

Source: `aj-http/src/main/java/com/ajaxjs/util/httpremote/`; tests: `aj-http/src/test/java/com/ajaxjs/util/httpremote/`. `TestMultipartPost`, `TestMultipartWriter` and `TestHttpIoHelpers` exercise local-server or in-memory behavior; several other tests contact public services and are not offline checks.

[Source repository](https://github.com/lightweight-component/aj-util/tree/main/aj-http)
