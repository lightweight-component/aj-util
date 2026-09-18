---
title: AJ HTTP 简介
layout: layouts/aj-util-cn.njk
---

# AJ HTTP

`aj-http` 是从 AJ Util 拆分出的 HTTP 客户端。Java 包名仍为 `com.ajaxjs.util.httpremote`；仅依赖 `ajaxjs-util` 不再提供这些 API，需要单独添加依赖。它基于 `HttpURLConnection`，提供同步请求、JSON/表单正文、multipart 上传、流式下载和功能有限的注解代理。

## 安装

当前 `aj-http/pom.xml` 的坐标为 **`com.ajaxjs:aj-net:2.1`**，与目录/工程名 `aj-http` 不同。这是源码 POM 的坐标，不代表最新已发布版本。模块依赖 `com.ajaxjs:ajaxjs-util:1.3.8`，使用 Java 8 API。

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-net</artifactId>
    <version>2.1</version>
</dependency>
```

使用 JSON 转换时，应用须在运行时提供默认引擎所需的 `jackson-databind` 和 `jackson-datatype-jsr310`（AJ Util POM 中版本均为 `2.22.1`，作用域为 `provided`）。纯文本请求不需要 JSON 转换。

## 第一个请求

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

每次操作使用独立的可变请求对象。成功表示 HTTP **200–299**，不表示业务成功。初始化、写正文或运行时错误可能直接抛出；`connect()` 内捕获的受检 I/O 异常记录在 `Response` 中。请求日志包含 URL、正文和响应文本，处理凭据或个人信息前应审查日志策略。保持正常的 TLS 校验。

## 使用指南

- [生命周期、正文与错误](/aj-http/http_request/Base-cn/)
- [GET、POST、PUT、DELETE 和 HEAD](/aj-http/http_request/Get-cn/)
- [上传、下载、代理与 TLS 限制](/aj-http/http_request/advanced-usage-cn/)

源码：`aj-http/src/main/java/com/ajaxjs/util/httpremote/`；测试：`aj-http/src/test/java/com/ajaxjs/util/httpremote/`。`TestMultipartPost`、`TestMultipartWriter` 和 `TestHttpIoHelpers` 使用本地服务或内存测试；其他部分测试访问公网，不能视为离线验证。

[源码仓库](https://github.com/lightweight-component/aj-util/tree/main/aj-http)
