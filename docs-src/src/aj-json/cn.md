---
title: aj-json
layout: layouts/aj-util-cn.njk
---

# aj-json：为了学习而写的 JSON 解析器

aj-json 是一个为了学习 JSON 如何解析、了解一点简单编译原理而写的小项目。它**不是可用于生产环境的 JSON 类库**。更适合把它当成一份可阅读、可修改、可跟着调试的学习材料：看看一串字符怎样一步步变成 Java 对象。

生产项目请使用有持续维护的 JSON 库，例如 Jackson、Gson 或 JSON-B。不要将 aj-json 用于不可信输入、需要严格兼容的接口，或不能出现意外解析结果的数据。

## 能从这个项目学到什么

程序拿到一段 JSON 文本后，主要要解决两个问题：

1. 每一小段文本代表什么？例如 `{` 表示对象开始，`true` 表示布尔值。
2. 这些小段能以什么顺序出现？例如对象的键后面必须是 `:`，再后面才是值。

aj-json 把这两件事分开。`Lexer` 负责逐个读取字符并贴上“标签”（token）；`FMS`（状态机）按照顺序读取 token，再组装出普通的 Java 值。

## 重要限制

- 当前源码中没有可作为正式功能使用的“Java 对象转 JSON”序列化器。
- 解析器的目标是小而易读，不是完整实现 JSON 标准，也不承诺兼容性。
- 它接受一些“看上去像 JSON”但不是标准 JSON 的写法，例如对象键不加引号、字符串用单引号。
- 解析对象使用 `HashMap`，不会保留原始字段顺序。
- 数字会变成 `Integer`、`Long` 或 `Double`；需要精确小数时不合适。
- 它没有按照生产流量或恶意输入进行设计、测试和加固。

## 从这里开始

只有在跟着示例学习或阅读源码时，才建议添加依赖：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-json</artifactId>
    <version>1.6</version>
</dependency>
```

接下来阅读[动手解析 JSON](/aj-json/parse-cn/)。想了解内部过程时，继续看[解析器如何工作](/aj-json/how-it-works-cn/)。

## 源码与参考

- [源代码](https://github.com/lightweight-component/aj-util/tree/main/aj-json)
- [Maven Central](https://central.sonatype.com/artifact/com.ajaxjs/aj-json)
- [Javadoc](https://javadoc.io/doc/com.ajaxjs/aj-json)
