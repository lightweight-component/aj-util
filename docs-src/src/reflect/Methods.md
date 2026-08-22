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

`findDeclaredMethodByTypes(String, Class<?>...)` searches the configured class and then its superclasses, excluding
`Object`. It can return private and other non-public declarations and makes the selected method accessible.
The lookup uses exact parameter types and returns `null` when no method exists.

```java
Methods methods = new Methods(MyService.class);
Method method = methods.findDeclaredMethodByTypes("handle", String.class);
```

`findDeclaredMethod(String, Object...)` converts each value to its exact runtime class. It does not perform
superclass, interface, or primitive-wrapper matching. If a value is `null`, the method returns `null` because its
exact type cannot be inferred; use `findDeclaredMethodByTypes(...)` when a null argument has a known declared type.

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

## Exact public-method lookup

`findPublicExactMethod(String, Object[])` looks up a public method using the exact runtime class of every argument.
It returns `null` when an argument is `null`, because an exact type cannot be inferred. The caller can then use
`findCompatibleMethod(...)`, which supports `null` for reference parameters.

`findPublicExactMethodByTypes(String, Class<?>[])` accepts explicit exact types. This is useful when the runtime
value is boxed but the declaration is primitive:

```java
Method setter = new Methods(Target.class)
        .findPublicExactMethodByTypes("setAge", new Class<?>[]{int.class});
```

Both methods use `Class.getMethod`, so they search only public methods, including inherited class and interface
methods. A `null` element in the explicit type array is rejected with `IllegalArgumentException`.

## Invocation

The name-based `execute(instance, methodName, parameters)` overload first attempts exact public-method lookup and
then falls back to compatible public-method lookup. A missing method ultimately raises `IllegalArgumentException`.
The overload accepting `Class<?>[]` performs exact public-method lookup with the supplied types. For example, it
can distinguish `int.class` from `Integer.class`.

```java
Object result = Methods.execute(service, "handle", new Object[]{"value"});

Object primitiveResult = Methods.execute(
        service,
        "setAge",
        new Class<?>[]{int.class},
        new Object[]{Integer.valueOf(18)}
);
```

To invoke a non-public method, resolve it explicitly with `findDeclaredMethodByTypes(...)` and pass the
resulting `Method`
to `execute(instance, method, parameters)`. This makes the visibility change explicit:

```java
Method privateMethod = new Methods(service)
        .findDeclaredMethodByTypes("privateHandle", String.class);
Object privateResult = Methods.execute(service, privateMethod, new Object[]{"value"});
```

A legitimate target `null` return remains `null`. An exception thrown by the target method is unwrapped from
`InvocationTargetException` and propagated.

`executeStatic(Method, Object[])` rejects a null `Method` with `IllegalArgumentException`, accepts only static
methods, and invokes them with a null reflection receiver. Target exceptions are unwrapped and propagated in the
same way as ordinary invocation. `executeDefault(...)` is the Java 8-oriented helper for interface default methods;
its legacy `MethodHandles.Lookup` access may not work on newer modular JDKs.
