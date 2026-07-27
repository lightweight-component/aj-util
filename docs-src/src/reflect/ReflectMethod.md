---
title: ReflectMethod
description: Finding and invoking Java methods through reflection
tags:
  - reflection
  - method invocation
  - Java
layout: layouts/aj-util.njk
---

# ReflectMethod

`ReflectMethod` finds and invokes methods on a configured class or object. `Methods` has been retired; use this
class for new code.

## Exact lookup

`getMethod(name, Class<?>...)` searches public methods, including inherited methods, using exact parameter types.
`getDeclaredMethod(name, Class<?>...)` searches only the configured class and may make a non-public method
accessible. `getSuperClassDeclaredMethod(...)` walks the class hierarchy and returns `null` when no declaration is
found.

```java
ReflectMethod reflect = new ReflectMethod(String.class);
Method substring = reflect.getMethod("substring", int.class, int.class);
```

## Compatible lookup

`findCompatibleMethod(String, Object...)` is the main API when argument values are available. It supports exact
matches, superclass parameters, recursively inherited interfaces, primitive-wrapper pairs, and `null` for
non-primitive parameters. Type traversal uses a `Set<Class<?>>` to avoid duplicate visits.

```java
interface Parent {}
interface Child extends Parent {}

class ChildImpl implements Child {}
class Target {
    public void accept(Parent value) {}
}

ReflectMethod reflect = new ReflectMethod(Target.class);
Method method = reflect.findCompatibleMethod("accept", new ChildImpl());
```

It searches public methods. If no compatible method exists it returns `null`. The older
`getMethodByArgumentUpCastingSearch()` and `getDMethodByArgumentInterface()` methods are deprecated compatibility
helpers and delegate to this API.

## Invocation

`execute(instance, method, args)` invokes a known `Method`. The name-based overload uses
`findCompatibleMethod(...)`; a missing method results in `IllegalArgumentException`. A legitimate `null` return
value remains `null`, while the target exception is propagated.

```java
Target target = new Target();
ReflectMethod reflect = new ReflectMethod();
reflect.execute(target, "accept", new Object[]{new ChildImpl()});
```

Use the overload accepting `Class<?>[]` when exact parameter types must be specified explicitly.
