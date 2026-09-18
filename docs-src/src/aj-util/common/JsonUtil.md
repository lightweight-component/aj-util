---
title: JsonUtil
description: JSON conversion through a pluggable JSON engine
tags:
  - JSON
  - serialization
  - Java
layout: layouts/aj-util.njk
---

# JsonUtil Tutorial

`JsonUtil` is a static facade for JSON serialization, parsing, and object conversion. It delegates work to a
`JsonEngine`, so callers do not need to expose implementation-specific types such as Jackson `ObjectMapper` or
`JsonNode`.

## Introduction

`JsonUtil` provides common JSON operations between JSON strings, Java objects, Maps, and Lists through the configured
JSON engine.

## Main Features

- Convert Java objects to JSON strings (pretty or compact)
- Parse JSON strings into Java objects
- Convert between JSON and Maps/Lists
- Type-safe JSON processing
- A lazily initialized, safely published shared `JsonEngine`
- An implementation-independent JSON tree returned as `Object`

## Methods

### 1. Serialization (to JSON)

1. `toJson(Object obj)` - Convert object to JSON string
2. `toJsonPretty(Object obj)` - Convert object to pretty-printed JSON

### 2. Deserialization (from JSON)

1. `fromJson(String jsonStr, Class<T> valueType)` - Parse JSON to specified type

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

1. `json2Node(String jsonStr)` - Parse JSON to an engine-specific tree object

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

## Engine configuration

On first use, `JsonUtil.getEngine()` loads `JsonEngineProvider` implementations through Java `ServiceLoader` and
selects the provider with the highest priority. The project provides a Jackson 2 engine when its optional Jackson
dependencies are present, but the facade itself is not tied to Jackson.

An application may set an engine explicitly before its first conversion:

```java
JsonUtil.setEngine(myJsonEngine);
```

`setEngine(null)` is rejected. Replacing the engine changes subsequent calls globally, so configure it during
application startup rather than concurrently with ordinary JSON work. Date support, duplicate-key handling, time zone
behavior, and the concrete tree-model type are engine-specific rather than guarantees of `JsonUtil`.

## Map, bean, and tree examples

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

`json2sortMap` requests a `LinkedHashMap` so parsed key order is retained. `convertValue` delegates to the selected
engine's object-conversion facility and is useful when the desired target type is known at runtime. `json2Node` is for
engine-specific tree traversal; cast its result only when the chosen engine is known.

## Lifecycle and failures

The first engine is cached in a volatile field after lazy creation. If no `JsonEngineProvider` is visible to the
runtime class loader, `getEngine()` throws `IllegalStateException`. `fromJson` wraps parser/mapper failures in a
`RuntimeException` with the original cause. Other methods delegate directly and may expose provider-specific
exceptions. Do not log untrusted raw JSON merely because a conversion failed.

## Implementation notes

`JsonUtil` keeps the selected engine in a `volatile` field and uses synchronized double-checked initialization. This
means normal calls avoid locking after startup while still publishing a fully created engine safely. Providers are
discovered through Java's standard `ServiceLoader`, then compared by their declared priority; this lets applications
replace the JSON backend without changing `JsonUtil` callers.

The facade deliberately exposes `Object` for `json2Node`: returning a library-specific tree type here would make the
common module permanently depend on that library. The trade-off is that tree processing code must know which provider
is active.

## Conclusion

The `JsonUtil` class provides comprehensive utility methods for working with JSON data, making it easier to serialize,
deserialize, and convert between JSON and Java objects.
