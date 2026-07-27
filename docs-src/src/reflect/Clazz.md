---
title: Clazz
description: Class loading and hierarchy inspection utilities
tags:
  - reflection
  - class loading
  - Java
layout: layouts/aj-util.njk
---

# Clazz

`Clazz` contains class-loading and type-hierarchy helpers. Object construction is provided separately by
`NewInstance`.

## Loading classes

`getClassByName(String)` loads a class by its fully qualified name and throws `RuntimeException` if it cannot be
found. The current wrapper does not retain `ClassNotFoundException` as its cause. The typed overload also verifies
assignability:

```java
Class<CharSequence> type =
        Clazz.getClassByName("java.lang.String", CharSequence.class);

// Throws ClassCastException: String is not a Number.
Clazz.getClassByName("java.lang.String", Number.class);
```

Passing a `null` target type throws `IllegalArgumentException`.

## Converting arguments to classes

`args2class(Object[])` returns the runtime class of each argument. A `null` or empty array produces `null`.
Individual `null` elements are not supported; use an explicit `Class<?>[]` when a parameter value is `null`.

```java
Class<?>[] types = Clazz.args2class(new Object[]{"text", 1});
// [String.class, Integer.class]
```

## Inspecting interfaces and superclasses

`getDeclaredInterface(Class<?>)` returns every interface implemented by the class and its superclasses. Parent
interfaces are traversed recursively. A `Set<Class<?>>` prevents duplicates in diamond-shaped interface graphs.

`getAllSuperClass(Class<?>)` returns the superclass chain, excluding the input class and `Object`. It is safe to
call with an interface type and returns an empty array in that case.

```java
Class<?>[] interfaces = Clazz.getDeclaredInterface(ArrayList.class);
Class<?>[] parents = Clazz.getAllSuperClass(ArrayList.class);
```

`getClassByInterface(Type)` derives a class name from `Type.toString()`. It is intended only for directly resolvable
class or parameterized-interface types; type variables, wildcards, and generic arrays are not reliably supported.

The hierarchy methods do not yet share a uniform `null` policy. `getAllSuperClass(null)` throws
`NullPointerException`, while `getDeclaredInterface(null)` currently returns an empty array.
