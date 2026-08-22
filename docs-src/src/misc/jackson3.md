---
title: Working with Jackson 3
description: Working with Jackson 3
tags:
  - Jackson3
layout: layouts/aj-util.njk
---

# Working with Jackson 3

`aj-util` ships with the Jackson JSON library by default, currently at version 2.x, which works well with Java 8.
However, as JDK 11/17 becomes more popular, upgrading to Jackson 3.x is necessary. `aj-util` supports newer versions of
Jackson and provides a unified interface for JSON utility functions. This article will show you how to use Jackson 3
with `aj-util`.

### Step 1: Exclude Jackson 2 Dependencies

First, exclude the Jackson 2 dependencies and add the Jackson 3 dependencies. If you're using Spring, you can omit this
step because the Spring framework already includes Jackson 3.

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

Since the Maven scope of the old Jackson library is `provided`, you don't actually have to exclude the dependencies
mentioned above. However, the next step is necessary.

### Step 2: Configure Java SPI

Create a file named `com.ajaxjs.util.json.JsonEngineProvider` under the folder `src/main/resources/META-INF/services`.
This is a Java SPI configuration file.

The content of this file should be:

```text
com.ajaxjs.util.json.jackson3.Jackson3Provider
```

### Step 3: Add the Jackson 3 Adapter

The Jackson 3 adapter is located at:

- [Jackson3Engine.java](https://gitee.com/lightweight-components/ajaxjs/blob/master/aj-spring-base/src/main/java/com/ajaxjs/util/json/jackson3/Jackson3Engine.java)
- [Jackson3Provider.java](https://gitee.com/lightweight-components/ajaxjs/blob/master/aj-spring-base/src/main/java/com/ajaxjs/util/json/jackson3/Jackson3Provider.java)

Simply copy these two files into your source code.

With that, you have finished configuring `aj-util` to work with Jackson 3.

### Optional: Exclude Unnecessary Libraries

Optionally, you can also exclude libraries such as SLF4j, Logback, JUnit, and Jakarta Servlet if they are not needed in
your project.

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