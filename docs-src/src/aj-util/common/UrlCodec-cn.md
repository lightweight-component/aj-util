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
