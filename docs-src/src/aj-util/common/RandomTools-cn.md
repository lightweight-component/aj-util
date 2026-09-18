---
title: RandomTools
description: 随机数字、字符串、安全随机字节与 UUID v7 字符串。
tags:
  - 随机数
  - UUIDv7
layout: layouts/aj-util-cn.njk
---

# RandomTools

`RandomTools` 用于生成普通随机数字和字符串，暴露共享 `SecureRandom`，并生成 UUID v7 字符串。

```java
int code = RandomTools.generateNumber();          // 六位数字，100000–999999
String text = RandomTools.generateRandomString(8); // 八位字母数字

String compactId = RandomTools.uuidV7();          // 32 位小写十六进制字符
String standardId = RandomTools.uuidV7(true);     // 8-4-4-4-12 格式
Date createdAt = RandomTools.showTime(compactId);
```

`generateNumber(int)` 只接受 1 到 9 位，`generateRandomString(int)` 要求长度为正。二者均使用 `ThreadLocalRandom`，不能用于密码、验证码、令牌或密码学密钥。

`uuidV7` 使用 `RandomTools.RANDOM`（`SecureRandom`）生成随机部分，并写入当前的 epoch 毫秒时间。UUIDv7 按该时间戳有序，但同一毫秒内或系统时钟回拨时不保证单调递增。它是标识符，不是秘密令牌。`showTime` 接受带连字符或 32 字符的 UUIDv7，其他 UUID 版本会被拒绝。

需要密码学强度的随机字节时，应使用 `RandomTools.RANDOM` 或 `DoCipher.randomBytes()`。

## UUIDv7 格式与校验

UUIDv7 包含 48 位 Unix 毫秒时间戳、版本位、RFC UUID 变体位和 74 位随机数据。默认的 `uuidV7()` 等同于 `uuidV7(false)`，返回 32 位小写紧凑格式。与要求 `UUID.toString()` 格式的 API 互通时，应选择带连字符形式。

```java
String id = RandomTools.uuidV7(true);
Date timestamp = RandomTools.showTime(id);

// showTime 接受两种形式：
Date sameTimestamp = RandomTools.showTime(id.replace("-", ""));
```

`showTime` 不会裁剪空白；输入格式错误或 UUID 不是版本 7 时抛出 `IllegalArgumentException`。返回的 `Date` 是标识符中嵌入的时间戳，不能独立证明标识符实际创建时间。

## 随机性选择

| API | 随机源 | 适用场景 |
| --- | --- | --- |
| `generateNumber`、`generateRandomString` | `ThreadLocalRandom` | UI 样例、非安全标识 |
| `uuidV7` | 随机部分使用共享 `SecureRandom` | 一般唯一标识 |
| `RandomTools.RANDOM` / `DoCipher.randomBytes` | `SecureRandom` | nonce、盐值、密钥材料 |

这些 API 都不维护唯一性注册表。应用需要唯一性时，应自行存储并约束。

## 实现原理与取舍

数字和字母数字字符串方法使用 `ThreadLocalRandom`，避免普通并发应用中竞争共享伪随机生成器。UUIDv7 则采用不同路径：先通过共享 `SecureRandom` 填充 16 字节，再按 UUIDv7 布局覆盖时间戳、版本和变体位置，最后借助 `UUID` 格式化。时间戳提取方法会从 UUID 的最高有效位按该布局反向读取。

这一设计提供了可排序标识和强随机部分，但不会协调同一毫秒内的调用。若数据库需要严格排序，应额外保存序列或排序字段。
