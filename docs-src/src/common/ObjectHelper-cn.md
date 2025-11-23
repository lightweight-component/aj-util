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
# ObjectHelper 使用教程

`ObjectHelper` 是一个对象工具类，提供了处理 Java 对象、集合、映射和各种数据结构的通用工具方法。

### 主要功能特性

1. **文本内容检查和验证**
2. **集合和数组空值检查**
3. **创建集合的便捷工厂方法（映射、列表、集合）**
4. **容量计算工具**

### 文本检查方法

#### 1. hasText 方法
检查给定字符串是否有实际文本内容：

```java
// 检查字符串是否包含非空白字符
boolean result1 = ObjectHelper.hasText("Hello");     // true
boolean result2 = ObjectHelper.hasText("   ");       // false
boolean result3 = ObjectHelper.hasText("");          // false
boolean result4 = ObjectHelper.hasText(null);        // false
```


#### 2. isEmptyText 方法
检查给定字符串是否为空或仅包含空白字符：

```java
// 检查字符串是否为空或仅包含空白字符
boolean result1 = ObjectHelper.isEmptyText("Hello"); // false
boolean result2 = ObjectHelper.isEmptyText("   ");   // true
boolean result3 = ObjectHelper.isEmptyText("");      // true
boolean result4 = ObjectHelper.isEmptyText(null);    // true
```


### 空值检查方法

#### 1. 数组空值检查
```java
// 检查数组是否为空
String[] array1 = null;
String[] array2 = new String[0];
String[] array3 = {"Hello"};

boolean result1 = ObjectHelper.isEmpty(array1); // true
boolean result2 = ObjectHelper.isEmpty(array2); // true
boolean result3 = ObjectHelper.isEmpty(array3); // false
```


#### 2. 集合空值检查
```java
// 检查集合是否为空
List<String> list1 = null;
List<String> list2 = new ArrayList<>();
List<String> list3 = Arrays.asList("Hello");

boolean result1 = ObjectHelper.isEmpty(list1); // true
boolean result2 = ObjectHelper.isEmpty(list2); // true
boolean result3 = ObjectHelper.isEmpty(list3); // false
```


#### 3. 映射空值检查
```java
// 检查映射是否为空
Map<String, String> map1 = null;
Map<String, String> map2 = new HashMap<>();
Map<String, String> map3 = new HashMap<>();
map3.put("key", "value");

boolean result1 = ObjectHelper.isEmpty(map1); // true
boolean result2 = ObjectHelper.isEmpty(map2); // true
boolean result3 = ObjectHelper.isEmpty(map3); // false
```


### 集合创建方法

#### 1. 创建映射
```java
// 创建单键值对映射
Map<String, String> map1 = ObjectHelper.mapOf("key1", "value1");

// 创建双键值对映射
Map<String, String> map2 = ObjectHelper.mapOf("key1", "value1", "key2", "value2");

// 创建三键值对映射
Map<String, String> map3 = ObjectHelper.mapOf("key1", "value1", "key2", "value2", "key3", "value3");

// 创建指定容量的映射
Map<String, String> map4 = ObjectHelper.mapOf(10); // 优化容量的HashMap
```


#### 2. 创建列表
```java
// 创建不可变列表
List<String> list1 = ObjectHelper.listOf("item1");
List<String> list2 = ObjectHelper.listOf("item1", "item2", "item3");
List<String> list3 = ObjectHelper.listOf(); // 空列表
```


#### 3. 创建集合
```java
// 创建不可变集合
Set<String> set1 = ObjectHelper.setOf("item1");
Set<String> set2 = ObjectHelper.setOf("item1", "item2", "item3");
Set<String> set3 = ObjectHelper.setOf(); // 空集合

// 自动去重
Set<String> set4 = ObjectHelper.setOf("item1", "item1", "item2"); // 只包含"item1"和"item2"
```


### 常量

- `EMPTY_PARAMS_MAP`: 空的不可变映射，当不需要参数时可用作默认值或标记值
- `DEFAULT_LOAD_FACTOR`: 哈希映射的默认负载因子，针对时间和空间效率进行了优化

### 注意事项

1. `listOf` 和 `setOf` 方法创建的是不可变集合
2. `setOf` 方法不允许包含 null 元素
3. `mapOf` 方法会根据预期大小优化初始容量，减少重新调整大小的操作