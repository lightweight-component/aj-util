---
title: ObjectHelper 教程
description: 为处理 Java 对象提供了实用工具方法
date: 2025-09-11
tags:
  - 对象
  - 工具
  - 克隆
layout: layouts/aj-util-cn.njk
---

# ObjectHelper 教程

本教程提供了 `ObjectHelper` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`ObjectHelper` 类为处理 Java 对象提供了实用工具方法。

## 简介

`ObjectHelper` 类包含用于对象克隆和映射创建的静态方法。

## 主要特性

- 可序列化对象的深度克隆
- 方便的映射创建方法
- 优化的映射初始化
- 空映射常量

## 常量

1. `EMPTY_PARAMS_MAP` - 不可变的空映射

## 方法

### 1. 克隆

1. `clone(T obj)` - 深度克隆可序列化对象

### 2. 映射创建

1. `mapOf(K k1, V v1)` - 创建包含单个条目的映射
2. `mapOf(K k1, V v1, K k2, V v2)` - 创建包含两个条目的映射
3. `mapOf(K k1, V v1, K k2, V v2, K k3, V v3)` - 创建包含三个条目的映射
4. `mapOf(int expectedSize)` - 创建具有预期大小的优化 HashMap

## 使用示例

### 对象克隆
```java
MyClass original = new MyClass();
MyClass cloned = ObjectHelper.clone(original);
```

### 映射创建
```java
Map<String, Integer> map1 = ObjectHelper.mapOf("a", 1);
Map<String, Integer> map2 = ObjectHelper.mapOf("a", 1, "b", 2);
Map<String, Integer> map3 = ObjectHelper.mapOf("a", 1, "b", 2, "c", 3);

// 优化映射
Map<String, Integer> map = ObjectHelper.mapOf(100); // 预分配约100个条目的空间
```

### 空映射
```java
Map<String, Object> empty = ObjectHelper.EMPTY_PARAMS_MAP;
```

## 实现说明

- `clone()` 使用 Java 序列化进行深度克隆
- `mapOf()` 方法提供了方便的映射初始化
- `mapOf(int)` 通过计算理想的初始容量来优化 HashMap 创建

## 结论

`ObjectHelper` 类提供了有用的工具，用于对象克隆和映射创建，简化了 Java 应用程序中常见的对象相关操作。