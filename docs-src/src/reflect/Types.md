---
title: Types Tutorial
description: Utility methods for working with Java generic type information
tags:
  - generics
  - type conversion
  - Java
layout: layouts/aj-util.njk
---

# Types

The `Types` class provides methods for retrieving generic type information, converting types, and handling parameterized
types. These methods facilitate efficient and convenient manipulation of Java types at runtime.

`type2class(Type)` handles all standard reflection `Type` implementations. Type variables resolve through their
single upper bound, upper-bounded and unbounded wildcards resolve through their single upper bound, and generic
arrays resolve after their component type. A lower-bounded wildcard or multiple upper bounds cannot identify one
unique class and therefore cause `IllegalArgumentException`.

## Methods

### 1. `getActualType(Type type)`

Retrieves the actual type arguments of a parameterized type.

* **Parameters:**
    * `type`: The type to retrieve the actual type arguments from.
* **Returns:** An array of `Type` representing the actual type arguments, or `null` if the input type is not a
  parameterized type.

**Example:**

```java
Type type = new ParameterizedType() {
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{String.class};
    }

    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
};
Type[] actualType = Types.getActualType(type);
// actualType will contain String.class
```

### 2. `getGenericReturnType(Method method)`

Retrieves the actual type arguments of a method's return type.

* **Parameters:**
    * `method`: The method to retrieve the return type from.
* **Returns:** An array of `Type` representing the actual type arguments of the return type.

**Example:**

```java
Method method = TestTypes.class.getMethod("getList2");
Type[] actualType = Types.getGenericReturnType(method);
// actualType will contain the generic return type of the method
```

### 3. `getGenericFirstReturnType(Method method)`

Retrieves the first actual type argument of a method's return type and converts it to a `Class`.

* **Parameters:**
    * `method`: The method to retrieve the return type from.
* **Returns:** The first actual type argument as a `Class`, or `null` if the return type is not parameterized.
* **Throws:** `IllegalArgumentException` if a parameterized return type contains no actual type arguments.

**Example:**

```java
Method method = TestTypes.class.getMethod("getList2");
Class<?> actualType = Types.getGenericFirstReturnType(method);
// actualType will be the first generic return type of the method as a Class
```

### 4. `getActualType(Class<?> clz)`

Retrieves the actual type arguments of a class's superclass.

* **Parameters:**
    * `clz`: The class to retrieve the actual type arguments from.
* **Returns:** An array of `Type` representing the actual type arguments of the class's superclass.

**Example:**

```java
Type[] actualType = Types.getActualType(ArrayList.class);
// actualType will contain the generic type arguments of ArrayList's superclass
```

### 5. `getActualClass(Class<?> clz)`

Retrieves the first actual type argument of a class's superclass and converts it to a `Class`.

* **Parameters:**
    * `clz`: The class to retrieve the actual type argument from.
* **Returns:** The first actual type argument as a `Class`.
* **Throws:** `IllegalArgumentException` if `clz` has no parameterized superclass or its first type argument cannot be
  resolved to a `Class`.

**Example:**

```java
class StringList extends ArrayList<String> {
}

Class<?> actualClass = Types.getActualClass(StringList.class);
// String.class

Types.getActualClass(String.class);
// throws IllegalArgumentException instead of failing with NullPointerException
```

### 6. `type2class(Type type)`

Converts a `Type` to a `Class` according to these rules:

- `Class`: returned directly;
- `ParameterizedType`: resolves its raw type;
- `TypeVariable`: resolves its only upper bound;
- `WildcardType`: resolves its only upper bound when it has no lower bound;
- `GenericArrayType`: resolves its component and returns the corresponding array class.

* **Parameters:**
    * `type`: The `Type` to convert.
* **Returns:** The `Class` representation of the `Type`, or `null` when the input is `null`.
* **Throws:** `IllegalArgumentException` when the type is unsupported or has no unique resolution, including
  lower-bounded wildcards and type variables with multiple upper bounds.

**Example:**

```java
Type type = String.class;
Class<?> actualClass = Types.type2class(type);
// actualClass will be String.class

class Example {
    List<String> names;
}

Type listType = Example.class.getDeclaredField("names").getGenericType();
Class<?> rawClass = Types.type2class(listType);
// rawClass will be List.class for a field declared as List<String>
```
