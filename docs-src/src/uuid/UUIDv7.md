---
title: UUIDv7 Tutorial
subTitle: 
description: Implementation and usage of time-ordered UUIDs
date: 
tags:
  - UUID
  - UUIDv7
  - Time-ordered
layout: layouts/aj-util.njk
---

# UUIDv7 Tutorial

This tutorial provides an overview of the `UUIDv7` class, which is part of the `lightweight-component/aj-util` library. `UUIDv7` implements the UUID version 7 specification, which offers time-ordered UUIDs with improved properties over traditional random UUIDs.

## Introduction

The `UUIDv7` class implements the UUID version 7 format as defined in the proposed UUID specification. UUIDv7 is designed to be time-ordered while maintaining the uniqueness guarantees of UUIDs. It embeds a Unix timestamp with millisecond precision in the most significant bits, making UUIDs sortable by creation time.

UUIDv7 addresses several limitations of the traditional random UUID (version 4) by:
1. Making UUIDs time-ordered for better database indexing
2. Maintaining compatibility with the UUID standard
3. Providing a more efficient representation of time than UUID version 1

## Methods

### 1. `randomUUID()`

Generates a new UUIDv7 using the current timestamp and secure random values.

* **Returns:** A new UUID instance conforming to the UUIDv7 format.

**Example:**

```java
UUID uuid = UUIDv7.randomUUID();
System.out.println(uuid); // e.g., "017f22e2-79b0-7cc3-98c4-dc0c0c07398f"
```

## UUIDv7 Structure

The UUIDv7 format follows this structure:

```
0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                           unix_ts_ms                           |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          unix_ts_ms           |  ver  |       rand_a          |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|var|                        rand_b                             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                            rand_b                             |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

Where:
- `unix_ts_ms`: 48 bits of Unix timestamp in milliseconds
- `ver`: 4 bits representing the UUID version (7)
- `var`: 2 bits representing the UUID variant (2)
- `rand_a` and `rand_b`: Random bits for uniqueness

## Implementation Details

The `UUIDv7` class generates UUIDs with the following characteristics:

1. The first 48 bits contain a Unix timestamp in milliseconds
2. The version bits (bits 48-51) are set to `0111` (7)
3. The variant bits (bits 64-65) are set to `10` (RFC 4122 variant)
4. The remaining bits are filled with cryptographically secure random values

This implementation uses `SecureRandom` to generate the random portions of the UUID, ensuring a high degree of uniqueness and unpredictability.

## Use Cases

UUIDv7 is particularly well-suited for:

1. **Database Primary Keys**: The time-ordered nature makes them efficient for indexing
2. **Distributed Systems**: They can be generated independently while maintaining rough time ordering
3. **Logging and Tracing**: The embedded timestamp helps with chronological analysis
4. **Any system requiring sortable unique identifiers**: Where the order of creation matters

## Comparison with Other UUID Versions

- **UUID v1**: Uses MAC address and time, but has privacy concerns and non-intuitive ordering
- **UUID v4**: Completely random, no sortability
- **UUID v7**: Time-ordered with random components, better for database indexing

## Conclusion

The `UUIDv7` class provides a modern implementation of time-ordered UUIDs that maintain compatibility with the UUID standard while offering improved properties for database systems and distributed applications. By embedding a timestamp in a standardized way, UUIDv7 enables efficient sorting and indexing while preserving the uniqueness guarantees expected from UUIDs.