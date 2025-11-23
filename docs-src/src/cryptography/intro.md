---
title: Introduction to Encryption and Decryption Package
description: Introduction to Encryption and Decryption Package
tags:
  - Encryption
  - Decryption
  - Utilities
layout: layouts/aj-util.njk
---
# Introduction to Encryption and Decryption Package

## Java AES/DES/RSA Encryption and Decryption API

The JDK contains mainstream encryption and decryption APIs, including AES/DES/3DES/RSA. These functionalities are mainly provided by the `javax.crypto` (JCE, Java Cryptography Extension) and `java.security` packages. Among these, only RSA belongs to asymmetric encryption (Asymmetric Encryption), while the others are symmetric encryption (Symmetric Encryption). The similarities and differences between them are as follows:

- It is called "symmetric encryption" because the same key is used for both encryption and decryption, which is "symmetric". Conversely, if one key is used for encryption and another for decryption, it is "asymmetric"
- Symmetric encryption generally executes faster, but its security is relatively poor compared to asymmetric encryption; asymmetric encryption is the opposite, with higher security but relatively poor execution efficiency
- There is no absolute superiority between the two, and the optimal choice should be made according to the occasion requirements

## Source Code

The final code is [here](https://gitcode.com/lightweight-component/aj-util/tree/main/src/main/java/com/ajaxjs/util/cryptography).

Usage can be referenced in [unit tests](https://gitcode.com/lightweight-component/aj-util/blob/main/src/test/java/com/ajaxjs/util/cryptography/TestCryptography.java).

Maven dependency reference, requires Java 8+

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-cryptography</artifactId>
    <version>1.1</version>
</dependency>
```


The component jar package is very small, only about 20kb. It only depends on a utility library I wrote myself.