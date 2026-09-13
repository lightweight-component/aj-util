---
title: ResourceHelper
description: 定位并读取类路径资源
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /io/ResourceHelper/
---

# ResourceHelper

`ResourceHelper` 用于定位类路径资源。不传目标类时，资源名相对于 classpath 根目录；传入目标类时，解析规则与 `Class.getResource` 一致。

```java
String text = new ResourceHelper("config/application.properties").toString();
Properties properties = new ResourceHelper("config/application.properties").getProperties();

InputStream stream = new ResourceHelper("config.properties", MyService.class).getStream();
```

`getStream()` 可读取打包在 JAR 内的资源。`getPath(boolean)` 和 `list(Consumer<File>)` 需要资源实际位于文件系统中，对 `jar:` URL 不一定可用。`getJarDir()` 在运行位置可确定时返回 JAR 所在目录。调用方需要自行关闭 `getStream()` 返回的流。

