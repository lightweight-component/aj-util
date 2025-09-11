---
title: ULID 教程
subTitle: 
description: 通用唯一词典排序标识符的实现和使用
date: 
tags:
  - UUID
  - ULID
  - 唯一标识符
layout: layouts/aj-util-cn.njk
---

# ULID 教程

本教程提供了 `ULID` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`ULID`（通用唯一词典排序标识符）提供了一种具有改进功能的 UUID 现代替代方案。

## 简介

`ULID` 类实现了 ULID 规范，该规范提供了与 UUID 的 128 位兼容性，同时通过 Crockford 的 Base32 编码提供了词典排序和更好的可读性。ULID 由以下部分组成：

1. 时间戳组件（48 位）- 毫秒精度
2. 随机性组件（80 位）- 加密安全的随机值

这种组合确保了 ULID：
- 按时间排序（可进行词典排序）
- URL 安全且不区分大小写
- 极不可能发生冲突
- 与 UUID 二进制兼容

## 方法

### 1. `random()` 和 `random(Random random)`

使用当前时间戳和随机熵生成随机 ULID。

* **参数：**
  * `random`（可选）：自定义随机生成器。
* **返回值：** 新的 ULID 实例。

**示例：**

```java
// 使用默认安全随机数
ULID ulid = ULID.random();

// 使用自定义随机生成器
ULID customUlid = ULID.random(new SecureRandom());
```

### 2. `generate(long time, byte[] entropy)`

从特定时间戳和熵字节生成 ULID。

* **参数：**
  * `time`：48 位时间戳（自纪元以来的毫秒数）。
  * `entropy`：80 位（10 字节）随机数据。
* **返回值：** 新的 ULID 实例。
* **抛出：** 如果时间戳无效或熵不是 10 字节，则抛出 `IllegalArgumentException`。

**示例：**

```java
long timestamp = System.currentTimeMillis();
byte[] entropy = new byte[10];
new SecureRandom().nextBytes(entropy);

ULID ulid = ULID.generate(timestamp, entropy);
```

### 3. `fromString(String val)`

从字符串表示解析 ULID。

* **参数：**
  * `val`：Crockford Base32 的 26 个字符的字符串。
* **返回值：** ULID 实例。
* **抛出：** 如果字符串无效，则抛出 `IllegalArgumentException`。

**示例：**

```java
ULID ulid = ULID.fromString("01ARZ3NDEKTSV4RRFFQ69G5FAV");
```

### 4. `fromBytes(byte[] v)`

从二进制表示构造 ULID。

* **参数：**
  * `v`：16 字节二进制数据。
* **返回值：** ULID 实例。
* **抛出：** 如果字节数组长度不是 16，则抛出 `IllegalArgumentException`。

**示例：**

```java
byte[] bytes = new byte[16];
// 用有效的 ULID 数据填充字节
ULID ulid = ULID.fromBytes(bytes);
```

### 5. `fromUUID(UUID val)` 和 `toUUID()`

在 ULID 和 UUID 格式之间转换。

* **参数：**
  * `val`：UUID 实例。
* **返回值：** ULID 实例或 UUID 实例。

**示例：**

```java
// 将 UUID 转换为 ULID
UUID uuid = UUID.randomUUID();
ULID ulid = ULID.fromUUID(uuid);

// 将 ULID 转换为 UUID
UUID convertedUuid = ulid.toUUID();
```

### 6. `toString()`

将 ULID 作为 Crockford 的 Base32 编码字符串（26 个字符）返回。

* **返回值：** 26 个字符的字符串表示。

**示例：**

```java
ULID ulid = ULID.random();
String ulidString = ulid.toString(); // 例如："01F8MECHZCP3WGH3SNAPCH3D5E"
```

### 7. `getTimestamp()` 和 `getEntropy()`

从 ULID 中提取时间戳或熵组件。

* **返回值：** 时间戳（long 类型）或熵（字节数组）。

**示例：**

```java
ULID ulid = ULID.random();
long timestamp = ulid.getTimestamp();
byte[] entropy = ulid.getEntropy();
```

## 与 UUID 的比较

ULID 相比传统的 UUID 有几个优势：

1. **可排序性**：由于其时间戳优先的结构，ULID 可以进行词典排序。
2. **可读性**：Crockford Base32 编码比 UUID 的十六进制格式更易于人类阅读。
3. **时间戳**：ULID 嵌入了创建时间，使其对基于时间的操作很有用。
4. **兼容性**：ULID 保持与 UUID 的二进制兼容性。

## 结论

`ULID` 类提供了一种现代标识符解决方案，它结合了 UUID 的唯一性和改进的可排序性和可读性。它特别适用于分布式系统、数据库和任何需要唯一的、按时间排序的标识符的应用程序。