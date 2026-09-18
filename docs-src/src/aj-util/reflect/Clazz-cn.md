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

`getClassByName(String)` 根据完整类名加载类；找不到类时抛出 `RuntimeException`，并将原始
`ClassNotFoundException` 保留为 cause。泛型重载还会验证类型是否兼容：

```java
Class<CharSequence> type =
        Clazz.getClassByName("java.lang.String", CharSequence.class);

// String 不是 Number，抛出 ClassCastException。
Clazz.getClassByName("java.lang.String", Number.class);
```

目标类型为 `null` 时抛出 `IllegalArgumentException`。

## 将参数转换为类

`args2class(Object[])` 返回每个参数的运行时类型。参数数组为 `null` 或空数组时返回 `null`。
数组内的单个 `null` 元素会触发 `IllegalArgumentException`，异常会指出元素索引；参数值为 `null`
时应显式传入 `Class<?>[]`。

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

`getClassByInterface(Type)` 直接委托给 `Types.type2class(Type)`，不再解析 `Type.toString()` 的展示
字符串。因此它对类、参数化类型、类型变量、通配符和泛型数组遵循相同规则；不能唯一解析的类型会
抛出 `IllegalArgumentException`。

两个层次方法都对 null 类参数抛出 `IllegalArgumentException`。
