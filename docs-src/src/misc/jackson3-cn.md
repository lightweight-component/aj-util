---
title: 使用 Jackson 3
description: 使用 Jackson 3
tags:
  - Jackson3
layout: layouts/aj-util.njk
---

# 使用 Jackson 3

`aj-util` 默认集成了 Jackson 2.x 版本的 JSON 库，它与 Java 8 配合良好。然而，随着 JDK 11/17 的普及，升级到 Jackson 3.x 变得很有必要。`aj-util` 支持新版本的 Jackson，并提供了一个统一的 JSON 工具函数接口。本文将向您展示如何在 `aj-util` 中使用 Jackson 3。

### 第一步：排除 Jackson 2 依赖

首先，排除 Jackson 2 的依赖并添加 Jackson 3 的依赖。如果您使用的是 Spring，可以跳过此步骤，因为 Spring 框架已经包含了 Jackson 3。

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.6</version>

    <exclusions>
        <exclusion>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

由于旧版 Jackson 库的 Maven 作用域（scope）是 `provided`，实际上您不必排除上述依赖。但是，下一步是必须的。

### 第二步：配置 Java SPI

在 `src/main/resources/META-INF/services` 目录下创建一个名为 `com.ajaxjs.util.json.JsonEngineProvider` 的文件。这是一个 Java SPI 配置文件。

该文件的内容应为：

```text
com.ajaxjs.util.json.jackson3.Jackson3Provider
```

### 第三步：添加 Jackson 3 适配器

Jackson 3 的适配器位于以下地址：

- [Jackson3Engine.java](https://gitee.com/lightweight-components/ajaxjs/blob/master/aj-spring-base/src/main/java/com/ajaxjs/util/json/jackson3/Jackson3Engine.java)
- [Jackson3Provider.java](https://gitee.com/lightweight-components/ajaxjs/blob/master/aj-spring-base/src/main/java/com/ajaxjs/util/json/jackson3/Jackson3Provider.java)

只需将这两个文件复制到您的源代码中即可。

至此，您已完成 `aj-util` 与 Jackson 3 的集成配置。

### 可选：排除不必要的库

如果您的项目中不需要，还可以选择性地排除 SLF4j、Logback、JUnit 和 Jakarta Servlet 等库。

```xml
<exclusion>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</exclusion>

<exclusion>
<groupId>ch.qos.logback</groupId>
<artifactId>logback-classic</artifactId>
</exclusion>

<exclusion>
<groupId>org.junit.jupiter</groupId>
<artifactId>junit-jupiter</artifactId>
</exclusion>

<exclusion>
<groupId>jakarta.servlet</groupId>
<artifactId>jakarta.servlet-api</artifactId>
</exclusion>
```