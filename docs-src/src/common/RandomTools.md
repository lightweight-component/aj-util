---
title: RandomTools Tutorial
description: Utility methods for generating random numbers and strings
tags:
  - random
  - utilities
  - Java
layout: layouts/aj-util.njk
---

# RandomTools Tutorial

`RandomTools` is a utility class for generating various types of random values, including numbers, strings, and UUIDs (with version 7 support).

### Main Features

1. **Thread-safe random generation**: Uses `ThreadLocalRandom` for high performance
2. **Cryptographic-strength randomness**: Uses `SecureRandom` for security-level randomness
3. **Multiple random value generation**: Supports numbers, strings, UUIDv7, etc.
4. **Time-ordered UUIDs**: UUIDv7 provides better locality than random UUIDs

### Number Random Generation

#### 1. Generate Six-Digit Numbers
```java
// Generate a six-digit random number (100000-999999)
int number = RandomTools.generateNumber();
System.out.println(number); // e.g.: 456789
```


#### 2. Generate Numbers with Specified Digits
```java
// Generate random numbers with specified digit count
int threeDigit = RandomTools.generateNumber(3); // 100-999
int fourDigit = RandomTools.generateNumber(4); // 1000-9999

System.out.println(threeDigit); // e.g.: 567
System.out.println(fourDigit);  // e.g.: 3456
```


### String Random Generation

#### 1. Generate Six-Character Random String
```java
// Generate a six-character alphanumeric random string
String randomStr = RandomTools.generateRandomString();
System.out.println(randomStr); // e.g.: aB3xY9
```


#### 2. Generate Random String with Specified Length
```java
// Generate random strings with specified length
String shortStr = RandomTools.generateRandomString(3);
String longStr = RandomTools.generateRandomString(10);

System.out.println(shortStr); // e.g.: Xy2
System.out.println(longStr);  // e.g.: AbC3dE5fGh
```


### UUID Generation

#### 1. Generate UUIDv7
```java
// Generate UUID version 7 (time-ordered)
UUID uuid = RandomTools.uuid();
System.out.println(uuid); // e.g.: 550e8400-e29b-41d4-a716-446655440000
```


#### 2. Generate UUIDv7 String Without Hyphens
```java
// Generate UUID string without hyphens
String uuidStr = RandomTools.uuidStr();
System.out.println(uuidStr); // e.g.: 550e8400e29b41d4a716446655440000
```


#### 3. View Timestamp of UUID
```java
// Get timestamp information from UUIDv7
String uuidString = RandomTools.uuidStr();
Date timestamp = RandomTools.showTime(uuidString);
System.out.println(timestamp); // e.g.: Wed Oct 25 14:30:45 CST 2023
```


### Notes

1. All generation methods are static and can be called directly through the class name
2. Number generation uses `ThreadLocalRandom` to ensure thread safety and performance
3. UUIDv7 is time-ordered and has better database indexing performance
4. String generation uses a fixed alphanumeric character pool