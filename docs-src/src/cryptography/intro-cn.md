---
title: 加密解密包简介
description: 整个库中使用的常见加密实用工具
tags:
  - 加密
  - 解密
  - 实用工具
layout: layouts/aj-util-cn.njk
---

# 加密与解密

`aj-util` 对 Java 8 的 JCE 与安全 API 做了轻量封装，覆盖对称加密、RSA 密钥管理和数字签名。主要入口为 [Cryptography](Cryptography-cn.md) 与 [RSA、密钥和签名](Rsa-cn.md)。

- 对称加密使用同一把密钥加密和解密，适合一般业务数据。
- RSA 使用公私钥对，通常用于保护较小的对称密钥或做签名，不适合直接加密大内容。
- 新设计的数据存储或通信协议建议优先使用 AES-GCM 或 PBE 辅助方法；DES、3DES、MD5、SHA-1 仅用于兼容遗留系统。

# 源码

最终的代码在[这里](https://gitcode.com/lightweight-component/aj-util/tree/main/src/main/java/com/ajaxjs/util/cryptography)。

使用方式也可参考模块内的单元测试。

Maven 依赖引用，需要 Java8+

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

该模块兼容 Java 8，密码学 API 没有强制的第三方运行时依赖。
