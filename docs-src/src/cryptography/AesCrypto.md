---
title: AesCrypto
subTitle: 2025-02-23 by Frank Cheung
description: AesCrypto
date: 2025-02-23
tags:
  - AesCrypto
layout: layouts/aj-util.njk
---
# AesCrypto Tutorial

This tutorial provides an overview of the `AesCrypto` class, which is part of the `lightweight-component/aj-util` library. `AesCrypto` offers a collection of utility methods for symmetric encryption and decryption using algorithms like AES, DES, 3DES, and PBE.

## Introduction

The `AesCrypto` class provides methods for encrypting and decrypting data using various symmetric encryption algorithms. It implements the Singleton pattern and offers a comprehensive set of tools for secure data encryption.

## Methods

### 1. `getInstance()`

Returns the singleton instance of the `AesCrypto` class.

* **Returns:** The singleton instance of `AesCrypto`.

**Example:**

```java
AesCrypto crypto = AesCrypto.getInstance();
```

### 2. `getSecretKey(String algorithmName, SecureRandom secure)`

Generates a secret key for the specified algorithm using the provided secure random generator and returns it as a Base64 encoded string.

* **Parameters:**
  * `algorithmName`: The name of the encryption algorithm.
  * `secure`: The secure random generator.
* **Returns:** A Base64 encoded string representation of the secret key.

**Example:**

```java
SecureRandom secureRandom = new SecureRandom();
String secretKey = AesCrypto.getSecretKey("AES", secureRandom);
```

### 3. `DES_encode(String res, String key)` and `DES_decode(String res, String key)`

Encrypts or decrypts data using the DES algorithm.

* **Parameters:**
  * `res`: The data to encrypt/decrypt.
  * `key`: The encryption/decryption key.
* **Returns:** The encrypted/decrypted data.

**Example:**

```java
AesCrypto crypto = AesCrypto.getInstance();
String encrypted = crypto.DES_encode("Hello, World!", "mySecretKey");
String decrypted = crypto.DES_decode(encrypted, "mySecretKey");
```

### 4. `AES_encode(String res, String key)` and `AES_decode(String res, String key)`

Encrypts or decrypts data using the AES algorithm with a 128-bit key size.

* **Parameters:**
  * `res`: The data to encrypt/decrypt.
  * `key`: The encryption/decryption key.
* **Returns:** The encrypted/decrypted data.

**Example:**

```java
AesCrypto crypto = AesCrypto.getInstance();
String encrypted = crypto.AES_encode("Hello, World!", "mySecretKey");
String decrypted = crypto.AES_decode(encrypted, "mySecretKey");
```

### 5. `encryptTripleDES(byte[] key, String data)` and `decryptTripleDES(byte[] key, byte[] data)`

Encrypts or decrypts data using the Triple DES (3DES) algorithm.

* **Parameters:**
  * `key`: The encryption/decryption key (24 bytes for Triple DES).
  * `data`: The data to encrypt/decrypt.
* **Returns:** The encrypted/decrypted data.

**Example:**

```java
byte[] key = new byte[24]; // Generate or obtain a 24-byte key
new SecureRandom().nextBytes(key);

byte[] encrypted = AesCrypto.encryptTripleDES(key, "Hello, World!");
String decrypted = AesCrypto.decryptTripleDES(key, encrypted);
```

### 6. `initSalt()`, `encryptPBE(String key, byte[] salt, String data)`, and `decryptPBE(String key, byte[] salt, byte[] data)`

Methods for Password-Based Encryption (PBE).

* **initSalt()**: Generates a random 8-byte salt.
* **encryptPBE()**: Encrypts data using PBE with the provided key and salt.
* **decryptPBE()**: Decrypts data using PBE with the provided key and salt.

**Example:**

```java
byte[] salt = AesCrypto.initSalt();
String key = "mySecretPassword";

byte[] encrypted = AesCrypto.encryptPBE(key, salt, "Hello, World!");
String decrypted = AesCrypto.decryptPBE(key, salt, encrypted);
```

## Conclusion

The `AesCrypto` class provides a comprehensive set of tools for symmetric encryption and decryption. It supports multiple algorithms (AES, DES, 3DES, PBE) and offers a simple interface for secure data handling. Remember to store encryption keys securely and follow best practices for cryptographic implementations.