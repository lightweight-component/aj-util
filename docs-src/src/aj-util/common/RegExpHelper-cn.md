---
title: RegExpHelper
description: 带缓存的面向对象正则表达式匹配。
tags:
  - 正则表达式
  - Java
layout: layouts/aj-util-cn.njk
---

# RegExpHelper

`RegExpHelper` 是当前的正则表达式 API。它持有一个已编译的 `Pattern`，并以对象方式提供匹配操作；`RegExpUtils` 已删除。

## 创建并复用对象

以正则字符串创建对象时，会使用线程安全的共享编译结果缓存。对同一表达式进行多次操作时，应复用同一个对象。

```java
RegExpHelper email = new RegExpHelper("[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}");

boolean found = email.contains("Contact a@ajaxjs.com");
boolean exact = email.fullMatch("a@ajaxjs.com");
```

`contains()` 使用 `Matcher.find()`，检查是否存在匹配；`fullMatch()` 使用 `Matcher.matches()`，要求整个输入都匹配。

## 提取匹配内容

```java
RegExpHelper number = new RegExpHelper("item-(\\d+)");

String first = number.match("item-42", 1);  // "42"
String lastGroup = number.match("item-42", -1); // 最后一个捕获组
String[] all = number.matchAll("item-1,item-2");
```

`match(text)` 返回首个匹配的第 0 组。`match(text, -1)` 返回最后一个捕获组。两者在没有匹配时返回 `null`；非法分组下标沿用 JDK `Matcher` 的异常行为。

也可以传入已编译的 `Pattern`，该 Pattern 会原样使用，不会写入共享字符串正则缓存。

```java
RegExpHelper helper = new RegExpHelper(Pattern.compile("\\d+"));
```

如需兼容式的一次调用，`RegExpHelper.match(regexp, text)` 和 `RegExpHelper.regMatch(regexp, text, groupIndex)` 会委托给对象 API，并使用同一份缓存。

## Matcher 访问与边界行为

需要控制匹配游标时，可通过 `getMatcher(text)` 获取新的 `Matcher`。`Pattern` 不可变，可以共享；`Matcher` 有状态，不应在线程间共享，也不应在改变其状态后当成新的匹配器使用。

```java
RegExpHelper helper = new RegExpHelper("a(b)");
Matcher matcher = helper.getMatcher("zabz");

if (matcher.find()) {
    String whole = matcher.group();  // "ab"
    String captured = matcher.group(1); // "b"
}
```

字符串正则缓存由所有 Helper 和 `getPattern(String)` 共享；以相同文本创建的两个对象会得到同一个 `Pattern` 实例。缓存适合反复使用的固定表达式，不适合无边界的用户输入表达式。非法正则抛出 JDK 的 `PatternSyntaxException`；null 输入遵循 JDK 的空值检查行为。

`matchAll` 返回每个不重叠 `find()` 结果的第 0 组，不返回各捕获组；如需取得所有匹配的分组，应使用 `getMatcher`。

## 实现原理与取舍

相对创建 `Matcher` 而言，编译正则表达式成本较高。`RegExpHelper` 因此用 `ConcurrentHashMap` 保存 Pattern，并通过 `computeIfAbsent` 获取：并发调用方请求同一表达式时，可复用已编译的 `Pattern`，而无需外部加锁。缓存刻意只保存字符串创建的 Pattern；调用方传入的 `Pattern` 完全由调用方控制。

每个匹配方法都会创建新的 Matcher，因为 `Matcher` 保存输入和游标状态。这样即便底层编译 Pattern 被共享，普通 Helper 操作之间仍然相互独立。
