package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.StringBytes;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCryptographyLegacy {
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

        PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());
        SecretKey secretKey;

        try {
            secretKey = SecretKeyMgr.getSecretKey(PBE_LEGACY, keySpec);
        } finally {
            keySpec.clearPassword();
        }

        DoCipher cipher = new DoCipher(PBE_LEGACY, Cipher.DECRYPT_MODE, secretKey);

        return cipher.doCipher(data, new PBEParameterSpec(salt, iterationCount), null).toUtf8();
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
        SecretKey secretKey = SecretKeyMgr.getSecretKey(DES, 0, SecretKeyMgr.getRandom(key));
        DoCipher cipher = new DoCipher(DES, Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doCipher(data, null, null).toHex();
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
        SecretKey secretKey = SecretKeyMgr.getSecretKey(DES, 0, SecretKeyMgr.getRandom(key));
        DoCipher cipher = new DoCipher(DES, Cipher.DECRYPT_MODE, secretKey);

        return cipher.doCipher(StringBytes.hexToBytes(data), null, null).toUtf8();
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
        DoCipher cipher = new DoCipher(TRIPLE_DES, Cipher.ENCRYPT_MODE, new SecretKeySpec(key, TRIPLE_DES));

        return cipher.doCipher(data, null, null).getResult();
    }

    /**
     * Data Encryption Standard algorithm name.
     */
    static String DES = "DES";

    /**
     * Triple DES (also known as DESede) algorithm name.
     */
    @SuppressWarnings("SpellCheckingInspection")
    static String TRIPLE_DES = "DESede";

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
        DoCipher cipher = new DoCipher(TRIPLE_DES, Cipher.DECRYPT_MODE, new SecretKeySpec(key, TRIPLE_DES));

        return cipher.doCipher(data, null, null).toUtf8();
    }

    final String key = "abc";
    final String word = "123";

    @Test
    void testAES() {
        String encWord = Aes.aesEncryptLegacy(word, key);
        assertEquals(word, Aes.aesDecryptLegacy(encWord, key));
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
        byte[] salt = Aes.randomBytes(Aes.PBE_SALT_LENGTH);
        byte[] encData = Aes.pbeEncrypt(word, key, salt, Aes.MIN_PBE_ITERATIONS);

        assertEquals(word, Aes.pbeDecrypt(encData, key, salt, Aes.MIN_PBE_ITERATIONS));
    }

    @Test
    void legacyPbeDecoderReadsHistoricalCiphertext() throws Exception {
        String password = "legacy-password";
        byte[] salt = "12345678".getBytes(StandardCharsets.US_ASCII);
        int iterations = 100;
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKey key = SecretKeyFactory.getInstance(TestCryptographyLegacy.PBE_LEGACY).generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(TestCryptographyLegacy.PBE_LEGACY);
        cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, iterations));
        byte[] encrypted = cipher.doFinal("legacy content".getBytes(StandardCharsets.UTF_8));
        keySpec.clearPassword();

        assertEquals("legacy content", PBE_legacy_decode(encrypted, password, salt, iterations));
        assertThrows(IllegalArgumentException.class, () -> PBE_legacy_decode(encrypted, password, new byte[7], iterations));
    }

    @Test
    void legacySymmetricConvenienceMethodsRoundTrip() {
        String text = "Legacy encryption 测试";
        String password = "compatibility-password";
        byte[] tripleDesKey = "123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

        assertEquals(text, Aes.aesDecryptLegacy(Aes.aesEncryptLegacy(text, password), password));
        assertEquals(text, DES_decode(DES_encode(text, password), password));
        assertEquals(text, tripleDES_decode(tripleDES_encode(text, tripleDesKey), tripleDesKey));
    }
}
