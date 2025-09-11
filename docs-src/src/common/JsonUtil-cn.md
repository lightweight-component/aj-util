---
title: JsonUtil 教程
description: 为 Java 应用程序中的 JSON 数据处理提供实用方法
tags:
  - JSON
  - 序列化
  - Java
layout: layouts/aj-util-cn.njk
---

# JsonUtil 教程

本教程提供了 `JsonUtil` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`JsonUtil` 类为 Java 应用程序中的 JSON 数据处理提供了实用工具方法。

## 简介

`JsonUtil` 类包含使用 Jackson 库的静态方法，用于常见的 JSON 操作，包括 JSON 字符串、Java 对象、Map 和 List 之间的转换。

## 主要特性

- 将 Java 对象转换为 JSON 字符串（美化或紧凑格式）
- 将 JSON 字符串解析为 Java 对象
- JSON 与 Map/List 之间的转换
- 类型安全的 JSON 处理
- Java 8 日期/时间支持
- 严格的重复检测

## 方法

### 1. 序列化 (转 JSON)

1. `toJson(Object obj)` - 将对象转换为 JSON 字符串
2. `toJsonPretty(Object obj)` - 将对象转换为美化格式的 JSON

### 2. 反序列化 (从 JSON)

1. `fromJson(String jsonStr, Class<T> valueType)` - 将 JSON 解析为指定类型
2. `fromJson(String jsonStr, JavaType valueType)` - 将 JSON 解析为复杂类型

### 3. JSON ↔ Map 转换

1. `json2map(String jsonStr)` - JSON 转 Map<String, Object>
2. `json2map(String jsonStr, Class<T> clazz)` - JSON 转带类型值的 Map
3. `json2sortMap(String jsonStr, Class<T> clazz)` - JSON 转保留顺序的 LinkedHashMap
4. `json2StrMap(String jsonStr)` - JSON 转 Map<String, String>

### 4. JSON ↔ List 转换

1. `json2list(String jsonArrayStr, Class<T> clazz)` - JSON 数组转 List<T>
2. `json2mapList(String jsonArrayStr)` - JSON 数组转 List<Map>

### 5. 对象转换

1. `convertValue(Object obj, Class<T> clazz)` - 转换对象类型
2. `pojo2map(Object obj)` - POJO 转 Map
3. `pojo2map(Object obj, Class<T> clazz)` - POJO 转带类型的 Map
4. `map2pojo(Map<String, Object> map, Class<T> clazz)` - Map 转 POJO

### 6. JSON 树模型

1. `json2Node(String jsonStr)` - 将 JSON 解析为 JsonNode 树

## 使用示例

### 基本序列化
```java
User user = new User("John", 30);
String json = JsonUtil.toJson(user); 
// {"name":"John","age":30}

String prettyJson = JsonUtil.toJsonPretty(user);
/*
{
  "name" : "John",
  "age" : 30
}
*/
```

### 基本反序列化
```java
String json = "{\"name\":\"John\",\"age\":30}";
User user = JsonUtil.fromJson(json, User.class);
```

### Map 转换
```java
Map<String, Object> map = new HashMap<>();
map.put("name", "John");
map.put("age", 30);

String json = JsonUtil.toJson(map);
Map<String, Object> map2 = JsonUtil.json2map(json);
```

### List 转换
```java
String jsonArray = "[{\"name\":\"John\"}, {\"name\":\"Alice\"}]";
List<User> users = JsonUtil.json2list(jsonArray, User.class);
```

## 配置

`JsonUtil` 类预配置了：
- Java 8 日期/时间支持
- 严格的重复检测
- 亚洲/上海时区
- 默认的 ObjectMapper 实例

## 结论

`JsonUtil` 类提供了全面的实用方法，用于处理 JSON 数据，使序列化、反序列化以及 JSON 与 Java 对象之间的转换更加容易。