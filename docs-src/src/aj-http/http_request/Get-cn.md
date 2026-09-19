---
title: HTTP 方法辅助类
layout: layouts/aj-util-cn.njk
alternate: /http_request/Get/
---

# GET、POST、PUT、DELETE 和 HEAD

以下类均位于 `com.ajaxjs.util.httpremote`。使用快捷方法前请阅读[生命周期与错误](/aj-http/http_request/Base-cn/)：文本/Map 方法不会强制检查 HTTP 成功，也不暴露连接供显式清理。需要状态、流式处理或确定性断开连接时使用 `Request`。

## GET 快捷方法

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

`Get.api(url, token)` 添加 `Authorization: Bearer <token>`。Bean 版本为 `Get.api(url, MyBean.class)`，也有带 token 或回调的重载。请使用实际 Bean 类；非成功状态返回 null。`new Get(url, callback)` 立即发送，不存在单参数 `Get(String)` 构造函数。手动配置使用 `new Get(HttpMethod.GET, url)`。

## POST 与 PUT

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

`Post.api` 返回解析后的 Map；`Put.api` 返回 `Response`。两者都有回调重载。`new Post(url, data, contentType, callback)` 及对应 `Put` 构造函数立即发送；内容类型为 null 时默认 JSON。三参数构造函数省略回调。表单正文使用 `HttpConstant.CONTENT_TYPE_FORM`。上传请使用 [MultipartPost](/aj-http/http_request/advanced-usage-cn/)，不是普通 Map 正文转换器。

## DELETE

`Delete.api(url, token)` 和 `Delete.api(url, callback)` 返回 Map。Bean 重载接受 `(url, Class<T>, token/callback)`。不存在 `Delete.api(url)`。需要状态和错误处理时，手动管理 `Request(HttpMethod.DELETE, url)` 或 `Delete(HttpMethod.DELETE, url)` 生命周期。

## HEAD 与连接辅助方法

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

`Head` 禁止自动重定向，除非 init 回调覆盖此设置。`get302redirect()` 直接读取 `Location`，不检查状态是否为 302。`getFileSize()` 虽返回 `long`，内部调用的是 int 版本 `getContentLength()`，未知或过大的长度可能为 `-1`。`is404()` 可能因 I/O 错误抛异常。这些都是**实例**方法，不是接受 URL 的静态工具。

`Head.map2header(map)`、`Head.json` 和 `Head.setBearerToken(token)` 返回连接配置回调。`map2header` 的值不能为 null。`HttpConstant.SET_USER_AGENT_DEFAULT` 也是回调；Cookie 和 Referer 请通过普通 `setRequestProperty` 设置，不存在专门的 Cookie 工具。

依据：`Get`、`Post`、`Put`、`Delete`、`Head` 及对应的 `Test*` 类。公网测试展示调用方式，不保证外部服务可用或覆盖全部边界。
