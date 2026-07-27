---
title: Fields
description: 字段层次查找和包装异常处理工具
tags:
  - 反射
  - 字段
  - Java
layout: layouts/aj-util-cn.njk
---

# Fields

`Fields` 提供字段层次查找和反射包装异常解包功能。

`getSuperClassDeclaredFields(Class<?>)` 返回输入类及其父类声明的字段，包括 private 字段，但不包含
`Object` 声明的字段。

`findField(Class<?>, String)` 沿相同层次查找并返回第一个匹配的 `Field`；找不到时返回 `null`。
找到非 public 字段并不会自动使其可访问。

```java
Field field = Fields.findField(MyClass.class, "name");
if (field != null && !field.isAccessible())
    field.setAccessible(true);
```

`getUnderLayerErr(Throwable)` 解开嵌套的 `InvocationTargetException` 和
`UndeclaredThrowableException`。包装异常没有 cause 时原样返回；输入为 `null` 时抛出
`IllegalArgumentException`。

`getUnderLayerErrMsg(Throwable)` 返回去掉开头异常类名后的底层异常文本。异常没有详细消息时，
`Throwable.toString()` 中没有冒号，当前会保留异常类名。
