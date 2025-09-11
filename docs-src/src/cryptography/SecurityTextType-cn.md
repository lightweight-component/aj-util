# SecurityTextType 教程

本教程提供了 `SecurityTextType` 接口的概述，该类是 `lightweight-component/aj-util` 库的一部分。`SecurityTextType` 接口定义了各种安全相关文本编码和哈希类型的枚举。

## 简介

`SecurityTextType` 接口包含嵌套枚举，用于分类不同类型的：

- 文本编码格式
- 摘要（哈希）算法
- 加密操作

## 枚举

### 1. Encode

定义各种文本编码格式：

1. `BASE16` - 十六进制编码
2. `BASE32` - Base32 编码
3. `BASE58` - Base58 编码（用于比特币）
4. `BASE64` - Base64 编码
5. `BASE91` - Base91 编码

### 2. Digest

定义消息摘要（哈希）算法：

1. `Md5` - MD5 哈希
2. `Md5WithSalt` - 带盐的 MD5 哈希

### 3. Cryptography

当前为空，保留用于未来的加密操作类型。

## 使用示例

```java
SecurityTextType.Encode encoding = SecurityTextType.Encode.BASE64;
SecurityTextType.Digest digest = SecurityTextType.Digest.Md5WithSalt;
```

## 结论

`SecurityTextType` 接口提供了一种类型安全的方式，在整个应用程序中引用不同的安全相关文本编码和哈希算法。