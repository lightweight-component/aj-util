---
title: Basic Process
description: Basic Process
tags:
  - Encoding
  - Hashing
  - Cryptography
layout: layouts/aj-util.njk
---

# Basic Process

The most primitive expression of encryption is `ciphertext = encryption_function(plaintext)`. Here, let's also mention MD5, which conforms to the definition of `ciphertext = encryption_function(plaintext)`. It appears to encrypt the input, but actually it is a type of hash function, which is completely different from encryption. More accurately, it returns a "characteristic" result of the input parameter, and this characteristic is unique. Since the same result is returned each time it is executed, a dictionary table can be constructed for comparison. As long as the dictionary table is large enough, MD5 is very insecure.

Therefore, we modify this encryption process by adding the key parameter, hoping that different keys will produce different ciphertexts each time, i.e., `ciphertext = encryption_function(plaintext, key)`.

AES (Advanced Encryption Standard) symmetric encryption also conforms to this original process. A rough Java API implementation is as follows:

```java
/**
 * Is it decryption mode or encryption mode?
 */
private final int mode;

/**
 * The name of the algorithm
 */
private final String algorithmName;

/**
 * Key
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
        throw new RuntimeException("The length of the encrypted string cannot exceed 214 bytes", e);
    } catch (InvalidKeyException e) {
        throw new IllegalArgumentException("Invalid Key.", e);
    } catch (InvalidAlgorithmParameterException e) {
        throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
    }
}
```


Firstly, we can see that we didn't adopt a functional style to define `doCipher()`, but rather used getters/setters for parameters (with Lombok generating them beforehand). These parameters are all the basic primitive data types required for `doCipher()` execution. Secondly, let's abstract the main process:

1. Determine which algorithm: AES or DES or RSA?
2. Determine decryption mode or encryption mode?
3. Pass in the key (is it `byte[]`? Or Java `Key` object?), and additionally, is `AlgorithmParameterSpec spec` parameter needed?
4. Input data (is it `byte[]`? Or String or Base64 String?), then execute encryption/decryption
5. The raw return is `byte[]`, so does the caller want to return String directly, or Base64 String, or HexString?

It seems that the core data type of the encryption/decryption process is `byte[]`. Regardless of which type of input parameter is used, it must ultimately be converted to `byte[]`. The more input parameter types we consider, the more convenient it becomes (no need for API callers to manually convert), and accordingly, we need to arrange more setters for conversion.

The general coding style approach is determined, and then we can proceed with coding.