package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCryptography {
    /**
     * Legacy PBE algorithm retained only for decrypting existing data.
     */
    @Deprecated
    @SuppressWarnings("SpellCheckingInspection")
    static String PBE_LEGACY = "PBEWITHMD5andDES";

    /**
     * Decrypts data created by the former PBEWithMD5AndDES implementation.
     * This method must not be used to encrypt new data.
     *
     * @param data           the data to be decoded
     * @param key            the password used to derive the decryption key
     * @param salt           the legacy 8-byte PBE salt
     * @param iterationCount the positive legacy key-derivation iteration count
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the password, salt, iteration count, key, or ciphertext is invalid
     * @throws RuntimeException         if the legacy algorithm is unavailable
     */
    @Deprecated
    public static String PBE_legacy_decode(byte[] data, String key, byte[] salt, int iterationCount) {
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("Legacy PBE password must not be empty.");

        if (data == null || data.length == 0)
            throw new IllegalArgumentException("Legacy PBE ciphertext must not be empty.");

        if (salt == null || salt.length != 8)
            throw new IllegalArgumentException("Legacy PBE salt must contain exactly 8 bytes.");

        if (iterationCount <= 0)
            throw new IllegalArgumentException("Legacy PBE iteration count must be greater than zero.");

        Cryptography cryptography = new Cryptography(PBE_LEGACY, Cipher.DECRYPT_MODE);
        PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());

        try {
            cryptography.setKey(SecretKeyMgr.getSecretKey(PBE_LEGACY, keySpec));
        } finally {
            keySpec.clearPassword();
        }

        cryptography.setSpec(new PBEParameterSpec(salt, iterationCount));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    /**
     * Encrypts the given data using DES and returns the result as a hex string.
     * Legacy compatibility only. Prefer AES/GCM for new encrypted data.
     *
     * @param data the plaintext to encrypt
     * @param key  the encryption key
     * @return the encrypted hex string
     * @throws RuntimeException         if DES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key or input is invalid
     */
    @Deprecated
    public static String DES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    /**
     * Decrypts the given DES-encrypted hex string.
     * Legacy compatibility only. Prefer AES/GCM for new encrypted data.
     *
     * @param data the encrypted hex string
     * @param key  the decryption key
     * @return the decrypted plaintext
     * @throws RuntimeException         if DES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key, hexadecimal input, or ciphertext is invalid
     */
    @Deprecated
    public static String DES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(StringBytes.hexToBytes(data));

        return cryptography.doCipherAsStr();
    }

    /**
     * Encrypts the given data using Triple DES and returns the raw bytes.
     *
     * @param data the plaintext to encrypt
     * @param key  the encryption key bytes
     * @return the encrypted bytes
     * @throws IllegalArgumentException if the key or input is invalid
     * @throws RuntimeException         if Triple DES is unavailable
     */
    @Deprecated
    public static byte[] tripleDES_encode(String data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.ENCRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setDataStr(data);

        return cryptography.doCipher();
    }

    /**
     * Decrypts the given Triple DES-encrypted bytes.
     *
     * @param data the encrypted bytes
     * @param key  the decryption key bytes
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the key or ciphertext is invalid
     * @throws RuntimeException         if Triple DES is unavailable
     */
    @Deprecated
    public static String tripleDES_decode(byte[] data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    final String key = "abc";
    final String word = "123";

    @Test
    void testAES() {
        String encWord = Cryptography.AES_encode(word, key);
        assertEquals(word, Cryptography.AES_decode(encWord, key));
    }

    @Test
    void testDES() {
        String encWord = DES_encode(word, key);
        assertEquals(word, DES_decode(encWord, key));
    }

    @SuppressWarnings("restriction")
    @Test
    void test3DES() {
        // 添加新安全算法,如果用 JCE 就要把它添加进去
        // 这里 addProvider 方法是增加一个新的加密算法提供者(个人理解没有找到好的答案,求补充)
//		Security.addProvider(new com.sun.crypto.provider.SunJCE());
        // byte 数组(用来生成密钥的)
        final byte[] keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x51, (byte) 0xCB, (byte) 0xDD, 0x55, 0x66, 0x77, 0x29, 0x74,
                (byte) 0x98, 0x30, 0x40, 0x36, (byte) 0xE2};
        String word = "This is a 3DES test. 测试";

        byte[] encoded = tripleDES_encode(word, keyBytes);

        assertEquals(word, tripleDES_decode(encoded, keyBytes));
    }

    @Test
    void testPBE() {
        byte[] salt = Cryptography.initSalt();
        byte[] encData = Cryptography.PBE_encode(word, key, salt, 100_000);

        assertEquals(word, Cryptography.PBE_decode(encData, key, salt, 100_000));
    }

    @Test
    void testDoSignature() {
// 生成公钥私钥
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        keyMgr.generateKeyPair();
        String privateKey = keyMgr.getPrivateKeyStr();

        byte[] helloWorlds = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).sign();
        String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();

        assertEquals(new Base64Utils(helloWorlds).encodeAsString(), result);
    }

    @Test
    void testDoVerify() {
// 生成公钥私钥
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        keyMgr.generateKeyPair();
        String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();
        String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();
        boolean verified = new DoVerify(Constant.SHA256_RSA).setStrData("hello world").setPublicKeyStr(publicKey).setSignatureBase64(result).verify();

        assertTrue(verified);
    }

    @Test
    public void testRSA() {
        // 生成公钥私钥
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        keyMgr.generateKeyPair();
        String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();

        System.out.println("公钥: \n\r" + publicKey);
        System.out.println("私钥： \n\r" + privateKey);
//		System.out.println("公钥加密--------私钥解密");

        String word = "你好，世界！";

        byte[] encWord = KeyMgr.publicKeyEncrypt(word.getBytes(), publicKey);
        String decWord = new String(KeyMgr.privateKeyDecrypt(encWord, privateKey));

        String eBody = new Base64Utils(encWord).encodeAsString();
        String decWord2 = new String(KeyMgr.privateKeyDecrypt(new Base64Utils(eBody).decode(), privateKey));
        System.out.println("加密前: " + word + "\n\r密文：" + eBody + "\n解密后: " + decWord2);
        assertEquals(word, decWord);

//		System.out.println("私钥加密--------公钥解密");

        String english = "Hello, World!";
        byte[] encEnglish = KeyMgr.privateKeyEncrypt(english.getBytes(), privateKey);
        String decEnglish = new String(KeyMgr.publicKeyDecrypt(encEnglish, publicKey));
//		System.out.println("加密前: " + english + "\n\r" + "解密后: " + decEnglish);

        assertEquals(english, decEnglish);
//		System.out.println("私钥签名——公钥验证签名");

// 产生签名
        String sign = new DoSignature(Constant.MD5_RSA).setPrivateKeyStr(privateKey).setData(encEnglish).signToString();
//		System.out.println("签名:\r" + sign);
// 验证签名
        assertTrue(new DoVerify(Constant.MD5_RSA).setPublicKeyStr(publicKey).setData(encEnglish).setSignatureBase64(sign).verify());
    }
}
