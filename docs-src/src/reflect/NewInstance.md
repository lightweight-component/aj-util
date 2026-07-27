---
title: NewInstance
description: Creating objects through public constructors
tags:
  - reflection
  - instantiation
  - Java
layout: layouts/aj-util.njk
---

# NewInstance

`NewInstance<T>` creates objects through public constructors. Interfaces cannot be instantiated.

```java
String value = new NewInstance<>(String.class).newInstance();

StringBuilder builder =
        new NewInstance<>(StringBuilder.class, "text").newInstance();
```

When arguments are supplied, the constructor is currently selected by their exact runtime classes. A public
constructor accepting a superclass or interface is therefore not matched from a subtype argument. Parameters
containing `null` also require an explicitly obtained `Constructor`.

The class argument must be non-null and concrete. Interfaces are rejected explicitly; abstract classes fail later
during constructor invocation.

## Constructor helpers

`getConstructor(Class<T>, Class<?>...)` returns an exact public constructor and throws `RuntimeException` if it
cannot be found. `newInstance(Constructor<T>, Object...)` invokes an already selected constructor and preserves
the original failure as its cause.

```java
Constructor<String> constructor =
        NewInstance.getConstructor(String.class, String.class);
String value = NewInstance.newInstance(constructor, "text");
```

`hasArgsCon(Class<?>)` reports whether the class exposes at least one public constructor with parameters.

Non-public constructors are outside this utility's supported scope. Passing a `null` `Constructor` is not validated
explicitly and currently produces `NullPointerException`.
