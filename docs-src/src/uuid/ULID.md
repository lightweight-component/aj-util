---
title: ULID Tutorial
subTitle: 
description: Implementation and usage of Universally Unique Lexicographically Sortable Identifiers
date: 
tags:
  - UUID
  - ULID
  - Unique Identifier
layout: layouts/aj-util.njk
---

# ULID Tutorial

This tutorial provides an overview of the `ULID` class, which is part of the `lightweight-component/aj-util` library. `ULID` (Universally Unique Lexicographically Sortable Identifier) offers a modern alternative to UUID with improved features.

## Introduction

The `ULID` class implements the ULID specification, which provides 128-bit compatibility with UUID while offering lexicographic sorting and better readability through Crockford's Base32 encoding. ULIDs are composed of:

1. A timestamp component (48 bits) - millisecond precision
2. A randomness component (80 bits) - cryptographically secure random values

This combination ensures that ULIDs are:
- Time-ordered (lexicographically sortable)
- URL-safe and case-insensitive
- Highly unlikely to have collisions
- Binary compatible with UUID

## Methods

### 1. `random()` and `random(Random random)`

Generates a random ULID using the current timestamp and random entropy.

* **Parameters:**
  * `random` (optional): A custom random generator.
* **Returns:** A new ULID instance.

**Example:**

```java
// Using default secure random
ULID ulid = ULID.random();

// Using custom random generator
ULID customUlid = ULID.random(new SecureRandom());
```

### 2. `generate(long time, byte[] entropy)`

Generates a ULID from a specific timestamp and entropy bytes.

* **Parameters:**
  * `time`: 48-bit timestamp (milliseconds since epoch).
  * `entropy`: 80-bit (10 bytes) random data.
* **Returns:** A new ULID instance.
* **Throws:** `IllegalArgumentException` if the timestamp is invalid or entropy is not 10 bytes.

**Example:**

```java
long timestamp = System.currentTimeMillis();
byte[] entropy = new byte[10];
new SecureRandom().nextBytes(entropy);

ULID ulid = ULID.generate(timestamp, entropy);
```

### 3. `fromString(String val)`

Parses a ULID from its string representation.

* **Parameters:**
  * `val`: 26-character string of Crockford Base32.
* **Returns:** A ULID instance.
* **Throws:** `IllegalArgumentException` if the string is invalid.

**Example:**

```java
ULID ulid = ULID.fromString("01ARZ3NDEKTSV4RRFFQ69G5FAV");
```

### 4. `fromBytes(byte[] v)`

Constructs a ULID from its binary representation.

* **Parameters:**
  * `v`: 16 bytes binary data.
* **Returns:** A ULID instance.
* **Throws:** `IllegalArgumentException` if the byte array length is not 16.

**Example:**

```java
byte[] bytes = new byte[16];
// Fill bytes with valid ULID data
ULID ulid = ULID.fromBytes(bytes);
```

### 5. `fromUUID(UUID val)` and `toUUID()`

Converts between ULID and UUID formats.

* **Parameters:**
  * `val`: A UUID instance.
* **Returns:** A ULID instance or UUID instance.

**Example:**

```java
// Convert UUID to ULID
UUID uuid = UUID.randomUUID();
ULID ulid = ULID.fromUUID(uuid);

// Convert ULID to UUID
UUID convertedUuid = ulid.toUUID();
```

### 6. `toString()`

Returns the ULID as a Crockford's Base32 encoded string (26 characters).

* **Returns:** A 26-character string representation.

**Example:**

```java
ULID ulid = ULID.random();
String ulidString = ulid.toString(); // e.g., "01F8MECHZCP3WGH3SNAPCH3D5E"
```

### 7. `getTimestamp()` and `getEntropy()`

Extracts the timestamp or entropy components from a ULID.

* **Returns:** The timestamp as a long or entropy as a byte array.

**Example:**

```java
ULID ulid = ULID.random();
long timestamp = ulid.getTimestamp();
byte[] entropy = ulid.getEntropy();
```

## Comparison with UUID

ULIDs offer several advantages over traditional UUIDs:

1. **Sortability**: ULIDs are lexicographically sortable due to their timestamp-first structure.
2. **Readability**: The Crockford Base32 encoding is more human-readable than UUID's hexadecimal format.
3. **Timestamp**: ULIDs embed creation time, making them useful for time-based operations.
4. **Compatibility**: ULIDs maintain binary compatibility with UUID.

## Conclusion

The `ULID` class provides a modern identifier solution that combines the uniqueness of UUIDs with improved sortability and readability. It's particularly useful for distributed systems, databases, and any application requiring unique, time-ordered identifiers.