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

## 如何选择资源名

两个构造器的查找根目录不同。这正是资源明明在 `src/test/resources` 中、运行时却找不到的常见原因。

| 构造方式 | 资源名 | 查找位置 |
| --- | --- | --- |
| `new ResourceHelper("a/b.txt")` | 无前导斜线 | classpath 根目录：`a/b.txt` |
| `new ResourceHelper("file.txt", MyService.class)` | 无前导斜线 | `MyService` 所在包 |
| `new ResourceHelper("/file.txt", MyService.class)` | 有前导斜线 | classpath 根目录：`file.txt` |

例如，若测试资源位于
`src/test/resources/com/ajaxjs/util/io/test.txt`，可相对于测试类读取：

```java
try (InputStream in = new ResourceHelper("test.txt", MyIoTest.class).getStream()) {
    byte[] bytes = new DataReader(in).readAsBytes();
    String text = new String(bytes, StandardCharsets.UTF_8);
}

// 同一个构造器中，加上前导斜线后会改为从 classpath 根目录查找。
String rootText = new ResourceHelper("/test.txt", MyIoTest.class).toString();
```

当资源不存在时，`getStream()`、`toString()` 与 `getProperties()` 会抛出 `IllegalArgumentException`。这样可以在
定位资源的位置立即暴露必需配置缺失的问题，而不是在后面以 `NullPointerException` 的形式失败。

## 读取文本和 properties

`toString()` 委托给 `DataReader`：以 UTF-8 解码、把换行规范化为当前平台分隔符，并关闭流。`getProperties()`
加载标准 Java `Properties` 文件，也会关闭流。

```java
Properties db = new ResourceHelper("test-demo.properties").getProperties();
String username = db.getProperty("database.username");

String json = new ResourceHelper("fixtures/request.json").toString();
```

当图片解码器、证书解析器等下游 API 需要流时，请使用 `getStream()`。此时由调用方负责关闭，且不要在下游 API
尚未读取完之前提前关闭它。

## 文件系统与打包资源的边界

classpath 查找返回的是 URL，并不一定对应一个 `File`。开发环境通常是 `file:` URL；部署后的应用常常是 `jar:`
URL。`getPath(true)` 只能把前者转换为可用的文件系统路径，因此它适用于测试或未打包应用，却不适合必须从可执行
JAR 内读取的资源。`list(Consumer<File>)` 也有同样的 `file:` 限制，并且枚举的是 classpath 根目录。

`getJarDir()` 用来定位运行产物旁边的文件，不是用于查找产物内部资源。`getClassName(File, String)` 根据 `.class`
文件名与包前缀生成二进制类名，并会拒绝非 `.class` 文件。

## 实现原理与取舍

该类不会把资源复制到临时目录，而是优先保留 classpath 的抽象并暴露流。因此，`getStream()` 能可靠读取 JAR 内
资源，而面向文件的 API 则不能保证可用。除非外部 API 确实要求 `Path` 或 `File`，否则优先使用流。

