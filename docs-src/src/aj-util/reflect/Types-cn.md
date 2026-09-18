---
title: Types 教程
description: Types 类提供了处理 Java 泛型类型信息的实用方法
tags:
  - 泛型
  - 类型转换
  - Java
layout: layouts/aj-util-cn.njk
---

# Types

`Types` 类提供了检索泛型类型信息、转换类型以及处理参数化类型的方法。这些方法有助于在运行时高效、方便地操作 Java 类型。

`type2class(Type)` 支持所有标准反射 `Type` 实现。类型变量通过唯一上界解析；上界通配符和无界通配符
通过唯一上界解析；泛型数组会先解析组件类型。下界通配符或多个上界不能确定唯一类，因此会抛出
`IllegalArgumentException`。

## 方法

### 1. `getActualType(Type type)`

检索参数化类型的实际类型参数。

* **参数说明：**
    * `type`: 要从中检索实际类型参数的类型。
* **返回值:** 表示实际类型参数的 `Type` 数组，如果输入类型不是参数化类型，则返回 `null`。

**示例:**

```java
Type type = new ParameterizedType() {
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{String.class};
    }

    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
};
Type[] actualType = Types.getActualType(type);
// actualType 将包含 String.class
```

### 2. `getGenericReturnType(Method method)`

检索方法返回类型的实际类型参数。

* **参数说明：**
    * `method`: 要从中检索返回类型的类型。
* **返回值:** 表示返回类型实际类型参数的 `Type` 数组。

**示例:**

```java
Method method = TestTypes.class.getMethod("getList2");
Type[] actualType = Types.getGenericReturnType(method);
// actualType 将包含方法的泛型返回类型
```

### 3. `getGenericFirstReturnType(Method method)`

检索方法返回类型的第一个实际类型参数并将其转换为 `Class`。

* **参数说明：**
    * `method`: 要从中检索返回类型的类型。
* **返回值:** 返回类型的第一个实际类型参数作为 `Class`，如果返回类型不是参数化的，则返回 `null`。
* **异常:** 参数化返回类型不包含实际类型参数时抛出 `IllegalArgumentException`。

**示例:**

```java
Method method = TestTypes.class.getMethod("getList2");
Class<?> actualType = Types.getGenericFirstReturnType(method);
// actualType 将是方法的第一个泛型返回类型，作为 Class
```

### 4. `getActualType(Class<?> clz)`

检索类的超类的实际类型参数。

* **参数说明：**
    * `clz`: 要从中检索实际类型参数的类。
* **返回值:** 表示类的超类的实际类型参数的 `Type` 数组。

**示例:**

```java
Type[] actualType = Types.getActualType(ArrayList.class);
// actualType 将包含 ArrayList 超类的泛型类型参数
```

### 5. `getActualClass(Class<?> clz)`

检索类的超类的第一个实际类型参数并将其转换为 `Class`。

* **参数说明：**
    * `clz`: 要从中检索实际类型参数的类。
* **返回值:** 超类的第一个实际类型参数作为 `Class`。
* **异常:** 如果 `clz` 没有参数化父类，或第一个类型参数无法解析为 `Class`，抛出 `IllegalArgumentException`。

**示例:**

```java
class StringList extends ArrayList<String> {
}

Class<?> actualClass = Types.getActualClass(StringList.class);
// String.class

Types.getActualClass(String.class);
// 抛出 IllegalArgumentException，不再发生 NullPointerException
```

### 6. `type2class(Type type)`

按照以下规则将 `Type` 转换为 `Class`：

- `Class`：直接返回；
- `ParameterizedType`：解析其原始类型；
- `TypeVariable`：解析其唯一上界；
- `WildcardType`：没有下界时解析其唯一上界；
- `GenericArrayType`：解析组件类型并返回对应的数组类。

* **参数说明：**
    * `type`: 要转换的 `Type`。
* **返回值:** `Type` 的 `Class` 表示；输入为 `null` 时返回 `null`。
* **异常:** 类型不受支持或不能唯一解析时抛出 `IllegalArgumentException`，包括下界通配符以及具有多个
  上界的类型变量。

**示例:**

```java
Type type = String.class;
Class<?> actualClass = Types.type2class(type);
// actualClass 将是 String.class

class Example {
    List<String> names;
}

Type listType = Example.class.getDeclaredField("names").getGenericType();
Class<?> rawClass = Types.type2class(listType);
// 如果字段声明为 List<String>，rawClass 将是 List.class
```
