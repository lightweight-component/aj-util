---
title: CollUtils 教程
description: 为 Java 应用程序中的集合和数组操作提供实用方法
tags:
  - 集合
  - 数组
  - Java
layout: layouts/aj-util-cn.njk
---

# CollUtils 教程

本教程提供了 `CollUtils` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`CollUtils` 类为 Java 应用程序中的集合和数组操作提供了实用工具方法。

## 简介

`CollUtils` 类包含用于常见集合和数组操作的静态方法，包括空检查、转换、合并和过滤。

## 主要特性

- 数组、集合和映射的空/null 检查
- 安全的集合访问
- 数组合并和连接
- 集合/数组转换
- 过滤和搜索集合
- 专门的整型数组处理

## 方法

### 1. 空检查

1. `isEmpty(Object[] array)` - 检查数组是否为 null 或空
2. `isEmpty(Collection<?> collection)` - 检查集合是否为 null 或空
3. `isEmpty(Map<?, ?> map)` - 检查映射是否为 null 或空

### 2. 安全访问

1. `getList(List<T> list)` - 如果输入为 null 则返回空列表

### 3. 数组操作

1. `printArray(Object[] arr)` - 打印数组内容用于调试
2. `concat(T[] first, T[] second)` - 连接两个数组
3. `addAll(T[]... arrays)` - 合并多个数组
4. `newArray(Class<?> componentType, int newSize)` - 创建新的空数组

### 4. 转换

1. `arrayList(E... elements)` - 将数组转换为 ArrayList
2. `intList2Arr(List<Integer> list)` - 将 Integer 列表转换为 int[]
3. `stringArr2intArr(String value)` - 将逗号分隔的字符串转换为 int[]

### 5. 过滤/搜索

1. `findOne(List<T> list, Predicate<T> filter)` - 查找第一个匹配元素
2. `findSome(List<T> list, Predicate<T> filter)` - 查找所有匹配元素

## 使用示例

### 空检查
```java
String[] arr = null;
boolean empty = CollUtils.isEmpty(arr); // true

List<String> list = new ArrayList<>();
empty = CollUtils.isEmpty(list); // true
```

### 安全访问
```java
List<String> list = null;
List<String> safeList = CollUtils.getList(list); // 返回空列表
```

### 数组操作
```java
String[] a = {"a", "b"};
String[] b = {"c", "d"};
String[] combined = CollUtils.concat(a, b); // ["a", "b", "c", "d"]
```

### 转换
```java
List<String> list = CollUtils.arrayList("a", "b", "c");

List<Integer> intList = Arrays.asList(1, 2, 3);
int[] intArr = CollUtils.intList2Arr(intList); // [1, 2, 3]

int[] nums = CollUtils.stringArr2intArr("1,2,3"); // [1, 2, 3]
```

### 过滤
```java
List<String> names = Arrays.asList("John", "Jane", "Doe");
String result = CollUtils.findOne(names, n -> n.startsWith("J")); // "John"
List<String> allJ = CollUtils.findSome(names, n -> n.startsWith("J")); // ["John", "Jane"]
```

## 结论

`CollUtils` 类提供了全面的实用方法，用于处理集合和数组，使 Java 应用程序中的常见操作更加方便和安全。