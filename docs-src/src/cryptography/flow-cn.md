---
title: 基本流程
subTitle: 2025-02-23 by Frank Cheung
description: RsaCrypto
date: 2025-02-23
tags:
  - 基本流程
layout: layouts/aj-util-cn.njk
---

# 基本流程
加密最原始的表达是`密文=加密函数(明文)`。这里顺便说说 MD5，它符合`密文=加密函数(明文)`的定义，看似对输入进行了加密，实则不然，它是哈希函数的一种，跟加密完全不是一个概念，更准确地说它返回了输入参数的“特征”结果，这个特征是唯一的。又因为每次执行都是返回相同的结果，这样的话，可以构成字典表去查对。只要这个字典表足够大，那么 MD5 是非常不安全的。

于是乎，我们对这个加密过程改造，增加入参 key 密钥，希望根据 key 的不同每次返回的密文也不一样，即`密文=加密函数(明文, 密钥)`。

AES（Advanced Encryption Standard） 对称加密也是符合这个原始的流程，粗糙的 Java API 实现如下：

```java
/**
 * 是解密模式还是加密模式？
 */
private final int mode;

/**
 * The name of the algorithm
 */
private final String algorithmName;

/**
 * 密钥
 */
private Key key;

private byte[] data;
    
public byte[] doCipher() {
    try {
        Cipher cipher = Cipher.getInstance(algorithmName);

        if (spec != null)
            cipher.init(mode, key, spec);
        else
            cipher.init(mode, key);

        if (associatedData != null)
            cipher.updateAAD(associatedData);

        return cipher.doFinal(data);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
        throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
        throw new RuntimeException("加密原串的长度不能超过214字节", e);
    } catch (InvalidKeyException e) {
        throw new IllegalArgumentException("Invalid Key.", e);
    } catch (InvalidAlgorithmParameterException e) {
        throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
    }
}
```

首先可见我们没有采用函数风格去定义`doCipher()`，而是通过 getter/setter 去入参（前面有 lombak 去生成）。这些入参都是`doCipher()`执行所需的基本原始数据类型。其次我们抽象一下主要的流程：

1. 确定是哪种算法：AES or DES or RSA?
1. 确定解密模式还是加密模式？
1. key 传入密钥（是`byte[]`？还是 Java `Key`对象），另外还有是否需要`AlgorithmParameterSpec spec`入参？
1. 输入数据 data（是`byte[]`？还是 String  还是 Base64 String？），然后执行加密/解密
1. 原始返回是`byte[]`，那么调用者希望是要直接返回 String，还是 Base64 String，抑或 HexString?


看来加解密过程核心数据类型都是`byte[]`。无论哪种类型入参都要最终转换到`byte[]`。每多考虑一种数据类型入参，便利性就多一点（不需要 API 调用者去手动转换），那么相应地就要安排多一个 setter 来进行转换。

大致的代码风格思路确定了，接着就可以着手进行编码了。