---
title: RsaCrypto
subTitle: 2025-02-23 by Frank Cheung
description: RsaCrypto
date: 2025-02-23
tags:
  - RsaCrypto
layout: layouts/aj-util.njk
---

# RsaCrypto Tutorial

This tutorial provides an overview of the `RsaCrypto` class, which is part of the `lightweight-component/aj-util` library. `RsaCrypto` offers a collection of utility methods for asymmetric encryption, decryption, and digital signatures using the RSA algorithm.

## Introduction

The `RsaCrypto` class provides methods for generating RSA key pairs, encrypting and decrypting data using public and private keys, and creating and verifying digital signatures. RSA is an asymmetric cryptographic algorithm that uses a pair of keys: a public key for encryption and a private key for decryption.

## Methods

### 1. `init()`

Initializes and returns an RSA key pair with a key size of 1024 bits.

* **Returns:** A map containing the public and private keys as byte arrays.

**Example:**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
```

### 2. `getPublicKey(Map<String, byte[]> map)` and `getPrivateKey(Map<String, byte[]> map)`

Retrieves the public or private key from a key pair map as a Base64 encoded string.

* **Parameters:**
  * `map`: The map containing the key pair.
* **Returns:** The Base64 encoded string representation of the key.

**Example:**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String publicKey = RsaCrypto.getPublicKey(keyPair);
String privateKey = RsaCrypto.getPrivateKey(keyPair);
```

### 3. `sign(String privateKey, byte[] data)`

Creates a digital signature for the provided data using the private key.

* **Parameters:**
  * `privateKey`: The Base64 encoded private key.
  * `data`: The data to sign.
* **Returns:** The Base64 encoded digital signature.

**Example:**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String privateKey = RsaCrypto.getPrivateKey(keyPair);
String signature = RsaCrypto.sign(privateKey, "Hello, World!".getBytes());
```

### 4. `verify(byte[] data, String publicKey, String sign)`

Verifies a digital signature using the public key.

* **Parameters:**
  * `data`: The original data.
  * `publicKey`: The Base64 encoded public key.
  * `sign`: The Base64 encoded signature to verify.
* **Returns:** `true` if the signature is valid, `false` otherwise.

**Example:**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String publicKey = RsaCrypto.getPublicKey(keyPair);
String privateKey = RsaCrypto.getPrivateKey(keyPair);

byte[] data = "Hello, World!".getBytes();
String signature = RsaCrypto.sign(privateKey, data);

boolean isValid = RsaCrypto.verify(data, publicKey, signature);
// isValid should be true
```

### 5. Public Key Operations: `encryptByPublicKey(byte[] data, String key)` and `decryptByPublicKey(byte[] data, String key)`

Encrypts or decrypts data using the public key.

* **Parameters:**
  * `data`: The data to encrypt/decrypt.
  * `key`: The Base64 encoded public key.
* **Returns:** The encrypted/decrypted data as a byte array.

**Example:**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String publicKey = RsaCrypto.getPublicKey(keyPair);

byte[] data = "Hello, World!".getBytes();
byte[] encrypted = RsaCrypto.encryptByPublicKey(data, publicKey);
```

### 6. Private Key Operations: `encryptByPrivateKey(byte[] data, String key)` and `decryptByPrivateKey(byte[] data, String key)`

Encrypts or decrypts data using the private key.

* **Parameters:**
  * `data`: The data to encrypt/decrypt.
  * `key`: The Base64 encoded private key.
* **Returns:** The encrypted/decrypted data as a byte array.

**Example:**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String privateKey = RsaCrypto.getPrivateKey(keyPair);
String publicKey = RsaCrypto.getPublicKey(keyPair);

// Encrypt with public key
byte[] data = "Hello, World!".getBytes();
byte[] encrypted = RsaCrypto.encryptByPublicKey(data, publicKey);

// Decrypt with private key
byte[] decrypted = RsaCrypto.decryptByPrivateKey(encrypted, privateKey);
// decrypted should be equal to the original data
```

## Conclusion

The `RsaCrypto` class provides a comprehensive set of tools for RSA asymmetric encryption, decryption, and digital signatures. It offers methods for key pair generation, data encryption/decryption using both public and private keys, and signature creation/verification. Remember to store private keys securely and follow best practices for cryptographic implementations.