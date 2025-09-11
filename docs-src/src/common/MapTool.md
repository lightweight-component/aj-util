---
title: MapTool Tutorial
description: Utility methods for working with Map data structures
tags:
  - Map utilities
  - XML conversion
  - data structures
layout: layouts/aj-util.njk
---

# MapTool Tutorial

This tutorial provides an overview of the `MapTool` class, which is part of the `lightweight-component/aj-util` library. The `MapTool` class provides utility methods for working with Map data structures in Java applications.

## Introduction

The `MapTool` class contains static methods for common Map operations such as conversion, joining, and XML serialization/deserialization.

## Main Features

- Join Map entries into strings with various delimiters
- Convert between different Map types and formats
- Convert between Maps and XML
- Deep copy Maps
- Helper methods for Map value processing

## Methods

### 1. `join()` Methods

Four overloaded methods for joining Map entries into strings:

1. `join(Map<String, T> map, String div, Function<T, String> fn)` - Join with custom delimiter and value processor
2. `join(Map<String, T> map, Function<T, String> fn)` - Join with default delimiter (&) and custom value processor
3. `join(Map<String, T> map, String div)` - Join with custom delimiter and default toString() value processor
4. `join(Map<String, T> map)` - Join with default delimiter (&) and default toString() value processor

### 2. `toMap()` Methods

Two methods for converting to Maps:

1. `toMap(String[] pairs, Function<String, Object> fn)` - Convert array of key=value strings to Map
2. `toMap(String[] columns, String[] values, Function<String, Object> fn)` - Convert parallel key and value arrays to Map

### 3. `getValue()`

`getValue(Map<String, T> map, String key, Consumer<T> s)` - Safely get and process a Map value if present

### 4. `as()` Methods

Two methods for Map conversion:

1. `as(Map<String, K> map, Function<K, T> fn)` - Convert Map values using a function
2. `as(Map<String, String[]> map)` - Convert Map with String[] values to Map<String, Object>

### 5. `deepCopy()`

`deepCopy(Map<T, K> map)` - Create a deep copy of a Map

### 6. XML Conversion Methods

1. `beanToXml(Object bean)` - Convert Java bean to XML string
2. `mapToXml(Map<String, ?> data)` - Convert Map to XML string
3. `xmlToMap(String strXML)` - Convert XML string to Map

## Usage Examples

### Joining Map Entries
```java
Map<String, String> map = new HashMap<>();
map.put("name", "John");
map.put("age", "30");

String joined = MapTool.join(map); // "name=John&age=30"
```

### Converting to Map
```java
String[] pairs = {"name=John", "age=30"};
Map<String, Object> map = MapTool.toMap(pairs, Integer::parseInt);
```

### XML Conversion
```java
Map<String, String> data = new HashMap<>();
data.put("name", "John");
data.put("age", "30");

String xml = MapTool.mapToXml(data); 
// <xml><name>John</name><age>30</age></xml>

Map<String, String> map = MapTool.xmlToMap(xml);
```

## Conclusion

The `MapTool` class provides comprehensive utility methods for working with Map data structures, including joining, conversion, and XML serialization/deserialization.