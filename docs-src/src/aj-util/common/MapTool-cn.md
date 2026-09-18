---
title: MapTool
description: Map 的拼接、解析、转换与扁平化。
tags:
  - Map
  - 转换
  - Java
layout: layouts/aj-util-cn.njk
---

# MapTool

`MapTool` 提供静态方法，用于简单字符串键 Map 的序列化、键值输入解析、Map 值转换和嵌套 Map 扁平化。XML 转换不属于 `MapTool`；XML 操作请使用 `XmlHelper`。

## 拼接 Map 条目

```java
Map<String, Object> values = new LinkedHashMap<>();
values.put("name", "Ada");
values.put("page", null);

String queryLike = MapTool.join(values); // name=Ada&page=
String custom = MapTool.join(values, ";", value -> String.valueOf(value));
```

`join` 按 Map 的迭代顺序写出 `key=value` 条目，默认分隔符为 `&`，null 值转换为空字符串。它不会对键和值进行 URL 编码；生成 HTTP 查询串时应先使用 `UrlCodec` 编码。

## 由键值输入创建 Map

```java
Map<String, Object> fromPairs = MapTool.toMap(
        new String[]{"id=42", "page=3"}, Integer::valueOf);

Map<String, Object> fromColumns = MapTool.toMap(
        new String[]{"id", "name"}, new String[]{"42", "Ada"}, null);

Map<String, String> query = MapTool.toMap("name=Ada+Lovelace&token=x%3D%3D");
```

数组键值对只以第一个 `=` 分隔键和值；`null` 条目会被忽略，缺少 `=` 的条目会被拒绝。并行键值数组必须等长。查询串重载通过 `UrlCodec` 解码表单编码，重复键以后一个值覆盖前一个值。

## 转换和扁平化值

```java
Map<String, Integer> lengths = MapTool.as(values, value -> value.toString().length());

Map<String, Object> nested = new LinkedHashMap<>();
nested.put("server", Collections.singletonMap("host", "localhost"));
Map<String, Object> flat = MapTool.flatten(nested);
// {server.host=localhost}
```

`as` 使用指定函数转换非 null 值，并保留 null 值。`flatten` 使用 `.` 连接嵌套层级：源键中的 `.` 会转义为 `\\.`，反斜杠会转义为 `\\\\`；空嵌套 Map 会保留为值。循环引用的 Map 会抛出 `IllegalArgumentException`。

## 详细解析行为

两类 `toMap` 在面对格式不完整的输入时，有意采用不同策略：

```java
Map<String, Object> strict = MapTool.toMap(
        new String[]{"token=header.payload=signature=="}, null);
// strict.get("token") 为 "header.payload=signature=="

Map<String, String> lenient = MapTool.toMap("flag&name=Ada");
// "flag" 没有 '='，会被忽略；lenient 只包含 name=Ada
```

数组形式通常用于结构化配置，因此非 null 条目缺少 `=` 时会被拒绝；查询串形式更宽容，会忽略没有完整键值对的字段。两种形式都不会对 `join` 的输出做 URL 编码，也都不会保留同一键的多个值。

扁平化后键会成为路径；除非调用方理解转义规则，否则不能简单地将它当作可逆的原始键：

```java
Map<String, Object> nested = new LinkedHashMap<>();
nested.put("a.b", Collections.singletonMap("c", 42));

Map<String, Object> flat = MapTool.flatten(nested);
// {a\\.b.c=42}
```

`flatten(null)` 返回空的 `LinkedHashMap`。List、数组、基本值和 null 都是叶子节点，只有 `Map` 值会继续遍历。

## 实现原理与取舍

`join` 直接遍历 `map.keySet()`，因此输出顺序取决于具体 Map 的迭代顺序；需要稳定序列化顺序时应使用 `LinkedHashMap`。解析器最多只切分一次（`split("=", 2)`），因此 Base64、类似 JWT 的字符串和包含 `=` 的签名能够保持完整。

`flatten` 使用深度优先递归，并使用基于对象身份的集合记录当前递归路径中的 Map。使用 identity 而非 `equals()`，可以识别真实对象环，而不会把两个内容相等但相互独立的 Map 误判为环。每个分支结束后都会移除当前路径，因此同一个非循环子 Map 可以在不同分支中复用。
