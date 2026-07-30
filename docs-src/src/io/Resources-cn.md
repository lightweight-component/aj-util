---
title: Resources
description: 对文件和 JAR 使用一致的 classpath 资源加载方式
tags:
  - 资源加载
  - classpath
  - Java
layout: layouts/aj-util-cn.njk
---

# Resources

`Resources` 可以把 classpath 资源读取为输入流、文本、Properties 或文件系统路径。

所有资源查找方法在资源不存在时统一抛出 `IllegalArgumentException`，不会返回 `null`，也不会向
标准错误输出诊断文本。

## 输入流、文本和 Properties

需要同时支持 IDE 和打包 JAR 时，应使用流式 API：

```java
try (InputStream in = Resources.getResource("config/app.yml")) {
    // 读取资源
}

String text = Resources.getResourceText("templates/page.html");
Properties properties = Resources.getProperties("application.properties");
```

## 文件系统路径

`getResourcesFromClasspath(...)` 和 `getResourcesFromClass(...)` 只对 `file:` URL 返回路径：

```java
String path = Resources.getResourcesFromClasspath("com/example/config.properties");
String relative = Resources.getResourcesFromClass(MyClass.class, "template.txt");
```

JAR 内资源不是普通 `Path`；这些方法遇到非文件 URL 时抛出 `UnsupportedOperationException`。读取
JAR 内容应使用 `getResource(...)`，不能解析 URL 的展示字符串来伪造文件路径。

`getClassName(File, String)` 根据 `.class` 文件名构造完整类名；`getJarDir()` 返回当前代码源的父目录。
