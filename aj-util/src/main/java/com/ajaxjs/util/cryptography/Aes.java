package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class Aes {
    /**
     * Minimum salt length used by this implementation
     */
    public static final int PBE_SALT_LENGTH = 16;

    /**
     * The minimum recommended iteration count for PBE key derivation.
     */
    public static final int MIN_PBE_ITERATIONS = 100_000;

    /**
     * The derived PBE key length in bits.
     */
    private static final int PBE_KEY_LENGTH = 128;

    /**
     * The nonce length in bytes for GCM mode.
     */
    private static final int GCM_NONCE_LENGTH = 12;

    /**
     * The authentication tag length in bits for GCM mode.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * Generates cryptographically secure random bytes.
     *
     * @param length number of bytes to generate
     * @return newly generated random bytes
     */
    public static byte[] randomBytes(int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length must be greater than zero.");

        byte[] salt = new byte[length];
        RandomTools.RANDOM.nextBytes(salt);

        return salt;
    }

    /**
     * Do encrypt
     *
     * @param data The text to be encrypted
     * @param key  The key
     * @return The encrypted string
     * @throws RuntimeException         if AES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static String aesEncryptLegacy(String data, String key) {
        SecretKey _key = SecretKeyMgr.getSecretKey(SecretKeyMgr.getRandom(key));
        DoCipher cryptography = new DoCipher("AES", Cipher.ENCRYPT_MODE, _key);

        return cryptography.doCipher(data, DoCipher.OutputType.HEX, null, null);
    }

    /**
     * Do decrypt
     *
     * @param data The text to be decrypted
     * @param key  The key
     * @return The decrypted string
     * @throws RuntimeException         if AES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key, hexadecimal input, or ciphertext is invalid
     */
    public static String aesDecryptLegacy(String data, String key) {
        SecretKey _key = SecretKeyMgr.getSecretKey(SecretKeyMgr.getRandom(key));
        DoCipher cryptography = new DoCipher("AES", Cipher.DECRYPT_MODE, _key);

        return cryptography.doCipher(StringBytes.hexToBytes(data), DoCipher.OutputType.UTF8_STR, null, null);
    }

    /**
     * Do encrypt
     *
     * @param data     The text to be encrypted
     * @param keyBytes The key
     * @return The encrypted string
     * @throws RuntimeException         if AES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static String aesEncrypt(String data, byte[] keyBytes, byte[] nonce, byte[] associatedData) {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, SecretKeyMgr.AES);
        DoCipher cryptography = new DoCipher(AES_GCM, Cipher.ENCRYPT_MODE, keySpec);

//        byte[] nonce = randomBytes(GCM_NONCE_LENGTH);
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);

        return cryptography.doCipher(data, DoCipher.OutputType.BASE64, spec, associatedData);
    }

    public static String aesEncrypt(String data, byte[] keyBytes, byte[] nonce) {
        return aesEncrypt(data, keyBytes, nonce, null);
    }

    /**
     * Do decrypt
     *
     * @param data     The text to be decrypted
     * @param keyBytes The key
     * @return The decrypted string
     * @throws RuntimeException         if AES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key, hexadecimal input, or ciphertext is invalid
     */
    public static String aesDecrypt(String data, byte[] keyBytes, byte[] nonce) {
        return aesDecrypt(data, keyBytes, nonce, null);
    }

    public static String aesDecrypt(String data, byte[] keyBytes, byte[] nonce, byte[] associatedData) {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, SecretKeyMgr.AES);
        DoCipher cryptography = new DoCipher(AES_GCM, Cipher.DECRYPT_MODE, keySpec);

        return cryptography.doCipher(new Base64Utils(data).decode(), DoCipher.OutputType.UTF8_STR, new GCMParameterSpec(128, nonce), associatedData);
    }

    /**
     * AES transformation used by WeChat mini programs: CBC mode with PKCS5 padding.
     */
    final static String AES_CBC = "AES/CBC/PKCS5Padding";


    /**
     * AES transformation used by WeChat mini programs: GCM mode with no padding.
     */
    final static String AES_GCM = "AES/GCM/NoPadding";

    public static String aesDecryptCBC(String data, String key, String spec) {
        SecretKeySpec keySpec = new SecretKeySpec(new Base64Utils(key).decode(), SecretKeyMgr.AES);
        DoCipher cryptography = new DoCipher(AES_CBC, Cipher.DECRYPT_MODE, keySpec);
        IvParameterSpec _spec = new IvParameterSpec(new Base64Utils(spec).decode());

        return cryptography.doCipher(new Base64Utils(data).decode(), DoCipher.OutputType.UTF8_STR, _spec, null);
    }

    /**
     * Encrypts the given data using password-based encryption (PBE) with AES/GCM.
     *
     * @param data           the plaintext to encrypt
     * @param key            the password used to derive the encryption key
     * @param salt           the salt for key derivation
     * @param iterationCount the iteration count for key derivation
     * @return the encrypted bytes, with the GCM nonce prepended
     * @throws IllegalArgumentException if the password, salt, iteration count, or plaintext is invalid
     * @throws RuntimeException         if a required cryptographic algorithm is unavailable
     */
    public static byte[] pbeEncrypt(String data, String key, byte[] salt, int iterationCount) {
        validatePbeParameters(salt, iterationCount);
        DoCipher cryptography = new DoCipher(AES_GCM, Cipher.ENCRYPT_MODE, derivePbeKey(key, salt, iterationCount));

        byte[] nonce = randomBytes(GCM_NONCE_LENGTH);
        byte[] encrypted = cryptography.doCipher(data, new GCMParameterSpec(GCM_TAG_LENGTH, nonce), null);
        byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
        System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);

        return result;
    }

    /**
     * Decrypts data produced by {@link #pbeEncrypt(String, String, byte[], int)}.
     *
     * @param data           the encrypted bytes, with the GCM nonce prepended
     * @param key            the password used to derive the decryption key
     * @param salt           the salt for key derivation
     * @param iterationCount the iteration count for key derivation
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the password, salt, iteration count, ciphertext, or tag is invalid
     * @throws RuntimeException         if a required cryptographic algorithm is unavailable
     */
    public static String pbeDecrypt(byte[] data, String key, byte[] salt, int iterationCount) {
        validatePbeParameters(salt, iterationCount);

        if (data == null || data.length < GCM_NONCE_LENGTH + GCM_TAG_LENGTH / Byte.SIZE)
            throw new IllegalArgumentException("PBE ciphertext is missing or too short.");

        DoCipher cryptography = new DoCipher(AES_GCM, Cipher.DECRYPT_MODE, derivePbeKey(key, salt, iterationCount));

        return cryptography.doCipher(Arrays.copyOfRange(data, GCM_NONCE_LENGTH, data.length),
                DoCipher.OutputType.UTF8_STR,
                new GCMParameterSpec(GCM_TAG_LENGTH, Arrays.copyOf(data, GCM_NONCE_LENGTH)), null);
    }

    /**
     * Derives an AES secret key from the given password, salt and iteration count.
     *
     * @param password       the PBE password
     * @param salt           the salt
     * @param iterationCount the iteration count
     * @return the derived AES secret key
     * @throws IllegalArgumentException if the password is null or empty or the key specification is invalid
     * @throws RuntimeException         if PBKDF2 with HMAC-SHA-256 is unavailable
     */
    static SecretKeySpec derivePbeKey(String password, byte[] salt, int iterationCount) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("PBE password must not be empty.");

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, PBE_KEY_LENGTH);

        try {
            SecretKey derivedKey = SecretKeyMgr.getSecretKey(keySpec);
            byte[] encoded = derivedKey.getEncoded();

            if (encoded == null)
                throw new IllegalStateException("Derived PBE key is not encodable.");

            return new SecretKeySpec(encoded, SecretKeyMgr.AES);
        } finally {
            keySpec.clearPassword();
        }
    }

    /**
     * Validates the PBE salt and iteration count parameters.
     *
     * @param salt           the salt to validate
     * @param iterationCount the iteration count to validate
     * @throws IllegalArgumentException if the salt is too short or the iteration count is too small
     */
    static void validatePbeParameters(byte[] salt, int iterationCount) {
        if (salt == null || salt.length < PBE_SALT_LENGTH)
            throw new IllegalArgumentException("PBE salt must contain at least " + PBE_SALT_LENGTH + " bytes.");

        if (iterationCount < MIN_PBE_ITERATIONS)
            throw new IllegalArgumentException("PBE iteration count must be at least " + MIN_PBE_ITERATIONS + ".");
    }
}
