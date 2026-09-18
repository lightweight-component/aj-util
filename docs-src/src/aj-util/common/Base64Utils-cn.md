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

## 标准、URL-safe 与填充模式

标准 Base64 使用 `+` 和 `/`。URL-safe Base64 改用 `-` 与 `_`，避免作为 URL 参数时还要转义这些字符。填充字符
`=` 用于补足四个输出字符一组；JWT 等格式可能省略填充，但是否接受由接收协议决定。

```java
byte[] tokenBytes = new byte[]{(byte) 0xfb, (byte) 0xff};

String standard = new Base64Utils(tokenBytes).encodeAsString(); // +/8=
String urlToken = new Base64Utils(tokenBytes)
        .setUrlSafe(true).setWithoutPadding(true).encodeAsString(); // -_8

byte[] original = new Base64Utils(urlToken).setUrlSafe(true).decode();
```

解码时，`urlSafe` 的选择必须与输入文本使用的字母表一致。`withoutPadding` 只是编码设置；解码可否接受无填充表示由
JDK 解码器决定，它不会自动替你选择 URL-safe 字母表。

## 二进制数据与文本的边界

Base64 传递的是字节。`String` 构造器和 `decodeAsString(...)` 是便利的文本边界，分别把文本转为字节、把字节转回文本。
文件、摘要、密钥等二进制材料应使用 `byte[]` 构造器。

```java
byte[] digest = MessageDigest.getInstance("SHA-256")
        .digest("payload".getBytes(StandardCharsets.UTF_8));
String wireValue = new Base64Utils(digest).encodeAsString();

byte[] restoredDigest = new Base64Utils(wireValue).decode();
```

传给构造器的 `byte[]` 会按引用保留。除非有意让结果随之改变，否则不要在构造后、调用 `encode()`/`decode()` 前修改数组。
Base64 不提供保密性：任何拿到文本的人都可以解码；同时它会把数据扩展为大约每 3 个输入字节对应 4 个输出字符。

## 实现原理与取舍

实现直接委托 `java.util.Base64`，而非维护自定义编解码器。因此可获得 JDK 的输入校验行为（非法输入抛出
`IllegalArgumentException`），并让标准与 URL-safe 行为始终与运行时保持一致。`encodeAsString()` 明确使用
US-ASCII，因为 Base64 输出天生就是 ASCII。
