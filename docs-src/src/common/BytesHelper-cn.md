---
title: Base64Utils 教程
description: Base64Utils 是一个用于 Base64 编码和解码操作的工具类
tags:
  - Base64
  - Java
layout: layouts/aj-util-cn.njk
---
# Base64Utils 使用教程

Base64Utils 是一个用于 Base64 编码和解码操作的工具类，提供了便捷的方法在字节数组和字符串之间进行转换。

### 主要功能特性

1. **支持标准和URL安全的Base64编码**
2. **可配置是否省略填充字符**
3. **支持多种字符集**
4. **链式调用API设计**

### 基本使用方法

#### 1. 创建实例

```java
// 从字节数组创建
byte[] data = "Hello World".getBytes();
Base64Utils utils = new Base64Utils(data);

// 从字符串创建（默认UTF-8）
Base64Utils utils = new Base64Utils("Hello World");

// 从字符串创建（指定字符集）
Base64Utils utils = new Base64Utils("Hello World", StandardCharsets.UTF_8);
```


#### 2. Base64编码

```java
// 基本编码
String encoded = new Base64Utils("Hello World").encodeAsString();
// 输出: SGVsbG8gV29ybGQ=

// URL安全编码
String urlSafeEncoded = new Base64Utils("Hello World")
    .setUrlSafe(true)
    .encodeAsString();
// 输出: SGVsbG8gV29ybGQ=

// 不带填充的编码
String noPadding = new Base64Utils("Hello World")
    .setWithoutPadding(true)
    .encodeAsString();
// 输出: SGVsbG8gV29ybGQ (没有末尾的=)
```


#### 3. Base64解码

```java
// 基本解码
String decoded = new Base64Utils("SGVsbG8gV29ybGQ=").decodeAsString();
// 输出: Hello World

// 指定字符集解码
String decodedWithCharset = new Base64Utils("SGVsbG8gV29ybGQ=")
    .decodeAsString(StandardCharsets.UTF_8);

// URL安全解码
String urlDecoded = new Base64Utils("SGVsbG8gV29ybGQ=", true)
    .setUrlSafe(true)
    .decodeAsString();
```


#### 4. 链式调用

由于使用了 `@Accessors(chain = true)` 注解，可以进行链式调用：

```java
String result = new Base64Utils("Hello World")
    .setUrlSafe(true)
    .setWithoutPadding(true)
    .encodeAsString();
```


#### 5. 格式化Base64字符串

```java
// 将长Base64字符串格式化为每64个字符一行
String longBase64 = "SGVsbG8gV29ybGQhIEkgYW0gYSBsb25nIHN0cmluZyBmb3IgdGVzdGluZyB0aGUgZm9ybWF0dGluZyBmdW5jdGlvbi4=";
String formatted = Base64Utils.formatBase64String(longBase64);
```


### 参数说明

- [withoutPadding](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\Base64Utils.java#L57-L57): 是否省略编码结果末尾的 `=` 字符
- [urlSafe](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\Base64Utils.java#L63-L63): 是否使用URL安全的Base64变体（使用 `-` 替代 `+`，`_` 替代 `/`）

### 注意事项

1. 如果未设置输入数据就调用编码或解码方法，会抛出 `IllegalStateException`
2. 编码结果默认使用 `ISO_8859_1` 字符集转换为字符串
3. 解码默认使用 `UTF-8` 字符集转换为字符串

这个工具类简化了Java中的Base64操作，特别适合需要频繁进行Base64编解码的场景。