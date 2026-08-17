package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.Cryptography;
import com.ajaxjs.util.io.DataWriter;
import com.ajaxjs.util.io.FileHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Generates, encodes, restores, loads, and applies RSA public and private keys.
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Data
public class KeyMgr implements Constant {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The key size, required. Like 1024, 2048, 4096.
     */
    private final int keySize;

    /**
     * The result of generating a key pair
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KeyPair keyPair;

    /**
     * Get a pair of keys: public key and private key
     *
     * @return Key pair object
     * @throws IllegalArgumentException if the key size is not 2048, 3072, or 4096 bits
     * @throws RuntimeException if the requested key-pair algorithm is unavailable
     */
    public KeyPair generateKeyPair() {
        if (keySize == 2048 || keySize == 3072 || keySize == 4096)
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName);
                generator.initialize(keySize);
                keyPair = generator.generateKeyPair();

                return keyPair;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
            }
        else
            throw new IllegalArgumentException("RSA key size must be 2048, 3072, or 4096 bits: " + keySize);
    }

    /**
     * Returns the encoded public-key bytes.
     *
     * @return the encoded public-key bytes
     * @throws IllegalStateException if a key pair has not been generated
     */
    public byte[] getPublicKeyBytes() {
        requireGeneratedKeyPair();
        return keyPair.getPublic().getEncoded();
    }

    /**
     * Returns the encoded private-key bytes.
     *
     * @return the encoded private-key bytes
     * @throws IllegalStateException if a key pair has not been generated
     */
    public byte[] getPrivateKeyBytes() {
        requireGeneratedKeyPair();

        return keyPair.getPrivate().getEncoded();
    }

    /**
     * Ensures that a key pair has been generated before accessing its keys.
     *
     * @throws IllegalStateException if no key pair has been generated
     */
    void requireGeneratedKeyPair() {
        if (keyPair == null)
            throw new IllegalStateException("Key pair has not been generated.");
    }

    /**
     * Returns the public key as Base64.
     *
     * @return the Base64-encoded public key
     * @throws IllegalStateException if a key pair has not been generated
     */
    public String getPublicKeyStr() {
        return new Base64Utils(getPublicKeyBytes()).encodeAsString();
    }

    /**
     * Returns the public key in PEM format.
     *
     * @return the PEM-encoded public key
     * @throws IllegalStateException if a key pair has not been generated
     */
    public String getPublicToPem() {
        return publicKeyToPem(getPublicKeyStr());
    }

    /**
     * Returns the private key as Base64.
     *
     * @return the Base64-encoded private key
     * @throws IllegalStateException if a key pair has not been generated
     */
    public String getPrivateKeyStr() {
        return new Base64Utils(getPrivateKeyBytes()).encodeAsString();
    }

    /**
     * Returns the private key in PEM format.
     *
     * @return the PEM-encoded private key
     * @throws IllegalStateException if a key pair has not been generated
     */
    public String getPrivateToPem() {
        return privateKeyToPem(getPrivateKeyStr());
    }

    /* ------------------------- Restore Key ------------------------ */

    /**
     * Restores an RSA public or private key from Base64 or PEM text.
     *
     * @param isPublic {@code true} to restore a public key; {@code false} to restore a private key
     * @param key      the Base64- or PEM-encoded key
     * @return the restored public or private key
     * @throws IllegalArgumentException if the key encoding is invalid
     * @throws RuntimeException if RSA is unavailable
     */
    public static Key restoreKey(boolean isPublic, String key) {
        // auto removes the pem
        if (isPublic)
            key = key.replaceAll("-----\\w+ PUBLIC KEY-----", CommonConstant.EMPTY_STRING);
        else
            key = key.replaceAll("-----\\w+ PRIVATE KEY-----", CommonConstant.EMPTY_STRING);

        key = key.replaceAll("\\s", CommonConstant.EMPTY_STRING);

        byte[] bytes = new Base64Utils(key).decode();

        try {
            KeyFactory f = KeyFactory.getInstance(RSA);

            return isPublic ? f.generatePublic(new X509EncodedKeySpec(bytes)) : f.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid RSA key encoding.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + RSA, e);
        }
    }

    /**
     * Restores a private key from its Base64/PEM-encoded string representation.
     *
     * @param key the Base64 or PEM-encoded private key string
     * @return the restored private key
     * @throws IllegalArgumentException if the key encoding is invalid
     * @throws RuntimeException if RSA is unavailable
     */
    public static PrivateKey restorePrivateKey(String key) {
        Key _key = restoreKey(false, key);

        return (PrivateKey) _key;
    }

    /* ------------------------- PEM ------------------------ */

    /**
     * Converts a private key to PEM text.
     *
     * @param privateKey the private key to convert
     * @return the PEM-encoded private key
     * @throws NullPointerException if the private key is null
     */
    @Deprecated
    public static String privateKeyToPem(PrivateKey privateKey) {
        String encoded = new Base64Utils(privateKey.getEncoded()).encodeAsString();

        return privateKeyToPem(encoded);
    }

    /**
     * Wraps a Base64-encoded private key in PEM boundaries.
     *
     * @param encoded the Base64-encoded private key
     * @return the PEM-encoded private key
     * @throws NullPointerException if the encoded key is null
     */
    public static String privateKeyToPem(String encoded) {
        return "-----BEGIN PRIVATE KEY-----\n" +
                Base64Utils.formatBase64String(encoded) +
                "\n-----END PRIVATE KEY-----";
    }

    /**
     * Converts a public key to PEM text.
     *
     * @param publicKey the public key to convert
     * @return the PEM-encoded public key
     * @throws NullPointerException if the public key is null
     */
    @Deprecated
    public static String publicKeyToPem(PublicKey publicKey) {
        String encoded = new Base64Utils(publicKey.getEncoded()).encodeAsString();

        return publicKeyToPem(encoded);
    }

    /**
     * Wraps a Base64-encoded public key in PEM boundaries.
     *
     * @param encoded the Base64-encoded public key
     * @return the PEM-encoded public key
     * @throws NullPointerException if the encoded key is null
     */
    public static String publicKeyToPem(String encoded) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64Utils.formatBase64String(encoded) +
                "\n-----END PUBLIC KEY-----";
    }

    /* ------------------------- encrypt/decrypt ------------------------ */

    /**
     * Applies an RSA cipher operation with a public or private key.
     *
     * @param isEncrypt {@code true} to encrypt; {@code false} to decrypt
     * @param isPublic  {@code true} to use a public key; {@code false} to use a private key
     * @param data      the input bytes
     * @param key       the Base64- or PEM-encoded key
     * @return the cipher result
     * @throws IllegalArgumentException if the key or input is invalid
     * @throws RuntimeException if RSA is unavailable
     */
    static byte[] action(boolean isEncrypt, boolean isPublic, byte[] data, String key) {
        int mode = isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        Cryptography cryptography = new Cryptography(RSA, mode);
        cryptography.setKey(restoreKey(isPublic, key));
        cryptography.setData(data);

        return cryptography.doCipher();
    }

    /**
     * Encrypts data with an RSA public key.
     *
     * @param data the plaintext bytes
     * @param key  the public key
     * @return the encrypted bytes
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static byte[] publicKeyEncrypt(byte[] data, String key) {
        return action(true, true, data, key);
    }

    /**
     * Encrypts data with the given public key and returns the result as a Base64 string.
     *
     * @param data the plaintext bytes
     * @param key  the public key string
     * @return the Base64-encoded encrypted data
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static String publicKeyEncryptAsBase64Str(byte[] data, String key) {
        return new Base64Utils(publicKeyEncrypt(data, key)).encodeAsString();
    }

    /**
     * Decrypts data with an RSA public key for legacy compatibility.
     *
     * @param data the encrypted bytes
     * @param key  the public key
     * @return the decrypted bytes
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static byte[] publicKeyDecrypt(byte[] data, String key) {
        return action(false, true, data, key);
    }

    /**
     * Encrypts data with an RSA private key for legacy compatibility.
     *
     * @param data the plaintext bytes
     * @param key  the private key
     * @return the encrypted bytes
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static byte[] privateKeyEncrypt(byte[] data, String key) {
        return action(true, false, data, key);
    }

    /**
     * Decrypts data with an RSA private key.
     *
     * @param data the encrypted bytes
     * @param key  the private key
     * @return the decrypted bytes
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static byte[] privateKeyDecrypt(byte[] data, String key) {
        return action(false, false, data, key);
    }

    /**
     * Decrypts data with the given private key and returns the result as a UTF-8 string.
     *
     * @param data the encrypted bytes
     * @param key  the private key string
     * @return the decrypted UTF-8 string
     * @throws IllegalArgumentException if the key or ciphertext is invalid
     */
    public static String privateKeyDecryptAsStr(byte[] data, String key) {
        return new StringBytes(privateKeyDecrypt(data, key)).getUTF8_String(); // needs to Base64?
    }

    /**
     * Loads a UTF-8 Base64- or PEM-encoded private key from an input stream.
     *
     * @param in the stream containing the private key
     * @return the restored private key
     * @throws IllegalArgumentException if the key encoding is invalid
     * @throws UncheckedIOException if the stream cannot be read
     */
    public static PrivateKey loadPrivateKey(InputStream in) {
        return loadPrivateKey(in, CommonConstant.UTF8);
    }

    /**
     * Loads a Base64- or PEM-encoded private key from an input stream using the requested charset.
     *
     * @param in      the stream containing the private key
     * @param charset the charset name used to decode the stream
     * @return the restored private key
     * @throws IllegalArgumentException if the key encoding or charset is invalid
     * @throws UncheckedIOException if the stream cannot be read
     */
    public static PrivateKey loadPrivateKey(InputStream in, String charset) {
        String privateKey;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(2048)) {
            new DataWriter(out).write(in);
            privateKey = out.toString(charset);
        } catch (IOException e) {
            throw new UncheckedIOException("无效的密钥", e);
        }

        return restorePrivateKey(privateKey);
    }

    /**
     * Loads a private key from the given file path.
     *
     * @param filePath the path to the file containing the private key
     * @return the loaded private key
     * @throws IllegalArgumentException if the key encoding is invalid
     * @throws RuntimeException if the file cannot be read
     */
    public static PrivateKey loadPrivateKey(String filePath) {
        String fileContent = new FileHelper(filePath).getFileContent();

        return restorePrivateKey(fileContent);
    }
}
