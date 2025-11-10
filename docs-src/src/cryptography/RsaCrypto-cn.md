---
title: RSA 加密解密
subTitle: 2025-02-23 by Frank Cheung
description: RsaCrypto
date: 2025-02-23
tags:
  - RSA 加密解密
layout: layouts/aj-util-cn.njk
---

# RSA 加密解密
RSA 非对称加密，事情比较多，可以分解为下面的子任务：

- 签名，封装在`com.ajaxjs.util.cryptography.rsa.DoSignature`完成
- 校验签名，封装在`com.ajaxjs.util.cryptography.rsa.DoVerify`完成
- 密钥管理，封装在`com.ajaxjs.util.cryptography.rsa.KeyMgr`完成
- 本身的 RSA 加密解密

下面分别进行介绍。
## 签名
入参包括算法、输入数据及私钥，执行`sign()`返回签名。涉及的类型如下：

- 输入数据，可以是`byte[]`或字符串
- 私钥，可以是 PrivateKey 对象或者字符串，字符串的话会经过`KeyMgr.restoreKey`还原为 PrivateKey 对象
- 返回的签名数据，是`byte[]`，可以调用`signToString()`返回 base64 编码的字符串

```java
// 生成公钥私钥
KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
keyMgr.generateKeyPair();
String privateKey = keyMgr.getPrivateKeyStr();

byte[] helloWorlds = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).sign();
String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();

assertEquals(EncodeTools.base64EncodeToString(helloWorlds), result);
```

值得一提的是，私钥哪里来？你可以通过如上的`KeyMgr`生成。

## 校验签名
入参包括算法、输入数据、签名数据及公钥，执行`verify()`返回签名。涉及的类型如下：

- 输入数据，可以是`byte[]`或字符串
- 签名数据，可以是`byte[]`或 Base64 字符串
- 公钥，可以是 PublicKey 对象或者字符串，字符串的话会经过`KeyMgr.restoreKey`还原为 PublicKey 对象
- 返的签名是否合法，是`boolean`

```java
// 生成公钥私钥
KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
keyMgr.generateKeyPair();
String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();
String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();
boolean verified = new DoVerify(Constant.SHA256_RSA).setStrData("hello world").setPublicKeyStr(publicKey).setSignatureBase64(result).verify();

assertTrue(verified);
```

值得一提的是，公钥、私钥哪里来？你可以通过如上的`KeyMgr`生成。

## RSA 加密解密
没什么好说的了，直接上 API 例子。
```java
// 生成公钥私钥
KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
keyMgr.generateKeyPair();
String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();

System.out.println("公钥: \n\r" + publicKey);
System.out.println("私钥： \n\r" + privateKey);
//		System.out.println("公钥加密--------私钥解密");

String word = "你好，世界！";

byte[] encWord = KeyMgr.publicKeyEncrypt(word.getBytes(), publicKey);
String decWord = new String(KeyMgr.privateKeyDecrypt(encWord, privateKey));

String eBody = EncodeTools.base64EncodeToString(encWord);
String decWord2 = new String(KeyMgr.privateKeyDecrypt(EncodeTools.base64Decode(eBody), privateKey));
System.out.println("加密前: " + word + "\n\r密文：" + eBody + "\n解密后: " + decWord2);
assertEquals(word, decWord);

//		System.out.println("私钥加密--------公钥解密");

String english = "Hello, World!";
byte[] encEnglish = KeyMgr.privateKeyEncrypt(english.getBytes(), privateKey);
String decEnglish = new String(KeyMgr.publicKeyDecrypt(encEnglish, publicKey));
//		System.out.println("加密前: " + english + "\n\r" + "解密后: " + decEnglish);

assertEquals(english, decEnglish);
//		System.out.println("私钥签名——公钥验证签名");

// 产生签名
String sign = new DoSignature(Constant.MD5_RSA).setPrivateKeyStr(privateKey).setData(encEnglish).signToString();
//		System.out.println("签名:\r" + sign);
// 验证签名
assertTrue(new DoVerify(Constant.MD5_RSA).setPublicKeyStr(publicKey).setData(encEnglish).setSignatureBase64(sign).verify());
```
## 密钥管理
关于密钥的一些工具方法在`KeyMgr`，包括公钥和私钥的。一般开源的都喜欢把`KeyPair`封装为 Map，而笔者觉得直接使用`KeyPair`本身就可以了，如果不太满足，则增加某些方法。例如`getPublicKeyBytes()`、`getPublicKeyStr()`、`getPublicToPem()`，相比使用 Map 更加清晰。

另外对于密钥本身还可以加密解密，安全性更高。