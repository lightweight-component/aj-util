---
title: Clazz
description: 类加载和类型层次检查工具
tags:
  - 反射
  - 类加载
  - Java
layout: layouts/aj-util-cn.njk
---

# Clazz

`Clazz` 提供类加载和类型层次检查功能。对象实例化已经独立到 `NewInstance`。

## 加载类

`getClassByName(String)` 根据完整类名加载类；找不到类时抛出 `RuntimeException`，当前包装异常不会
保留 `ClassNotFoundException` 作为 cause。泛型重载还会验证类型是否兼容：

```java
Class<CharSequence> type =
        Clazz.getClassByName("java.lang.String", CharSequence.class);

// String 不是 Number，抛出 ClassCastException。
Clazz.getClassByName("java.lang.String", Number.class);
```

目标类型为 `null` 时抛出 `IllegalArgumentException`。

## 将参数转换为类

`args2class(Object[])` 返回每个参数的运行时类型。参数数组为 `null` 或空数组时返回 `null`。
数组内的单个 `null` 元素不受支持；参数值为 `null` 时应显式传入 `Class<?>[]`。

```java
Class<?>[] types = Clazz.args2class(new Object[]{"text", 1});
// [String.class, Integer.class]
```

## 检查接口和父类

`getDeclaredInterface(Class<?>)` 返回类及其父类实现的所有接口，并递归遍历父接口。内部使用
`Set<Class<?>>`，因此菱形接口继承不会产生重复结果。

`getAllSuperClass(Class<?>)` 返回父类链，不包含输入类和 `Object`。传入接口类型是安全的，此时返回空数组。

```java
Class<?>[] interfaces = Clazz.getDeclaredInterface(ArrayList.class);
Class<?>[] parents = Clazz.getAllSuperClass(ArrayList.class);
```

`getClassByInterface(Type)` 根据 `Type.toString()` 推导类名，只适用于可以直接解析的类或参数化接口
类型；类型变量、通配符和泛型数组不能可靠解析。

各层次方法目前没有统一的 `null` 策略：`getAllSuperClass(null)` 会抛出 `NullPointerException`，
而 `getDeclaredInterface(null)` 当前返回空数组。
