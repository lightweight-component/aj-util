---
title: RandomTools 教程
description: RandomTools 类提供了生成随机数和随机字符串的实用方法
tags:
  - 随机数
  - 工具类
  - Java
layout: layouts/aj-util-cn.njk
---
# RandomTools 使用教程

RandomTools 是一个用于生成各种类型随机值的工具类，包括数字、字符串和 UUID（支持版本 7）。

### 主要功能特性

1. **线程安全的随机数生成**：使用 `ThreadLocalRandom` 提供高性能
2. **加密强度的随机数生成**：使用 `SecureRandom` 提供加密级别的安全性
3. **多种随机值生成**：支持数字、字符串、UUIDv7 等
4. **时间有序 UUID**：UUIDv7 提供比随机 UUID 更好的局部性

### 数字随机数生成

#### 1. 生成六位数字
```java
// 生成六位随机数字（100000-999999）
int number = RandomTools.generateNumber();
System.out.println(number); // 例如：456789
```


#### 2. 生成指定位数的数字
```java
// 生成指定位数的随机数字
int threeDigit = RandomTools.generateNumber(3); // 100-999
int fourDigit = RandomTools.generateNumber(4); // 1000-9999

System.out.println(threeDigit); // 例如：567
System.out.println(fourDigit);  // 例如：3456
```


### 字符串随机数生成

#### 1. 生成六位随机字符串
```java
// 生成六位字母数字随机字符串
String randomStr = RandomTools.generateRandomString();
System.out.println(randomStr); // 例如：aB3xY9
```


#### 2. 生成指定长度的随机字符串
```java
// 生成指定长度的随机字符串
String shortStr = RandomTools.generateRandomString(3);
String longStr = RandomTools.generateRandomString(10);

System.out.println(shortStr); // 例如：Xy2
System.out.println(longStr);  // 例如：AbC3dE5fGh
```


### UUID 生成

#### 1. 生成 UUIDv7
```java
// 生成 UUID 版本 7（时间有序）
UUID uuid = RandomTools.uuid();
System.out.println(uuid); // 例如：550e8400-e29b-41d4-a716-446655440000
```


#### 2. 生成无连字符的 UUIDv7 字符串
```java
// 生成无连字符的 UUID 字符串
String uuidStr = RandomTools.uuidStr();
System.out.println(uuidStr); // 例如：550e8400e29b41d4a716446655440000
```


#### 3. 查看 UUID 的时间戳
```java
// 获取 UUIDv7 中的时间戳信息
String uuidString = RandomTools.uuidStr();
Date timestamp = RandomTools.showTime(uuidString);
System.out.println(timestamp); // 例如：Wed Oct 25 14:30:45 CST 2023
```


### 常量说明

- `RANDOM`: `SecureRandom` 实例，用于加密强度的随机数生成
- `SIMPLE_RANDOM`: `Random` 实例，用于基本的随机数生成
- `STR`: 字符池，包含大小写字母和数字（a-z, A-Z, 0-9）

### 注意事项

1. 所有生成方法都是静态的，可以直接通过类名调用
2. 数字生成使用 `ThreadLocalRandom` 保证线程安全和性能
3. UUIDv7 是时间有序的，具有更好的数据库索引性能
4. 字符串生成使用固定的字母数字字符池

---
