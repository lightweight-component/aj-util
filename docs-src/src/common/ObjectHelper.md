---
title: ObjectHelper Tutorial
description: Provides utility methods for working with Java objects
date: 2025-09-11
tags:
  - object
  - utilities
  - cloning
layout: layouts/aj-util.njk
---

# ObjectHelper Tutorial

This tutorial provides an overview of the `ObjectHelper` class, which is part of the `lightweight-component/aj-util` library. The `ObjectHelper` class provides utility methods for working with Java objects.

## Introduction

The `ObjectHelper` class contains static methods for object cloning and map creation utilities.

## Main Features

- Deep cloning of Serializable objects
- Convenient map creation methods
- Optimized map initialization
- Empty map constant

## Constants

1. `EMPTY_PARAMS_MAP` - Immutable empty map

## Methods

### 1. Cloning

1. `clone(T obj)` - Deep clone a Serializable object

### 2. Map Creation

1. `mapOf(K k1, V v1)` - Create map with single entry
2. `mapOf(K k1, V v1, K k2, V v2)` - Create map with two entries
3. `mapOf(K k1, V v1, K k2, V v2, K k3, V v3)` - Create map with three entries
4. `mapOf(int expectedSize)` - Create optimized HashMap with expected size

## Usage Examples

### Object Cloning
```java
MyClass original = new MyClass();
MyClass cloned = ObjectHelper.clone(original);
```

### Map Creation
```java
Map<String, Integer> map1 = ObjectHelper.mapOf("a", 1);
Map<String, Integer> map2 = ObjectHelper.mapOf("a", 1, "b", 2);
Map<String, Integer> map3 = ObjectHelper.mapOf("a", 1, "b", 2, "c", 3);

// Optimized map
Map<String, Integer> map = ObjectHelper.mapOf(100); // Pre-sized for ~100 entries
```

### Empty Map
```java
Map<String, Object> empty = ObjectHelper.EMPTY_PARAMS_MAP;
```

## Implementation Notes

- `clone()` uses Java serialization for deep cloning
- `mapOf()` methods provide convenient map initialization
- `mapOf(int)` optimizes HashMap creation by calculating ideal initial capacity

## Conclusion

The `ObjectHelper` class provides useful utilities for object cloning and map creation, simplifying common object-related operations in Java applications.