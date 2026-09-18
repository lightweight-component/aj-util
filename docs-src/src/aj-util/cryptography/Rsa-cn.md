---
title: RSA、签名、PEM 与证书
layout: layouts/aj-util-cn.njk
---

# RSA 密钥与加密

使用 `com.ajaxjs.util.cryptography.rsa.Rsa`，而不是已移除的 `KeyMgr`。`generateKeyPair(int)` 仅接受 **2048、3072 或 4096** 位。此限制只用于生成，并不意味着每个恢复/导入的密钥都会检查最低强度。

```java
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;

KeyPair pair = Rsa.generateKeyPair(2048);
byte[] encrypted = new Rsa("small secret").encryptWithPublicKey(pair.getPublic());
String plaintext = new Rsa(encrypted).decryptToString(pair.getPrivate());
```

主要方法使用 `RSA/ECB/OAEPWithSHA-256AndMGF1Padding`，显式指定 **OAEP 和 MGF1 均使用 SHA-256**，label 为空。公钥加密、私钥解密。它不是旧的提供者默认 RSA/PKCS#1 方案，也不是以私钥“加密”模拟签名的 API。

`Rsa(byte[])` 复制输入。`Rsa(String)` 按 UTF-8 编码，**不会 Base64 解码密文**。`encryptWithPublicKeyToBase64(...)` 返回 Base64；创建解密用 `Rsa` 前须将其解码为字节（如 `new Base64Utils(text).decode()`）。加解密方法接受 Key 对象或支持的 PEM/Base64 密钥字符串。`decryptWithPrivateKey(...)` 返回原始字节，`decryptToString(...)` 仅适用于 UTF-8 明文。

OAEP-SHA256 输入上限为 `modulusBytes - 2*32 - 2`：**2048 位密钥为 190 字节**，不是 190 个字符。没有分块处理。大数据应使用明确设计的混合协议：AES-GCM 加密正文，RSA 保护短对称密钥。超长输入、填充不匹配或非法密文/密钥会失败，不会返回部分明文。

独立静态方法 `Rsa.encryptOAEP(String, X509Certificate)` / `decryptOAEP(String base64Ciphertext, PrivateKey)` 为外部协议兼容（如微信支付）使用 **OAEP-SHA1**。它们不能与主要 SHA-256 方法互换，也不验证证书信任。

## 签名与验签

```java
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;

KeyPair pair = Rsa.generateKeyPair(2048);
String signature = new DoSignature(pair.getPrivate()).signToBase64("message");
boolean valid = new DoVerify(pair.getPublic()).verify("message", signature);
if (!valid) {
    throw new SecurityException("Invalid signature");
}
```

默认算法为 `SHA256withRSA`（JCA 签名，不是 RSA cipher，也不是 RSA-PSS）。构造函数接受对应 Key 对象或 PEM/Base64 密钥字符串；生成的全参构造函数接受 `(String algorithmName, PrivateKey/PublicKey)`。`new DoSignature(String)` 把 String 解释为**私钥**，不是算法名。

`sign(byte[]/String)` 返回字节；`signToBase64(byte[]/String)` 返回文本。`verify` 接受数据字节或 UTF-8 文本，以及签名字节或 Base64。每次调用创建新的 `Signature`，没有链式输入/结果 setter。普通不匹配返回 false，但畸形签名可能引发 `IllegalStateException`（提供者 `SignatureException`）；非法 Base64/算法/密钥可能引发 `IllegalArgumentException`。null 行为随重载不同：签名 byte 输入/缺失密钥在签名时用 `IllegalStateException` 检查；null String 签名输入产生 `NullPointerException`；验签 byte 输入/签名校验使用 `IllegalArgumentException`。异常应按验签失败处理，不能视为通过。

## RestoreKey 与 PemUtils

```java
import com.ajaxjs.util.cryptography.rsa.PemUtils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

KeyPair pair = Rsa.generateKeyPair(2048);
String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());
PublicKey publicKey = RestoreKey.restorePublicKey(publicPem);
PrivateKey privateKey = RestoreKey.restorePrivateKey(privatePem);
```

- `restorePublicKey(String)` 接受 X.509 **SubjectPublicKeyInfo** DER 的 Base64，可使用 `BEGIN PUBLIC KEY` PEM 边界。这是密钥结构，不是 X.509 证书。
- `restorePrivateKey(String)` 接受未加密的 **PKCS#8** DER/Base64 或 `BEGIN PRIVATE KEY` PEM，并移除空白。显式拒绝 PKCS#1 `RSA PUBLIC KEY` / `RSA PRIVATE KEY`；不支持加密私钥 PEM。
- 格式错误抛 `IllegalArgumentException`，null 输入抛 `NullPointerException`，RSA factory 不可用抛 `IllegalStateException`。
- `loadPrivateKey(InputStream)` 默认 UTF-8；字符集重载接受字符集名称。其 `DataWriter`/`DataReader` 路径**会关闭传入输入流**。String 重载加载文件路径。读取失败可能抛出非受检 I/O 异常。
- `PemUtils.encodeKeyBase64(Key)` 编码 `getEncoded()`。PEM 辅助方法以 64 列折行封装公私钥，也接受 Base64 String；不验证 DER，不把 PKCS#1 转为 PKCS#8。编码工具不支持不可导出的密钥。私钥 PEM/Base64 仍是秘密明文密钥材料，须保护存储，禁止记录日志。

## 证书

`com.ajaxjs.util.cryptography.CertificateUtils.getCert(String path/InputStream)` 解析 PEM/DER X.509 并调用 `checkValidity()`，会关闭传入流。非法/过期/尚未生效的证书产生 `RuntimeException`；文件/流 I/O 错误可能产生 `UncheckedIOException`。解析和日期检查**不验证**信任链、吊销、主机名、用途或来源真实性。使用证书公钥前须单独建立信任。

`deserializeToCerts(String apiV3Key, Map<String,Object> response)` 是特定协议工具：key 必须编码为 **32 个 UTF-8 字节**（不是 Base64）；`data` 必须为列表，每项包含 `encrypt_certificate` Map，字段为 `ciphertext`、`nonce`、`associated_data`。密文为 Base64；nonce 和 AAD 为 UTF-8 字符串（移除外围引号），GCM 要求 12 字节 nonce。该方法用 `AesGcm` 解密，解析并检查证书日期，返回以实际证书序列号（`BigInteger`）为键的 Map。缺失字段/非法参数可能抛 `IllegalArgumentException`，错误嵌套类型可能抛 `ClassCastException`。它不是通用证书信任验证器。

依据：`Rsa`、`DoSignature`、`DoVerify`、`RestoreKey`、`PemUtils`、`CertificateUtils`；`rsa/TestRsa`、`rsa/TestVerify`、`rsa/TestRestoreKey`、`rsa/TestPemUtils`、`TestCertificateUtils`。现有证书测试夹具的有效期随时间变化，不要把旧夹具或网络响应当成当前可信证书。
