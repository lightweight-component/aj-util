---
title: NewInstance
description: 通过 public 构造器创建对象
tags:
  - 反射
  - 实例化
  - Java
layout: layouts/aj-util-cn.njk
---

# NewInstance

`NewInstance<T>` 通过 public 构造器创建对象。接口不能实例化。

```java
String value = new NewInstance<>(String.class).newInstance();

StringBuilder builder =
        new NewInstance<>(StringBuilder.class, "text").newInstance();
```

传入参数时，当前实现按照参数的精确运行时类型选择构造器。因此，子类型参数不能匹配接收父类或接口的
public 构造器；包含 `null` 的参数也需要调用方显式取得 `Constructor`。

类参数必须非 `null` 且可实例化。null 类参数会触发 `IllegalArgumentException`；接口会被明确拒绝；
抽象类在实际调用构造器时失败。

## 构造器辅助方法

`getConstructor(Class<T>, Class<?>...)` 返回参数类型精确匹配的 public 构造器；找不到时抛出
`RuntimeException`。`newInstance(Constructor<T>, Object...)` 调用已经选定的构造器，并将原始异常
保留为 cause。null 类参数或构造器会触发 `IllegalArgumentException`。

```java
Constructor<String> constructor =
        NewInstance.getConstructor(String.class, String.class);
String value = NewInstance.newInstance(constructor, "text");
```

`hasArgsCon(Class<?>)` 判断类是否公开了至少一个有参数的 public 构造器。

非 public 构造器不在该工具的支持范围内。`hasArgsCon(Class<?>)` 同样对 null 类参数抛出
`IllegalArgumentException`。
