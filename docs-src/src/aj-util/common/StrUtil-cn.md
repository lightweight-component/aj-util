---
title: StrUtil
description: 字符串模板与数组、列表拼接。
tags:
  - 字符串
  - 模板
layout: layouts/aj-util-cn.njk
---

# StrUtil

`StrUtil` 当前提供简单模板替换以及数组、列表拼接；不再提供填充、字符计数或成员判断方法。

## 模板

Map 重载替换 `${name}` 占位符，Map 中缺失或为 null 的值替换为空字符串。

```java
Map<String, Object> values = new HashMap<>();
values.put("name", "Ada");
String message = StrUtil.simpleTpl("Hello, ${name}! ${missing}", values);
// Hello, Ada!
```

JavaBean 重载替换可读属性对应的 `#{property}` 占位符。Bean 属性为 null 时替换为文本 `null`，只写属性会跳过。替换值按字面量处理，`$` 和反斜杠可安全使用。getter 执行失败时，抛出的 `RuntimeException` 会标明属性名并保留原始异常作为 cause。

```java
String message = StrUtil.simpleTpl("Hello, #{name}!", user);
```

## 拼接数组和列表

```java
String array = StrUtil.join(new String[]{"a", null, "c"}, "&");
String list = StrUtil.join(Arrays.asList("a", "b", "c"), "[%s]", ", ");
// a&&c
// [a], [b], [c]
```

可用重载包括泛型数组、带格式模板的字符串数组，以及带可选模板的字符串列表。null 元素会输出为空字符串。

## 占位符和格式规则

两类模板重载刻意使用不同的占位符语法：

| 重载 | 占位符 | 缺失/null 值 |
| --- | --- | --- |
| `simpleTpl(String, Map)` | `${word}` | 空字符串 |
| `simpleTpl(String, Object)` | `#{property}` | 可读属性为 null 时输出文本 `null` |

Map 占位符仅匹配单词字符（`${name_1}` 有效，`${user.name}` 不属于该 API 的占位符）。无法识别的文本保持不变。Bean 重载检查 JavaBean getter 属性（包含继承属性），不直接读取字段。

```java
Map<String, Object> values = new HashMap<>();
values.put("path", "$1\\temp");
String result = StrUtil.simpleTpl("path=${path}", values);
// path=$1\temp
```

之所以能够安全处理 `$1`，是因为替换内容在调用 `Matcher.appendReplacement` 前会被引用；`$1` 只是数据，不是正则反向引用。列表拼接的模板会传入 `String.format`，因此应包含如 `%s` 的合适转换符。

## 选择合适的工具

`StrUtil` 专注于小型格式化操作。正则替换使用 `RegExpHelper`；URL/表单编码使用 `UrlCodec`；字节、字符集和十六进制转换使用 `StringBytes`。

## 实现原理与取舍

Map 模板重载使用一个预编译 `Pattern` 扫描 `${word}`，再通过 `Matcher.appendReplacement` / `appendTail` 构造结果。对替换文本进行引用后，正则引擎不会把值中的 `$` 或反斜杠解释为特殊语法。Bean 重载则使用 JavaBeans 内省，并针对每个可读属性调用普通 `String.replace`；这也解释了它为何使用 `#{property}` 语法，以及为什么 null 行为不同。

拼接方法使用 `StringBuilder`，仅在元素之间附加分隔符，从而避免尾部分隔符。它们刻意不转义值、也不进行依赖 Locale 的格式化；需要时应在拼接前完成这些转换。
