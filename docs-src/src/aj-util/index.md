---
title: AJ Utilities
description: Lightweight, object-oriented utilities for Java 8 and later
date: 2026-09-13
tags:
  - AJ Utilities
  - Java Utils
  - Documentation
layout: layouts/aj-util.njk
---

# AJ Utilities

AJ Utilities is a lightweight Java 8+ library. Its APIs favour small, focused objects where state or configuration is
needed, while keeping stateless operations convenient. The project provides source code, Javadoc, and unit tests for
common application tasks.

| Area | Main APIs | Description |
| --- | --- | --- |
| Encoding and hashes | `Base64Utils`, `StringBytes`, `UrlCodec`, `HashHelper` | Base64, hexadecimal, URL values, message digests, and HMACs. |
| General utilities | `ConvertBasicValue`, `ObjectHelper`, `MapTool`, `RandomTools`, `StrUtil`, `RegExpHelper` | Type conversion, objects, maps, random values, strings, and cached regular expressions. |
| Structured data | `JsonUtil`, `XmlHelper` | JSON conversion through a pluggable engine and secure XML helpers. |
| Date and time | `DateTools`, `DateTypeConvert`, `Formatter` | Java 8 time formatting, strict parsing, and conversion between legacy and modern types. |
| I/O | `FileHelper`, `ResourceHelper`, `DataReader`, `DataWriter`, `ZipHelper`, `UnzipHelper` | File, classpath resource, stream, ZIP creation, and protected ZIP extraction operations. |
| Cryptography | `DoCipher`, `AesGcm`, `AesCbc`, `AesPbe`, `AesLegacy`, `Rsa`, `SecretKeyMgr`, `RestoreKey`, `PemUtils`, `DoSignature`, `DoVerify` | AES, password-based encryption, RSA-OAEP, keys and signatures; see the [cryptography guide](/aj-util/cryptography/intro/). |
| Reflection | `Clazz`, `Fields`, `Methods`, `NewInstance`, `Types` | Class loading, member lookup, object construction, and generic type inspection. |
| Logging | `Trace`, `TextBox`, `EnableOperationLog` | MDC trace context, formatted console text boxes, and an operation-log marker annotation. |

HTTP requests, uploads, downloads and annotation proxies now belong to the separate [AJ HTTP project](/aj-http/). Its Java package remains `com.ajaxjs.util.httpremote`, but it requires its own dependency; see the AJ HTTP installation guide.

## Source code

[GitHub](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util) | [Gitee](https://gitee.com/lightweight-components/aj-util)

## Links

[User Manual](https://aj-util.ajaxjs.com) | [Ask DeepWiki](https://deepwiki.com/lightweight-component/aj-util) | [Java Documents](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util)

## Install

Runs on Java 8+. Maven:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
