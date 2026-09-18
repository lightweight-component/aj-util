---
title: 密码学概览
layout: layouts/aj-util-cn.njk
---

# 密码学

AJ Util 对 JCA/JCE 做轻量封装，显式配置密钥并在每次操作时传入数据。当前源码已替换原来的单体 `Cryptography` 和 `rsa.KeyMgr` API；文档保留原有 URL 以兼容书签。

| 包 | 当前 API | 用途 |
| --- | --- | --- |
| `com.ajaxjs.util.cryptography` | `DoCipher`、`CipherResult`、`SecretKeyMgr`、`CertificateUtils` | 通用加解密、结果编码、对称密钥、证书解析 |
| `com.ajaxjs.util.cryptography.aes` | `AesGcm`、`AesCbc`、`AesPbe`、`AesLegacy`、`AesCipherResult` | 认证加密、兼容模式、基于密码的密钥派生 |
| `com.ajaxjs.util.cryptography.rsa` | `Rsa`、`DoSignature`、`DoVerify`、`RestoreKey`、`PemUtils` | 小数据 RSA-OAEP、签名、密钥恢复与 PEM 编码 |

- 新的加密数据优先使用 **AES-GCM**。保存密文/tag、nonce 和完全一致的 AAD；同一密钥绝不能重复使用 nonce。
- 需要从密码派生密钥时使用 **AesPbe**。盐、迭代次数、格式/版本元数据须在带 nonce 前缀的密文之外单独保存。
- RSA 用于会话密钥等小数据，不适合大文件。签名验证用于认证数据，不是解密。
- 摘要（`HashHelper`）和 Base64/hex 编码不是加密。摘要可能碰撞；新安全设计不推荐 MD5/SHA-1。不要仅用快速摘要存储密码。
- CBC 不提供认证，旧 ECB 仅用于历史互通。`TestCryptographyLegacy` 中的 DES/3DES 和旧 PBE 示例是测试内部辅助方法，不是当前库的便捷 API。

阅读[加解密流程与密钥处理](/aj-util/cryptography/flow-cn/)、[AES/PBE](/aj-util/cryptography/Cryptography-cn/) 和 [RSA、PEM 与证书](/aj-util/cryptography/Rsa-cn/)。
