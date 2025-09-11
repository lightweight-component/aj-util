---
title: MonotonicULID Tutorial
subTitle: 
description: Implementation and usage of monotonic increasing ULIDs
date: 
tags:
  - UUID
  - ULID
  - Monotonic
layout: layouts/aj-util.njk
---

# MonotonicULID Tutorial

This tutorial provides an overview of the `MonotonicULID` class, which is part of the `lightweight-component/aj-util` library. `MonotonicULID` extends the functionality of the standard ULID by ensuring that identifiers generated within the same millisecond are monotonically increasing.

## Introduction

The `MonotonicULID` class implements a monotonic version of the ULID specification. When multiple ULIDs are generated within the same millisecond, the random component is incremented by 1 bit in the least significant bit position (with carrying). This ensures that ULIDs generated in rapid succession within the same millisecond will still be ordered correctly when sorted lexicographically.

This feature is particularly useful for high-throughput systems where many identifiers might be generated within the same millisecond, and maintaining strict ordering is important.

## Methods

### 1. `random()`

Generates a monotonically increasing ULID using the default secure random generator.

* **Returns:** A new ULID instance that is guaranteed to be greater than any previously generated ULID within the same millisecond.

**Example:**

```java
ULID ulid1 = MonotonicULID.random();
ULID ulid2 = MonotonicULID.random();
// If generated in the same millisecond, ulid2 will be lexicographically greater than ulid1
```

### 2. Constructor: `MonotonicULID(Random random)`

Creates a new MonotonicULID generator with a custom random number generator.

* **Parameters:**
  * `random`: The random number generator to use.

**Example:**

```java
MonotonicULID generator = new MonotonicULID(new SecureRandom());
```

### 3. `next()`

Generates the next monotonic ULID from this generator instance.

* **Returns:** A new ULID instance.
* **Throws:** `IllegalStateException` if entropy overflows in the same millisecond.

**Example:**

```java
MonotonicULID generator = new MonotonicULID(new SecureRandom());
ULID ulid1 = generator.next();
ULID ulid2 = generator.next();
// ulid2 will be greater than ulid1 if generated in the same millisecond
```

## How Monotonicity Works

The monotonicity is achieved through the following mechanism:

1. When a new ULID is requested, the current timestamp is compared with the timestamp of the last generated ULID.
2. If the timestamps are different (new millisecond), new random entropy is generated.
3. If the timestamps are the same (same millisecond), the entropy from the last ULID is incremented by 1.
4. If incrementing would cause an overflow, an `IllegalStateException` is thrown.

This approach ensures that:
- ULIDs are always increasing within the same millisecond
- The timestamp component still maintains the overall time-based ordering
- The probability of collisions remains extremely low

## Use Cases

MonotonicULID is particularly useful for:

1. **Database Primary Keys**: Ensures insertion order matches the key order
2. **Distributed Systems**: Provides consistent ordering across multiple nodes
3. **High-Throughput Applications**: Maintains order even when generating thousands of IDs per millisecond
4. **Time-Series Data**: Perfect for data that needs to be stored in time order

## Comparison with Standard ULID

While standard ULIDs are already sortable by time, MonotonicULID adds an extra guarantee:

- **Standard ULID**: Two ULIDs generated in the same millisecond will be ordered randomly (based on their random component)
- **MonotonicULID**: Two ULIDs generated in the same millisecond will be ordered by their generation sequence

## Conclusion

The `MonotonicULID` class provides an enhanced version of ULID that guarantees strict ordering even within the same millisecond. This makes it ideal for high-performance systems where both uniqueness and precise ordering are critical requirements.