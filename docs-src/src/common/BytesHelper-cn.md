---
title: BytesHelper 教程
description: 字节数组拼接和十六进制转换工具
tags:
  - 字节数组
  - 十六进制
  - Java
layout: layouts/aj-util-cn.njk
---

# BytesHelper 教程

`BytesHelper` 提供无状态的字节数组辅助方法。

## 拼接数组

```java
byte[] result = BytesHelper.concat(new byte[]{1, 2}, new byte[]{3, 4});
// [1, 2, 3, 4]
```

## 编码为十六进制

```java
String hex = BytesHelper.bytesToHexStr(new byte[]{0x1A, 0x2B});
// "1A2B"
```

输出统一使用大写十六进制字符。

## 解码十六进制

```java
byte[] bytes = BytesHelper.parseHexStr2Byte("1A2B");
```

空字符串返回空字节数组。输入长度必须为偶数，且所有字符都必须是合法的十六进制字符，否则抛出 `IllegalArgumentException`；奇数长度输入不会再被静默截断。
