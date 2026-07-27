---
title: Methods
description: Finding and invoking Java methods through reflection
tags:
  - reflection
  - method invocation
  - Java
layout: layouts/aj-util.njk
---

# Methods

`Methods` locates and invokes methods on a configured class or object.

## Declared-method lookup

`findDeclaredMethod(String, Class<?>...)` searches the configured class and then its superclasses, excluding
`Object`. It can return private and other non-public declarations and makes the selected method accessible.
The lookup uses exact parameter types and returns `null` when no method exists.

```java
Methods methods = new Methods(MyService.class);
Method method = methods.findDeclaredMethod("handle", String.class);
```

The overload accepting argument values converts each non-null value to its exact runtime class. It does not perform
superclass, interface, or primitive-wrapper matching, and a `null` element causes `NullPointerException`.

## Compatible public-method lookup

`findCompatibleMethod(String, Object...)` searches public methods, including inherited methods. It supports:

- exact runtime types;
- superclass and recursively inherited interface parameters;
- primitive-wrapper pairs such as `int` and `Integer`;
- `null` for non-primitive parameters.

```java
interface Parent {}
interface Child extends Parent {}

class ChildImpl implements Child {}
class Target {
    public void accept(Parent value) {}
}

Method method =
        new Methods(Target.class).findCompatibleMethod("accept", new ChildImpl());
```

The candidate with the lowest type-hierarchy distance is selected. If unrelated overloads receive the same score,
the current implementation selects the first method returned by reflection; callers should use explicit parameter
types when overload ambiguity is possible. Numeric primitive widening and varargs expansion are not supported.

## Invocation

The name-based `execute(instance, methodName, parameters)` overload uses compatible public-method lookup. A missing
method ultimately raises `IllegalArgumentException`. The overload accepting `Class<?>[]` uses declared-method lookup
and can therefore invoke a non-public method after it has been made accessible.

```java
Object result = Methods.execute(service, "handle", new Object[]{"value"});

Object privateResult = Methods.execute(
        service,
        "privateHandle",
        new Class<?>[]{String.class},
        new Object[]{"value"}
);
```

A legitimate target `null` return remains `null`. An exception thrown by the target method is unwrapped from
`InvocationTargetException` and propagated.

`executeStatic(Method, Object[])` accepts only static methods. `executeDefault(...)` is the Java 8-oriented helper
for interface default methods; its legacy `MethodHandles.Lookup` access may not work on newer modular JDKs.
