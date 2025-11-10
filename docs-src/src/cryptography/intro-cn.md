---
title: 加密解密包简介
description: 整个库中使用的常见加密实用工具
tags:
  - 加密
  - 解密
  - 实用工具
layout: layouts/aj-util-cn.njk
---

# Java AES/DES/RSA 加密解密 API

JDK 中蕴含了主流的加密解密 API，包括 AES/DES/3DES/RSA，这些功能主要由`javax.crypto`（JCE, Java Cryptography Extension）和`java.security`包提供。其中只有 RSA 属于非对称加密（Asymmetric Encryption），其他都是对称加密（Symmetric Encryption），它们之间的异同如下：

- 之所以被称为“对称加密”，即无论加密还是解密都是同一一个密钥（key），即为“对称”，反之加密一个密钥、解密另外一个密钥的话，则为“非对称”
- 对称加密一般执行速度较快，但相对于非对称加密安全性较差；而非对称加密则恰恰相反，安全性较高但执行效率相对差
- 两者之间没有绝对优劣之分，应视乎场合需求择优选用

# 源码
最终的代码在[这里](https://gitcode.com/lightweight-component/aj-util/tree/main/src/main/java/com/ajaxjs/util/cryptography)。

使用方式可以参见[单测](https://gitcode.com/lightweight-component/aj-util/blob/main/src/test/java/com/ajaxjs/util/cryptography/TestCryptography.java)。

Maven 依赖引用，需要 Java8+

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-cryptography</artifactId>
    <version>1.0</version>
</dependency>
```
该组件 jar 包体积很小，才 20kb~。只依赖了我自己写的一个工具库。