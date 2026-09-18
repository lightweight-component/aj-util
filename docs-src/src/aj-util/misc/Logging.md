---
title: Logging utilities
description: MDC trace context, formatted text boxes, and operation-log markers.
tags:
  - logging
  - MDC
layout: layouts/aj-util.njk
---

# Logging utilities

The `com.ajaxjs.util.log` package contains small helpers intended to be used with SLF4J.

## Trace context

`Trace` defines MDC keys `traceId`, `bizAction`, and `ENABLE_OPERATION_LOG`. `saveLogToMDC()` appends content only when `ENABLE_OPERATION_LOG` is already present in MDC.

```java
MDC.put(Trace.ENABLE_OPERATION_LOG, "Start: ");
Trace.saveLogToMDC("created order; ");
String operationLog = MDC.get(Trace.ENABLE_OPERATION_LOG);
MDC.remove(Trace.ENABLE_OPERATION_LOG);
```

MDC is generally thread-local. Propagate or clear it explicitly when work crosses executor threads or request boundaries.

## TextBox

`TextBox` formats diagnostic key-value data using box-drawing characters and ANSI colors.

```java
String output = new TextBox()
        .boxStart("Request")
        .line("id: ", requestId)
        .line("status: ", 200)
        .boxEnd();
```

It wraps long lines by default, counts common CJK characters as double width, and truncates a single value at 1,500 characters. `setWrapLongLines(false)` switches to truncation. ANSI output is appropriate for capable terminals, not for every structured log destination.

## EnableOperationLog

`@EnableOperationLog` is a runtime method annotation with an optional title:

```java
@EnableOperationLog("Create order")
public void createOrder() {
}
```

The annotation is only a marker in this module. An AOP interceptor or other application integration must decide how to process it.

