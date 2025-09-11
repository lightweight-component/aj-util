---
title: CollUtils Tutorial
description: Utility methods for working with collections and arrays in Java applications
tags:
  - collections
  - arrays
  - Java
layout: layouts/aj-util.njk
---

# CollUtils Tutorial

This tutorial provides an overview of the `CollUtils` class, which is part of the `lightweight-component/aj-util` library. The `CollUtils` class provides utility methods for working with collections and arrays in Java applications.

## Introduction

The `CollUtils` class contains static methods for common collection and array operations, including empty checks, conversions, merging, and filtering.

## Main Features

- Empty/null checks for arrays, collections and maps
- Safe collection access
- Array merging and concatenation
- Collection/array conversions
- Filtering and searching collections
- Specialized int array handling

## Methods

### 1. Empty Checks

1. `isEmpty(Object[] array)` - Check if array is null or empty
2. `isEmpty(Collection<?> collection)` - Check if collection is null or empty
3. `isEmpty(Map<?, ?> map)` - Check if map is null or empty

### 2. Safe Access

1. `getList(List<T> list)` - Returns empty list if input is null

### 3. Array Operations

1. `printArray(Object[] arr)` - Print array contents for debugging
2. `concat(T[] first, T[] second)` - Concatenate two arrays
3. `addAll(T[]... arrays)` - Merge multiple arrays
4. `newArray(Class<?> componentType, int newSize)` - Create new empty array

### 4. Conversions

1. `arrayList(E... elements)` - Convert array to ArrayList
2. `intList2Arr(List<Integer> list)` - Convert Integer list to int[]
3. `stringArr2intArr(String value)` - Convert comma-separated string to int[]

### 5. Filtering/Searching

1. `findOne(List<T> list, Predicate<T> filter)` - Find first matching element
2. `findSome(List<T> list, Predicate<T> filter)` - Find all matching elements

## Usage Examples

### Empty Checks
```java
String[] arr = null;
boolean empty = CollUtils.isEmpty(arr); // true

List<String> list = new ArrayList<>();
empty = CollUtils.isEmpty(list); // true
```

### Safe Access
```java
List<String> list = null;
List<String> safeList = CollUtils.getList(list); // returns empty list
```

### Array Operations
```java
String[] a = {"a", "b"};
String[] b = {"c", "d"};
String[] combined = CollUtils.concat(a, b); // ["a", "b", "c", "d"]
```

### Conversions
```java
List<String> list = CollUtils.arrayList("a", "b", "c");

List<Integer> intList = Arrays.asList(1, 2, 3);
int[] intArr = CollUtils.intList2Arr(intList); // [1, 2, 3]

int[] nums = CollUtils.stringArr2intArr("1,2,3"); // [1, 2, 3]
```

### Filtering
```java
List<String> names = Arrays.asList("John", "Jane", "Doe");
String result = CollUtils.findOne(names, n -> n.startsWith("J")); // "John"
List<String> allJ = CollUtils.findSome(names, n -> n.startsWith("J")); // ["John", "Jane"]
```

## Conclusion

The `CollUtils` class provides comprehensive utility methods for working with collections and arrays, making common operations more convenient and safer in Java applications.