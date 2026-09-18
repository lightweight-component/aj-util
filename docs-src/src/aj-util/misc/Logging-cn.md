---
title: 日志工具
description: MDC 链路上下文、格式化文本框和操作日志标记。
tags:
  - 日志
  - MDC
layout: layouts/aj-util-cn.njk
---

# 日志工具

`com.ajaxjs.util.log` 包提供与 SLF4J 配合使用的小型辅助工具。

## Trace 上下文

`Trace` 定义了 `traceId`、`bizAction` 和 `ENABLE_OPERATION_LOG` 等 MDC 键。仅当 MDC 已存在 `ENABLE_OPERATION_LOG` 时，`saveLogToMDC()` 才会追加内容。

```java
MDC.put(Trace.ENABLE_OPERATION_LOG, "Start: ");
Trace.saveLogToMDC("created order; ");
String operationLog = MDC.get(Trace.ENABLE_OPERATION_LOG);
MDC.remove(Trace.ENABLE_OPERATION_LOG);
```

MDC 通常是线程本地上下文。任务跨线程池或请求边界时，需要自行传播或清理。

## TextBox

`TextBox` 使用制表框字符和 ANSI 颜色格式化诊断用键值数据。

```java
String output = new TextBox()
        .boxStart("Request")
        .line("id: ", requestId)
        .line("status: ", 200)
        .boxEnd();
```

它默认换行长文本、将常见 CJK 字符按双宽度计算，并将单个值截断到 1,500 个字符。`setWrapLongLines(false)` 改为截断模式。ANSI 输出适合支持它的终端，并不适用于所有结构化日志目标。

## EnableOperationLog

`@EnableOperationLog` 是带可选标题的运行时方法注解：

```java
@EnableOperationLog("创建订单")
public void createOrder() {
}
```

该注解在本模块中仅是标记。具体如何记录操作日志，需要由应用的 AOP 拦截器或其他集成代码决定。

