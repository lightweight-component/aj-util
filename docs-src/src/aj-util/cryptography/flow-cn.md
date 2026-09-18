---
title: 加解密流程与密钥处理
layout: layouts/aj-util-cn.njk
---

# DoCipher 与 CipherResult

`new DoCipher(transformation, Key)` 保存配置，不保存输入或 JCE cipher 引擎。每次操作创建新的 `Cipher`。当前 API 没有 mode/data setter，也没有无参数 `doCipher()`。

```java
import com.ajaxjs.util.cryptography.CipherResult;
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

SecretKey key = SecretKeyMgr.getSecretKey("AES", 128, new SecureRandom());
DoCipher cipher = new DoCipher("AES/GCM/NoPadding", key);
byte[] nonce = DoCipher.randomBytes(12);
byte[] aad = "record:v1".getBytes(StandardCharsets.UTF_8);
GCMParameterSpec parameters = new GCMParameterSpec(128, nonce);
CipherResult encrypted = cipher.doCipher(Cipher.ENCRYPT_MODE, "hello", parameters, aad);
CipherResult decrypted = cipher.doCipherFromBase64(
        Cipher.DECRYPT_MODE, encrypted.toBase64(), parameters, aad);
String plaintext = decrypted.toUtf8();
```

上面的 nonce **只能用于一次加密**。需要自动生成 nonce 时使用 [AesGcm](/aj-util/cryptography/Cryptography-cn/)。通用 `DoCipher` 不打包或返回提供者自动生成的 IV；请显式指定所需参数并自行保存。

## 输入、输出与错误

- `doCipher(int, byte[], AlgorithmParameterSpec, byte[])` 处理原始字节；String 重载按 UTF-8 编码。`doCipherFromBase64(...)` 先解码二进制输入。不支持 AAD 的算法传 null。
- `CipherResult.getResult()` 返回原始字节；`toBase64()` 和大写 `toHex()` 用于表示二进制密文。`toUtf8()` 用于解密后的 UTF-8 明文，不适合任意密文。
- 结果数组**没有防御性复制**。`AesCipherResult` 还有可变的 `CipherResult nonce` 属性，手动创建时初始为 null。不要修改共享输入/结果数组，也不要把这些容器当成不可变安全记录。
- 转换名为空、模式不是 ENCRYPT/DECRYPT、密钥或 byte 输入为 null，抛出 `IllegalStateException`。String/Base64 输入为 null 时抛出 `NullPointerException`。
- 不支持的转换、非法密钥/参数、块长度、填充和 GCM 认证失败包装为带 cause 的 `IllegalArgumentException`。认证失败必须停止处理，不得接受未认证输出或回退弱模式。

与旧的有状态 API 不同，`DoCipher` 和 AES 封装每次调用创建新引擎，只保留密钥/配置。但不能据此对调用方传入的 Key、可变结果或数组做全面线程安全保证。没有自动销毁密钥功能，应控制密钥生命周期，禁止记录秘密信息。

## SecretKeyMgr

`getSecretKey("AES", bits, secureRandom)` 生成随机密钥。正数显式初始化长度；当前方法体对零**或负数**都使用提供者默认长度（不能假定负数会被拒绝）。`getSecretKey(SecureRandom)` 选择 AES-128。原始 AES 密钥通常为 16/24/32 字节，使用时由提供者校验。

`getSecretKey(algorithm, KeySpec)` 委托给 `SecretKeyFactory`；`getSecretKey(KeySpec)` 选择 `PBKDF2WithHmacSHA256`。原始 AES 字节使用 `new SecretKeySpec(bytes, "AES")` 或 AES 封装类，不要把普通密码字符串当作原始密钥。`getSecretKeyAsStr(algorithm, bits, random)` **生成另一个新密钥**并返回 Base64，不是编码先前生成的密钥。不可导出的密钥不能这样编码。

`getRandom(algorithm, text)` / `getRandom(text)`（默认 `SHA1PRNG`）以 UTF-8 文本调用 `SecureRandom.setSeed`。这只是补充内部状态，**不是**可移植、确定性的密码 KDF。密码加密使用 `AesPbe`。`DoCipher.randomBytes(length)` 使用 `RandomTools.RANDOM`（`SecureRandom`），要求长度为正；其他 `RandomTools` 随机字符串/数字方法不能替代它。

依据：`DoCipher`、`CipherResult`、`SecretKeyMgr`、`aes/AesCipherResult`、`TestDoCipher`、`TestCipherResult`、`TestSecretKeyMgr`。方法体优先于过时的异常注释，尤其是负密钥长度的处理。
