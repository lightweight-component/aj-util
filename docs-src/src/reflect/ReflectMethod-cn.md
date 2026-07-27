---
title: ReflectMethod
description: 通过反射查找和调用 Java 方法
tags:
  - 反射
  - 方法调用
  - Java
layout: layouts/aj-util-cn.njk
---

# ReflectMethod

`ReflectMethod` 用于在指定类或对象上查找并调用方法。`Methods` 已停止使用，新代码应使用本类。

## 精确查找

`getMethod(name, Class<?>...)` 使用精确参数类型查找 public 方法，包括继承的方法。
`getDeclaredMethod(name, Class<?>...)` 只查找当前类声明的方法，并可将非 public 方法设为可访问。
`getSuperClassDeclaredMethod(...)` 沿父类层次查找，找不到时返回 `null`。

```java
ReflectMethod reflect = new ReflectMethod(String.class);
Method substring = reflect.getMethod("substring", int.class, int.class);
```

## 兼容参数查找

有实际参数值时，核心入口是 `findCompatibleMethod(String, Object...)`。它支持精确类型、父类参数、
递归继承的接口、基本类型与包装类型配对，以及非基本类型参数的 `null` 值。类型遍历使用
`Set<Class<?>>` 防止重复访问。

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

该方法只搜索 public 方法；找不到兼容方法时返回 `null`。旧的
`getMethodByArgumentUpCastingSearch()` 和 `getDMethodByArgumentInterface()` 已标记为废弃，
仅作为兼容辅助方法，并统一委托给该入口。

## 调用方法

`execute(instance, method, args)` 调用已知的 `Method`。按方法名调用的重载会使用
`findCompatibleMethod(...)`；找不到方法时抛出 `IllegalArgumentException`。目标方法正常返回
`null` 时仍返回 `null`，目标方法抛出的异常会向上传播。

```java
Target target = new Target();
ReflectMethod reflect = new ReflectMethod();
reflect.execute(target, "accept", new Object[]{new ChildImpl()});
```

需要明确指定精确参数类型时，使用带 `Class<?>[]` 参数的重载。
