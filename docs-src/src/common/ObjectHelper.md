---
title: ObjectHelper Tutorial
description: Provides utility methods for working with Java objects
date: 2025-09-11
tags:
  - object
  - utilities
  - cloning
layout: layouts/aj-util.njk
---
# ObjectHelper Tutorial

`ObjectHelper` is an object utility class that provides common utility methods for working with Java objects, collections, maps, and various data structures.

### Main Features

1. **Text content checking and validation**
2. **Collection and array emptiness checks**
3. **Convenient factory methods for creating collections (maps, lists, sets)**
4. **Capacity calculation utilities**

### Text Checking Methods

#### 1. hasText Method
Checks if the given string has actual text content:

```java
// Check if string has non-whitespace content
boolean result1 = ObjectHelper.hasText("Hello");     // true
boolean result2 = ObjectHelper.hasText("   ");       // false
boolean result3 = ObjectHelper.hasText("");          // false
boolean result4 = ObjectHelper.hasText(null);        // false
```


#### 2. isEmptyText Method
Checks if the given string is empty or contains only whitespace:

```java
// Check if string is empty or contains only whitespace
boolean result1 = ObjectHelper.isEmptyText("Hello"); // false
boolean result2 = ObjectHelper.isEmptyText("   ");   // true
boolean result3 = ObjectHelper.isEmptyText("");      // true
boolean result4 = ObjectHelper.isEmptyText(null);    // true
```


### Emptiness Checking Methods

#### 1. Array Emptiness Check
```java
// Check if array is empty
String[] array1 = null;
String[] array2 = new String[0];
String[] array3 = {"Hello"};

boolean result1 = ObjectHelper.isEmpty(array1); // true
boolean result2 = ObjectHelper.isEmpty(array2); // true
boolean result3 = ObjectHelper.isEmpty(array3); // false
```


#### 2. Collection Emptiness Check
```java
// Check if collection is empty
List<String> list1 = null;
List<String> list2 = new ArrayList<>();
List<String> list3 = Arrays.asList("Hello");

boolean result1 = ObjectHelper.isEmpty(list1); // true
boolean result2 = ObjectHelper.isEmpty(list2); // true
boolean result3 = ObjectHelper.isEmpty(list3); // false
```


#### 3. Map Emptiness Check
```java
// Check if map is empty
Map<String, String> map1 = null;
Map<String, String> map2 = new HashMap<>();
Map<String, String> map3 = new HashMap<>();
map3.put("key", "value");

boolean result1 = ObjectHelper.isEmpty(map1); // true
boolean result2 = ObjectHelper.isEmpty(map2); // true
boolean result3 = ObjectHelper.isEmpty(map3); // false
```


### Collection Creation Methods

#### 1. Creating Maps
```java
// Create single key-value pair map
Map<String, String> map1 = ObjectHelper.mapOf("key1", "value1");

// Create double key-value pair map
Map<String, String> map2 = ObjectHelper.mapOf("key1", "value1", "key2", "value2");

// Create triple key-value pair map
Map<String, String> map3 = ObjectHelper.mapOf("key1", "value1", "key2", "value2", "key3", "value3");

// Create map with specified capacity
Map<String, String> map4 = ObjectHelper.mapOf(10); // HashMap with optimized capacity
```


#### 2. Creating Lists
```java
// Create immutable list
List<String> list1 = ObjectHelper.listOf("item1");
List<String> list2 = ObjectHelper.listOf("item1", "item2", "item3");
List<String> list3 = ObjectHelper.listOf(); // Empty list
```


#### 3. Creating Sets
```java
// Create immutable set
Set<String> set1 = ObjectHelper.setOf("item1");
Set<String> set2 = ObjectHelper.setOf("item1", "item2", "item3");
Set<String> set3 = ObjectHelper.setOf(); // Empty set

// Automatic deduplication
Set<String> set4 = ObjectHelper.setOf("item1", "item1", "item2"); // Contains only "item1" and "item2"
```


### Constants

- `EMPTY_PARAMS_MAP`: An empty, immutable map useful as a default or sentinel value when no parameters are needed
- `DEFAULT_LOAD_FACTOR`: Default load factor for hash maps, optimized for time and space efficiency

### Notes

1. `listOf` methods create immutable collections
2. `setOf` method does not allow null elements
3. `mapOf` methods optimize initial capacity based on expected size to minimize resizing operations