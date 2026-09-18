---
title: Base64Utils
description: Base64Utils
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /common/Base64Utils/
---

# Base64Utils

`com.ajaxjs.util.Base64Utils` 封装 Java 标准及 URL-safe Base64 编解码。Base64 是编码，不是加密。

## 输入和输出

构造器接收 `byte[]`、`String`（UTF-8）或 `String, Charset`。输入和构造器字符集不能为 null。字节数组保留引用，不会复制。

`setUrlSafe(true)` 同时选择编码和解码的 URL 字母表；`setWithoutPadding(true)` 仅影响编码填充。两个 setter 都返回当前实例。

`encode()`、`decode()` 返回字节；`encodeAsString()` 返回 ASCII Base64。`decodeAsString()` 默认 UTF-8，`Charset` 重载指定解码后文本的字符集。非法 Base64 抛出 `IllegalArgumentException`。

```java
import com.ajaxjs.util.Base64Utils;
import java.nio.charset.StandardCharsets;

String encoded = new Base64Utils("你好", StandardCharsets.UTF_16)
        .setUrlSafe(true).setWithoutPadding(true).encodeAsString();
String decoded = new Base64Utils(encoded).setUrlSafe(true)
        .decodeAsString(StandardCharsets.UTF_16);
```

`formatPemBase64(String)` 每 64 个字符换行，不追加末尾换行或 PEM 头尾。即使原文是 UTF-16，解码 Base64 文本时也应使用默认字符串构造器。
