---
title: AJ Utilities 简介
description: 面向 Java 8 及以上版本的轻量级、面向对象工具库
date: 2026-09-13
tags:
  - AJ Utilities
  - Java 工具库
  - 中文文档
layout: layouts/aj-util-cn.njk
---

# AJ Utilities 简介

轻量级 Java 8+ 工具库。在需要保存配置或状态的场景中，API 采用小而清晰的对象；无状态操作则保持简洁。项目提供源码、Javadoc 与单元测试。

| 领域 | 主要 API | 说明 |
| --- | --- | --- |
| 编码与摘要 | `Base64Utils`、`StringBytes`、`UrlCodec`、`HashHelper` | Base64、十六进制、URL 参数、消息摘要与 HMAC。 |
| 常用工具 | `ConvertBasicValue`、`ObjectHelper`、`MapTool`、`RandomTools`、`StrUtil`、`RegExpUtils` | 类型转换、对象、Map、随机值、字符串和正则表达式。 |
| 结构化数据 | `JsonUtil`、`XmlHelper` | 可替换 JSON 引擎与安全的 XML 工具。 |
| 日期时间 | `DateTools`、`DateTypeConvert`、`Formatter` | Java 8 时间格式化、严格解析和新旧日期类型转换。 |
| I/O | `FileHelper`、`ResourceHelper`、`DataReader`、`DataWriter`、`ZipHelper`、`UnzipHelper` | 文件、类路径资源、流、ZIP 压缩和带保护的 ZIP 解压。 |
| HTTP | `Get`、`Post`、`Put`、`Delete`、`Head`、`Request`、`Response` | 轻量 HTTP 请求、上传、下载和注解式调用。 |
| 密码学 | `Cryptography`、`SecretKeyMgr`、`KeyMgr`、`DoSignature`、`DoVerify` | 对称加密、PBE、RSA 密钥管理、签名和验签。 |
| 反射 | `Clazz`、`Fields`、`Methods`、`NewInstance`、`Types` | 类加载、成员查找、对象构造与泛型类型检查。 |

## 源代码

[GitHub](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util) | [Gitee](https://gitee.com/lightweight-components/aj-util)

## 链接

[用户手册](https://aj-util.ajaxjs.com) |  [Ask DeepWiki](https://deepwiki.com/lightweight-component/aj-util) | [Java 文档](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util)

## 安装

需要 Java 8+ 或以上。Maven 坐标：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
