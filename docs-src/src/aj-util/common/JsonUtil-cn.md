---
title: JsonUtil
description: 基于可替换 JSON 引擎的 JSON 转换工具
tags:
  - JSON
  - 序列化
  - Java
layout: layouts/aj-util-cn.njk
---

# JsonUtil 教程

`JsonUtil` 是 JSON 序列化、解析和对象转换的静态门面。它将工作委托给 `JsonEngine`，调用方无需暴露 Jackson `ObjectMapper`、
`JsonNode` 等实现特定类型。

## 简介

`JsonUtil` 通过当前配置的 JSON 引擎提供 JSON 字符串、Java 对象、Map 和 List 之间的常见转换。

## 主要特性

- 将 Java 对象转换为 JSON 字符串（美化或紧凑格式）
- 将 JSON 字符串解析为 Java 对象
- JSON 与 Map/List 之间的转换
- 类型安全的 JSON 处理
- 延迟初始化并安全发布共享 `JsonEngine`
- 以 `Object` 返回实现无关的 JSON 树模型

## 方法

### 1. 序列化 (转 JSON)

1. `toJson(Object obj)` - 将对象转换为 JSON 字符串
2. `toJsonPretty(Object obj)` - 将对象转换为美化格式的 JSON

### 2. 反序列化 (从 JSON)

1. `fromJson(String jsonStr, Class<T> valueType)` - 将 JSON 解析为指定类型

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

1. `json2Node(String jsonStr)` - 将 JSON 解析为引擎特定的树对象

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

## 引擎配置

首次使用时，`JsonUtil.getEngine()` 会通过 Java `ServiceLoader` 加载 `JsonEngineProvider` 实现，并选择优先级最高的提供者。
工程在可选的 Jackson 依赖存在时提供 Jackson 2 引擎，但门面 API 本身不绑定 Jackson。

应用可在首次转换之前显式设置引擎：

```java
JsonUtil.setEngine(myJsonEngine);
```

`setEngine(null)` 会被拒绝。替换引擎会全局影响后续调用，应在应用启动阶段配置，而不是与普通 JSON 操作并发执行。日期支持、重复键处理、时区行为和具体树模型类型取决于引擎，并非 `JsonUtil` 的统一保证。

## Map、Bean 和树模型示例

```java
Map<String, Object> source = new LinkedHashMap<>();
source.put("name", "Ada");
source.put("age", 36);

String json = JsonUtil.toJson(source);
Map<String, String> strings = JsonUtil.json2StrMap(json);
Map<String, Object> values = JsonUtil.json2map(json);
User user = JsonUtil.map2pojo(values, User.class);
Map<String, Object> userValues = JsonUtil.pojo2map(user);
```

`json2sortMap` 请求 `LinkedHashMap`，以保留解析后的键顺序。`convertValue` 委托给所选引擎的对象转换能力，适合目标类型在运行时才确定的场景。`json2Node` 用于引擎特定的树遍历，只有在确定所选引擎时才应强制转换其结果。

## 生命周期与失败处理

首次创建完成后，引擎会缓存在 volatile 字段中。运行时类加载器中没有可见的 `JsonEngineProvider` 时，`getEngine()` 抛出 `IllegalStateException`。`fromJson` 将解析/映射失败包装为带原始 cause 的 `RuntimeException`；其他方法直接委托，可能暴露提供者特定异常。转换失败时，也不应因此记录不受信任的原始 JSON 内容。

## 实现原理与取舍

`JsonUtil` 将选中的引擎保存于 `volatile` 字段，并通过 synchronized 的双重检查完成延迟初始化。这样启动后的普通调用无需加锁，同时能安全发布已完整创建的引擎。提供者使用 Java 标准 `ServiceLoader` 发现，再按声明的优先级比较；应用无需改动 `JsonUtil` 调用方即可替换 JSON 后端。

门面刻意让 `json2Node` 返回 `Object`：若在此处返回某个库特定的树类型，通用模块将永久依赖该库。代价是处理树模型的代码必须知道当前激活的提供者。

## 结论

`JsonUtil` 类提供了全面的实用方法，用于处理 JSON 数据，使序列化、反序列化以及 JSON 与 Java 对象之间的转换更加容易。
