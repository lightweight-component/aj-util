---
title: JsonUtil Tutorial
description: Utility methods for working with JSON data in Java applications
tags:
  - JSON
  - serialization
  - Java
layout: layouts/aj-util.njk
---

# JsonUtil Tutorial

This tutorial provides an overview of the `JsonUtil` class, which is part of the `lightweight-component/aj-util` library. The `JsonUtil` class provides utility methods for working with JSON data in Java applications.

## Introduction

The `JsonUtil` class contains static methods for common JSON operations using Jackson library, including conversion between JSON strings, Java objects, Maps, and Lists.

## Main Features

- Convert Java objects to JSON strings (pretty or compact)
- Parse JSON strings into Java objects
- Convert between JSON and Maps/Lists
- Type-safe JSON processing
- Java 8 date/time support
- Strict duplicate detection

## Methods

### 1. Serialization (to JSON)

1. `toJson(Object obj)` - Convert object to JSON string
2. `toJsonPretty(Object obj)` - Convert object to pretty-printed JSON

### 2. Deserialization (from JSON)

1. `fromJson(String jsonStr, Class<T> valueType)` - Parse JSON to specified type
2. `fromJson(String jsonStr, JavaType valueType)` - Parse JSON with complex type

### 3. JSON ↔ Map Conversion

1. `json2map(String jsonStr)` - JSON to Map<String, Object>
2. `json2map(String jsonStr, Class<T> clazz)` - JSON to Map with typed values
3. `json2sortMap(String jsonStr, Class<T> clazz)` - JSON to LinkedHashMap preserving order
4. `json2StrMap(String jsonStr)` - JSON to Map<String, String>

### 4. JSON ↔ List Conversion

1. `json2list(String jsonArrayStr, Class<T> clazz)` - JSON array to List<T>
2. `json2mapList(String jsonArrayStr)` - JSON array to List<Map>

### 5. Object Conversion

1. `convertValue(Object obj, Class<T> clazz)` - Convert object types
2. `pojo2map(Object obj)` - Convert POJO to Map
3. `pojo2map(Object obj, Class<T> clazz)` - Convert POJO to typed Map
4. `map2pojo(Map<String, Object> map, Class<T> clazz)` - Convert Map to POJO

### 6. JSON Tree Model

1. `json2Node(String jsonStr)` - Parse JSON to JsonNode tree

## Usage Examples

### Basic Serialization
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

### Basic Deserialization
```java
String json = "{\"name\":\"John\",\"age\":30}";
User user = JsonUtil.fromJson(json, User.class);
```

### Map Conversion
```java
Map<String, Object> map = new HashMap<>();
map.put("name", "John");
map.put("age", 30);

String json = JsonUtil.toJson(map);
Map<String, Object> map2 = JsonUtil.json2map(json);
```

### List Conversion
```java
String jsonArray = "[{\"name\":\"John\"}, {\"name\":\"Alice\"}]";
List<User> users = JsonUtil.json2list(jsonArray, User.class);
```

## Configuration

The `JsonUtil` class is pre-configured with:
- Java 8 date/time support
- Strict duplicate detection
- Asia/Shanghai timezone
- Default ObjectMapper instance

## Conclusion

The `JsonUtil` class provides comprehensive utility methods for working with JSON data, making it easier to serialize, deserialize, and convert between JSON and Java objects.