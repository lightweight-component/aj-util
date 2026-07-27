---
title: Fields
description: Field hierarchy and wrapped-exception helpers
tags:
  - reflection
  - fields
  - Java
layout: layouts/aj-util.njk
---

# Fields

`Fields` provides field-hierarchy lookup and helpers for unwrapping reflection exceptions.

`getSuperClassDeclaredFields(Class<?>)` returns fields declared by the input class and its superclasses, including
private fields but excluding fields declared by `Object`.

`findField(Class<?>, String)` searches the same hierarchy and returns the first matching `Field`, or `null` when
no field exists. Finding a non-public field does not by itself make it accessible.

```java
Field field = Fields.findField(MyClass.class, "name");
if (field != null && !field.isAccessible())
    field.setAccessible(true);
```

`getUnderLayerErr(Throwable)` unwraps nested `InvocationTargetException` and
`UndeclaredThrowableException` instances. A wrapper without a cause is returned unchanged; a `null` input is
rejected with `IllegalArgumentException`.

`getUnderLayerErrMsg(Throwable)` returns the unwrapped exception text without its leading class name. If an
exception has no detail message, `Throwable.toString()` contains no colon and the class name is currently retained.
