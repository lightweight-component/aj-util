---
title: Version Tutorial
description: Provides environment detection and configuration utilities
date: 2025-09-11
tags:
  - environment
  - configuration
  - version
layout: layouts/aj-util.njk
---

# Version Tutorial

This tutorial provides an overview of the `Version` class, which is part of the `lightweight-component/aj-util` library. The `Version` class provides environment detection and configuration utilities.

## Introduction

The `Version` class contains static methods and fields for detecting runtime environment characteristics and configuring default settings.

## Main Features

- Operating system detection
- Debug mode detection
- Timezone configuration
- Unit test detection

## Fields

1. `OS_NAME` - Operating system name (lowercase)
2. `isDebug` - Flag indicating debug/development mode
3. `isRunningTest` - Cache for unit test detection

## Methods

### 1. `isRunningTest()`

Detects if the code is running in a JUnit test environment by checking the stack trace for JUnit runners.

* **Returns:** true if running in a test environment

## Static Initialization

The class automatically:
1. Checks and sets default timezone to Asia/Shanghai if not already set
2. Determines debug mode based on OS (non-Linux = debug mode)

## Usage Examples

### Check Debug Mode
```java
if (Version.isDebug) {
    // Development environment specific code
}
```

### Detect Test Environment
```java
if (Version.isRunningTest()) {
    // Test-specific setup
}
```

### Get OS Info
```java
String os = Version.OS_NAME; // "windows 10", "linux", etc.
```

## Conclusion

The `Version` class provides simple utilities for detecting runtime environment characteristics and configuring default settings like timezone.