---
title: AES 与基于密码的加密
layout: layouts/aj-util-cn.njk
---

# AES 与基于密码的加密

这些类位于 `com.ajaxjs.util.cryptography.aes`。此 URL 沿用旧 `Cryptography` 页面；请使用下列当前类，而不是已删除的静态便捷方法。

## AesGcm：新数据优先使用

```java
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import com.ajaxjs.util.cryptography.aes.AesCipherResult;
import com.ajaxjs.util.cryptography.aes.AesGcm;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

byte[] key = SecretKeyMgr.getSecretKey("AES", 128, new SecureRandom()).getEncoded();
AesGcm aes = new AesGcm(key);
byte[] aad = "record:v1".getBytes(StandardCharsets.UTF_8);
AesCipherResult encrypted = aes.encrypt("secret", aad);
String ciphertextBase64 = encrypted.toBase64();
byte[] nonce = encrypted.getNonce().getResult();
String plaintext = aes.decrypt(ciphertextBase64, nonce, aad);
```

`AesGcm(byte[])` 接受原始 AES 密钥字节；`AesGcm(String)` 接受 **Base64 编码密钥**，不是密码。密钥通常为 16/24/32 字节。`encrypt(String)` 和 `encrypt(String, byte[] associatedData)` 每次生成安全随机的 **12 字节 nonce**。双参数中的字节数组是 AAD，**不是 nonce**。`encrypt(String, byte[] nonce, byte[] associatedData)` 用于协议自行管理 nonce 的场景，并检查长度。

转换为 `AES/GCM/NoPadding`，使用 **128 位 tag**。`AesCipherResult` 保存 `[密文][16 字节 tag]`；单独的 `getNonce()` 保存 nonce。Base64 输出**不包含** nonce。须同时保存/传输二者，以及密钥标识、格式版本和完全一致的 AAD（或可重建它的方法）。AAD 被认证但不加密，也不保存在结果中。nonce 不必保密，但同一密钥下绝不能重复；随机生成不是强制唯一性登记机制。

`decrypt(String base64Ciphertext, byte[] nonce, byte[] associatedData)` 返回 UTF-8 明文。错误密钥、nonce、AAD 或篡改导致认证失败，抛出 `IllegalArgumentException`。生产环境绝不能复用固定演示 nonce。

## AesPbe：密码派生的 AES-GCM

以下完整方法从调用方接收密码；不要硬编码生产密码。

```java
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.aes.AesPbe;

public class PasswordExample {
    public static String roundTrip(String password) {
        byte[] salt = DoCipher.randomBytes(AesPbe.PBE_SALT_LENGTH);
        int iterations = AesPbe.MIN_PBE_ITERATIONS;
        AesPbe pbe = new AesPbe(password, salt, iterations);
        byte[] encrypted = pbe.encrypt("secret");
        return new AesPbe(password, salt, iterations).decrypt(encrypted);
    }
}
```

`AesPbe(String password, byte[] salt, int iterationCount)` 使用 `PBKDF2WithHmacSHA256` 派生 **AES-128**。它拒绝 null/空密码、少于 **16 字节**的盐和低于 **100,000** 的迭代次数，但不拒绝纯空白密码。此下限只是实现门槛，不是适用于所有部署的最新密码强化建议；应测量性能并选择合适的更高工作因子。

`encrypt(String)` 返回原始 `[12 字节 nonce][密文][16 字节 tag]`；`decrypt(byte[])` 要求此格式，拒绝 null 或不足 28 字节的数据。它**不是** `AesGcm` 的分离 nonce 格式。随机盐、迭代次数、KDF/格式版本和密钥策略须另行保存。每次独立派生使用新的随机盐。密码/盐/迭代次数错误或篡改会导致认证失败。这些 PBE 便捷方法没有 AAD 参数。

对象保留派生密钥。内部 `PBEKeySpec` 的密码会清除，但调用方不可变 String 及所有密钥材料不会自动擦除。

## AesCbc：仅用于互通

```java
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import com.ajaxjs.util.cryptography.aes.AesCbc;
import com.ajaxjs.util.cryptography.aes.AesCipherResult;
import java.security.SecureRandom;

AesCbc aes = new AesCbc(SecretKeyMgr.getSecretKey("AES", 128, new SecureRandom()).getEncoded());
AesCipherResult encrypted = aes.encrypt("legacy protocol content");
String plaintext = aes.decrypt(encrypted.toBase64(), encrypted.getNonce().toBase64());
```

CBC 使用 `AES/CBC/PKCS5Padding`，原始/Base64 密钥构造函数与 GCM 一致。`encrypt(String)` 生成随机 **16 字节 IV**；`encrypt(String, byte[] iv)` 要求恰好 16 字节。这里 `AesCipherResult.getNonce()` 实际保存 IV。`decrypt(String, String)` 要求 **Base64 密文和 Base64 IV**，不同于 GCM 的字节数组 nonce。

CBC 不提供认证：填充/解密成功不能证明完整性。仅在已有认证协议中使用，并生成新的不可预测 IV；不要自行设计 CBC 协议或向攻击者暴露不同填充错误。新方案优先 GCM。

## AesLegacy 与生命周期

`new AesLegacy(keyString).encrypt(text)` 返回大写 hex；`decrypt(hex)` 返回 UTF-8。实际构造函数选择 **`AES/ECB/PKCS5Padding`**，并非旧注释所述的裸 `AES` 转换。它通过旧 `SecretKeyMgr.getRandom` 种子随机机制派生 AES-128，不是可移植的密码 KDF。仅用于验证过提供者兼容性的历史数据；ECB 泄漏重复块且没有认证。

所有封装每次调用都会使用新 cipher 引擎。结果数组与 nonce 容器可变、未复制，详见 [DoCipher/CipherResult](/aj-util/cryptography/flow-cn/)。它们不实现密钥轮换、安全存储、消息封装格式或自动销毁。测试依据：`aes/TestAes`、`aes/TestAesCipherResult` 和 `TestDoCipher` 覆盖参数拒绝、格式、AAD 及篡改/错误密码失败；测试中的固定/全零密钥不能用于生产。
