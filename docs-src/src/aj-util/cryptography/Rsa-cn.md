---
title: RSA、签名、PEM 与证书
layout: layouts/aj-util-cn.njk
---

# RSA 密钥与加密

`com.ajaxjs.util.cryptography.rsa.Rsa` 是一个**绑定密钥**的 RSA cipher：使用接收方公钥创建实例用于加密，使用对应私钥创建另一个实例用于解密。它取代了 `RsaDefault` 和旧的有状态 `Rsa` API；密钥对生成现归属 `RestoreKey`。

```java
import com.ajaxjs.util.cryptography.rsa.PemUtils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;

KeyPair pair = RestoreKey.generateKeyPair(2048);
String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());

String ciphertextBase64 = new Rsa(publicPem, true)
        .encrypt("small secret")
        .toBase64();
String plaintext = new Rsa(privatePem, false).decrypt(ciphertextBase64);
```

默认 transformation 是 `RSA/ECB/OAEPWithSHA-256AndMGF1Padding`：OAEP 摘要和 MGF1 摘要都显式使用 SHA-256，OAEP label 为空。两端必须使用相同的 transformation 和参数。绑定公钥的实例只能加密，绑定私钥的实例只能解密；该 API 不能代替签名。

`encrypt(String)` 按 UTF-8 编码文本，并返回二进制 `CipherResult`。因此 Base64 才是通常的传输/存储形式：`encrypt(...).toBase64()` 与 `decrypt(String)` 相对应。二进制内容使用 `encrypt(byte[])`，通过 `decryptToResult(byte[])` 还原，`CipherResult.getResult()` 取得还原后的字节。若调用方必须声明密文字符串是否为 Base64，可使用 `decryptToResult(String, boolean)`。避免使用 `decryptRawUtf8Ciphertext(String)`：任意密文字节通常不是合法 UTF-8，该方法只为一种遗留表示形式保留。

OAEP-SHA256 的明文上限为 `modulusBytes - 2*32 - 2`：**2048 位密钥时为 190 字节**。这是字节数，不是字符数，且不会自动分块。文件或其他大负载应采用混合方案：用 AES-GCM 加密正文，RSA 仅加密短的随机 AES 密钥。非法密文、超长明文、错误密钥或填充不匹配都会失败，不会返回部分明文。

## 兼容算法与证书

只有当外部协议明确要求 OAEP SHA-1 时，才在**加解密两端**选择公开的兼容常量：

```java
String ciphertextBase64 = new Rsa(Rsa.RSA_CIPHER_SHA1, publicPem, true)
        .encrypt("protocol value")
        .toBase64();
String value = new Rsa(Rsa.RSA_CIPHER_SHA1, privatePem, false)
        .decrypt(ciphertextBase64);
```

该模式显式以 SHA-1 用于 OAEP 和 MGF1，仅用于协议兼容，不是新集成的首选设置。其他自定义 transformation 构造函数只接受 `Rsa` 已登记的算法，因而仍会使用匹配的 OAEP 参数；若确实需要不同的 JCA transformation 或 OAEP label，应直接使用 `DoCipher`。

`new Rsa(X509Certificate)` 适合在对方以证书提供 RSA 公钥时使用。构造函数只读取公钥；解析证书或据此创建 `Rsa` 都不会验证证书链、吊销状态、主机名、预期用途或来源真实性。

## 签名与验签

RSA 加密和签名是不同的 JCA 操作。使用私钥 `DoSignature` 签名，使用相应公钥 `DoVerify` 验签：

```java
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;

String signature = new DoSignature(pair.getPrivate()).signToBase64("message");
boolean valid = new DoVerify(pair.getPublic()).verify("message", signature);
if (!valid) {
    throw new SecurityException("Invalid signature");
}
```

默认签名算法为 `SHA256withRSA`——JCA RSA 签名，不是 RSA cipher，也不是 RSA-PSS。构造函数接受相应的 Key 对象或 PEM/Base64 密钥文本；全参构造函数形如 `(String algorithmName, PrivateKey/PublicKey)`。`new DoSignature(String)` 中的 String 被解释为私钥，不是算法名。

`sign(byte[]/String)` 返回原始字节，`signToBase64(byte[]/String)` 返回文本。`verify` 接受字节或 UTF-8 文本，以及原始或 Base64 签名。正常不匹配返回 `false`；畸形签名、Base64、密钥或算法可能抛出异常，绝不能把异常当作验签通过。

## RestoreKey 与 PemUtils

```java
import com.ajaxjs.util.cryptography.rsa.PemUtils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import java.security.PrivateKey;
import java.security.PublicKey;

PublicKey publicKey = RestoreKey.restorePublicKey(publicPem);
PrivateKey privateKey = RestoreKey.restorePrivateKey(privatePem);
```

- `RestoreKey.generateKeyPair(int)` 仅接受 **2048**、**3072** 或 **4096** 位。该限制约束新生成的密钥，并不检查每一个恢复密钥的最低强度。
- `restorePublicKey(String)` 接受 Base64 或 `BEGIN PUBLIC KEY` PEM，其内容为 X.509 **SubjectPublicKeyInfo**。这是公钥结构，不是 X.509 证书。
- `restorePrivateKey(String)` 接受未加密的 Base64 或 `BEGIN PRIVATE KEY` PEM，其内容为 **PKCS#8**。会去除空白；PKCS#1 的 `RSA PUBLIC KEY` / `RSA PRIVATE KEY` 以及加密私钥 PEM 被明确拒绝。
- `loadPrivateKey(InputStream)` 默认 UTF-8，其读流路径会关闭传入的 input。String 重载读取文件路径。
- `PemUtils` 为可导出的密钥生成 Base64 或每行 64 列的 PEM。它只封装文本，不验证 DER，也不将 PKCS#1 转换为 PKCS#8。PEM/Base64 私钥文本仍是敏感材料：须妥善保存，禁止记录日志。

## 证书

`com.ajaxjs.util.cryptography.CertificateUtils.getCert(String path/InputStream)` 解析 PEM/DER X.509，并调用 `checkValidity()`；传入的流会被关闭。非法、过期或尚未生效的证书会导致异常。解析和日期校验**不会**验证信任链、吊销、主机名、预期用途或来源真实性。使用证书公钥前须另外建立这些信任属性。

`deserializeToCerts(String apiV3Key, Map<String,Object> response)` 是特定协议的辅助方法。其 key 必须编码为 **32 个 UTF-8 字节**（不是 Base64），其 `data` 必须包含 `encrypt_certificate` Map，其中有 Base64 密文、UTF-8 nonce 与 associated data。它通过 `AesGcm` 解密，解析并检查证书日期，以序列号（`BigInteger`）为键返回证书；它不是通用的证书信任校验器。

依据：`Rsa`、`DoSignature`、`DoVerify`、`RestoreKey`、`PemUtils`、`CertificateUtils`；`rsa/TestRsa`、`rsa/TestVerify`、`rsa/TestRestoreKey`、`rsa/TestPemUtils`、`TestCertificateUtils`。
