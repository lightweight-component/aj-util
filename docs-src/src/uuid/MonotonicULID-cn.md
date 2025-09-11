---
title: MonotonicULID 教程
subTitle: 
description: 提供单调递增ULID的实现和使用方法
date: 
tags:
  - UUID
  - ULID
  - 单调递增
layout: layouts/aj-util-cn.njk
---

# MonotonicULID 教程

本教程提供了 `MonotonicULID` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`MonotonicULID` 扩展了标准 ULID 的功能，确保在同一毫秒内生成的标识符单调递增。

## 简介

`MonotonicULID` 类实现了 ULID 规范的单调版本。当在同一毫秒内生成多个 ULID 时，随机组件在最低有效位位置递增 1 位（带进位）。这确保了在同一毫秒内快速连续生成的 ULID 在按词典排序时仍然正确排序。

这个特性对于高吞吐量系统特别有用，在这些系统中，可能在同一毫秒内生成许多标识符，并且维持严格的排序很重要。

## 方法

### 1. `random()`

使用默认安全随机生成器生成单调递增的 ULID。

* **返回值：** 一个新的 ULID 实例，保证比同一毫秒内之前生成的任何 ULID 都大。

**示例：**

```java
ULID ulid1 = MonotonicULID.random();
ULID ulid2 = MonotonicULID.random();
// 如果在同一毫秒内生成，ulid2 在词典排序上将大于 ulid1
```

### 2. 构造函数：`MonotonicULID(Random random)`

使用自定义随机数生成器创建新的 MonotonicULID 生成器。

* **参数：**
  * `random`：要使用的随机数生成器。

**示例：**

```java
MonotonicULID generator = new MonotonicULID(new SecureRandom());
```

### 3. `next()`

从此生成器实例生成下一个单调 ULID。

* **返回值：** 一个新的 ULID 实例。
* **抛出：** 如果在同一毫秒内熵溢出，则抛出 `IllegalStateException`。

**示例：**

```java
MonotonicULID generator = new MonotonicULID(new SecureRandom());
ULID ulid1 = generator.next();
ULID ulid2 = generator.next();
// 如果在同一毫秒内生成，ulid2 将大于 ulid1
```

## 单调性如何工作

单调性通过以下机制实现：

1. 当请求新的 ULID 时，将当前时间戳与上次生成的 ULID 的时间戳进行比较。
2. 如果时间戳不同（新的毫秒），则生成新的随机熵。
3. 如果时间戳相同（同一毫秒），则将上一个 ULID 的熵加 1。
4. 如果增加会导致溢出，则抛出 `IllegalStateException`。

这种方法确保：
- ULID 在同一毫秒内始终递增
- 时间戳组件仍然保持整体基于时间的排序
- 冲突的概率仍然极低

## 使用场景

MonotonicULID 特别适用于：

1. **数据库主键**：确保插入顺序与键顺序匹配
2. **分布式系统**：在多个节点上提供一致的排序
3. **高吞吐量应用**：即使每毫秒生成数千个 ID 也能保持顺序
4. **时间序列数据**：非常适合需要按时间顺序存储的数据

## 与标准 ULID 的比较

虽然标准 ULID 已经可以按时间排序，但 MonotonicULID 增加了一个额外的保证：

- **标准 ULID**：在同一毫秒内生成的两个 ULID 将随机排序（基于它们的随机组件）
- **MonotonicULID**：在同一毫秒内生成的两个 ULID 将按照它们的生成顺序排序

## 结论

`MonotonicULID` 类提供了 ULID 的增强版本，即使在同一毫秒内也能保证严格排序。这使其非常适合高性能系统，在这些系统中，唯一性和精确排序都是关键要求。