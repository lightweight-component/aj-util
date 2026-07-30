---
title: Methods
description: 通过反射查找和调用 Java 方法
tags:
  - 反射
  - 方法调用
  - Java
layout: layouts/aj-util-cn.njk
---

# Methods

`Methods` 用于在指定类或对象上查找并调用方法。

## 声明方法查找

`findDeclaredMethod(String, Class<?>...)` 从指定类开始沿父类向上查找，但不包含 `Object`。它可以返回
private 等非 public 声明，并将找到的方法设为可访问。该入口按精确参数类型查找，找不到时返回 `null`。

```java
Methods methods = new Methods(MyService.class);
Method method = methods.findDeclaredMethod("handle", String.class);
```

接收实际参数值的重载，会把每个非 `null` 参数转换成精确运行时类型。它不进行父类、接口或基本类型与
包装类型的兼容匹配；参数数组中包含 `null` 元素时会抛出 `NullPointerException`。

## 兼容 public 方法查找

`findCompatibleMethod(String, Object...)` 搜索 public 方法，包括继承的方法。它支持：

- 精确运行时类型；
- 父类参数和递归继承的接口参数；
- `int`/`Integer` 等基本类型与包装类型配对；
- 非基本类型参数的 `null` 值。

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

方法会选择类型层次距离最小的候选项。如果互不相关的重载得到相同分数，当前实现会选择反射返回的
第一个方法；可能产生重载歧义时，应显式传入参数类型。当前不支持基本数值类型扩宽和可变参数展开。

## 精确 public 方法查找

`findPublicExactMethod(String, Object[])` 按每个实参的精确运行时类型查找 public 方法。参数中包含
`null` 时无法推断精确类型，因此返回 `null`；调用方随后可使用支持引用类型 `null` 参数的
`findCompatibleMethod(...)`。

`findPublicExactMethodByTypes(String, Class<?>[])` 接收显式精确类型，适合运行时值是包装类型、声明
参数却是基本类型的情况：

```java
Method setter = new Methods(Target.class)
        .findPublicExactMethodByTypes("setAge", new Class<?>[]{int.class});
```

两个入口都使用 `Class.getMethod`，因此只搜索 public 方法，包括继承的类方法和接口方法。显式类型
数组包含 `null` 元素时会抛出 `IllegalArgumentException`。

## 调用方法

按方法名调用的 `execute(instance, methodName, parameters)` 会先尝试精确 public 方法查找，找不到
时再回退到兼容 public 方法查找。最终仍找不到时抛出 `IllegalArgumentException`。带 `Class<?>[]`
参数的重载按调用者给出的类型执行精确 public 方法查找，例如可以明确区分 `int.class` 与
`Integer.class`。

```java
Object result = Methods.execute(service, "handle", new Object[]{"value"});

Object primitiveResult = Methods.execute(
        service,
        "setAge",
        new Class<?>[]{int.class},
        new Object[]{Integer.valueOf(18)}
);
```

需要调用非 public 方法时，应先通过 `findDeclaredMethod(...)` 显式取得 `Method`，再调用
`execute(instance, method, parameters)`，使访问范围变化清晰可见：

```java
Method privateMethod = new Methods(service)
        .findDeclaredMethod("privateHandle", String.class);
Object privateResult = Methods.execute(service, privateMethod, new Object[]{"value"});
```

目标方法正常返回 `null` 时仍返回 `null`。目标方法抛出的异常会从 `InvocationTargetException`
中解包并继续向上传播。

`executeStatic(Method, Object[])` 只接受静态方法。`executeDefault(...)` 是面向 Java 8 的接口默认方法
辅助入口；它对旧版 `MethodHandles.Lookup` 的访问在较新的模块化 JDK 上可能无法工作。
