---
title: UrlCodec
description: UrlCodec
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /common/UrlCodec/
---

# UrlCodec

`com.ajaxjs.util.UrlCodec` 操作的是**单个原始参数值**，不是完整 URL。构造器接收 `String` 或 `String, Charset`，默认 UTF-8，参数不能为 null。

| 操作 | 空格 | 字面加号 |
| --- | --- | --- |
| `encodeForm()` | `+` | `%2B` |
| `encodeQueryValue()` | `%20` | `%2B` |
| `decodeForm()` | 把 `+` 解码为空格 | 使用 `%2B` |
| `decodeQueryValue()` | 解码 `%20` | 保留 `+` |

查询值编码保留 `~`，转义 `*`。已有百分号转义会再次编码；非法百分号转义抛出 `IllegalArgumentException`。

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

## 其他操作

`concatUrl(String, String)` 只处理边界斜线拼接，不进行 URI 解析、校验或规范化，也不特殊处理查询串和片段。

`simpleGET(String)` 打开 URL 流，不设置显式超时或请求头；以 UTF-8 读取，规范化换行并追加末尾分隔符，关闭输入流。URL 构造/打开失败返回 null，读取失败可能抛出 `UncheckedIOException`。需要控制请求时使用 [AJ HTTP API](/aj-http/http_request/Base-cn/)。查询串解析使用 `MapTool.toMap(String)`。

## 先编码值，再组装 URL

应分别编码每个键和值。不要把 `?`、`&`、`=` 或已经拼好的 URL 传给 `encodeQueryValue()`；在这个 API 中它们都是数据，
会被转义。这样可避免参数歧义与意外的二次编码。

```java
String q = new UrlCodec("Java 8 + streams").encodeQueryValue();
String page = new UrlCodec("2").encodeQueryValue();
String url = UrlCodec.concatUrl("https://example.com/api", "search")
        + "?q=" + q + "&page=" + page;
// https://example.com/api/search?q=Java%208%20%2B%20streams&page=2
```

该类有意不解析或构建多值查询参数 Map。若完整 URL 需要校验、相对基地址解析，或需要从结构化组件构造，应在边界使用
`java.net.URI` 或专门的 HTTP 客户端。

## 表单语义与查询值语义

使用 `application/x-www-form-urlencoded` 的 HTML 表单以 `+` 表示空格；`URLEncoder`/`URLDecoder` 遵循这一约定。
RFC 3986 风格的查询值通常以 `%20` 表示空格，并保留字面加号。这一区别对签名 URL 与 token 尤其重要。

```java
String form = new UrlCodec("a b+c").encodeForm();             // a+b%2Bc
String rawQuery = new UrlCodec("a b+c").encodeQueryValue();  // a%20b%2Bc

new UrlCodec("a+b%2Bc").decodeForm();          // "a b+c"
new UrlCodec("a%20b+c").decodeQueryValue();    // "a b+c"
```

百分号转义表示使用该 codec 字符集的字节，默认 UTF-8。编码时字面 `%` 不会被当成转义：
`new UrlCodec("%2F").encodeQueryValue()` 会得到 `%252F`。只对确认只编码过一次的文本解码；非法 `%` 序列会快速
抛出 `IllegalArgumentException`。

## 路径拼接与简单读取

`concatUrl` 至多删除第一个部分末尾的一个斜线，并根据第二部分是否有前导斜线进行拼接。它对常规路径片段很可预测，但
有意不处理内部重复斜线、查询串和片段。

```java
UrlCodec.concatUrl("https://host/api/", "/users"); // https://host/api/users
UrlCodec.concatUrl("https://host/api", "users");  // https://host/api/users
```

`simpleGET` 仅适用于小型、可信、尽力而为的读取。它没有超时、请求头、状态码处理、重定向策略、认证或响应字符集检测。
构造或打开 URL 失败时返回 `null`；内容读取阶段失败仍可能抛出 `UncheckedIOException`。常规网络调用请使用 AJ HTTP API。

## 实现原理与取舍

表单方法直接委托 JDK 的 `URLEncoder`/`URLDecoder`。查询值编码先得到表单编码结果，再把 `+` 改为 `%20`、`*` 改为
`%2A`，并把 `%7E` 还原为 `~`。查询值解码时，会先把字面加号改写为 `%2B`，再调用 `URLDecoder`。这个小适配既保留
JDK 对百分号字节的可靠处理，也提供 RFC 3986 风格的值语义。
