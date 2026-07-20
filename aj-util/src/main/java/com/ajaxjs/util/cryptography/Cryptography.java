package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/**
 * AES/DES/3DES/PBE 对称加密/解密
 */
@Data
@RequiredArgsConstructor
public class Cryptography {
    public static final int PBE_SALT_LENGTH = 16;

    public static final int MIN_PBE_ITERATIONS = 100_000;

    private static final int PBE_KEY_LENGTH = 128;

    private static final int GCM_NONCE_LENGTH = 12;

    private static final int GCM_TAG_LENGTH = 128;

    /**
     * The name of the algorithm
     */
    private final String algorithmName;

    /**
     * 是解密模式还是加密模式？
     */
    private final int mode;

    private Key key;

    public void setKeyData(byte[] keyData) {
        key = new SecretKeySpec(keyData, algorithmName);
    }

    private SecretKey secretKey;

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        key = new SecretKeySpec(secretKey.getEncoded(), algorithmName);
    }

    private byte[] data;

    public void setDataStr(String dataStr) {
        data = new StringBytes(dataStr).getUTF8_Bytes();
    }

    public void setDataStrBase64(String dataStrBase64) {
        data = new Base64Utils(dataStrBase64).decode();
    }

    private AlgorithmParameterSpec spec;

    private byte[] associatedData;

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
        } catch (AEADBadTagException e) {
            throw new IllegalArgumentException("Authentication failed: the key, parameters, associated data, or ciphertext is invalid.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Invalid input length for transformation: " + algorithmName, e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("Cipher operation failed because the key, padding, or ciphertext is invalid.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
        }
    }

    public String doCipherAsStr() {
        return new StringBytes(doCipher()).getUTF8_String();
    }

    public String doCipherAsBase64Str() {
        return new Base64Utils(doCipher()).encodeAsString();
    }

    /**
     * Get hex string of cipher, which is good for encrypt.
     *
     * @return Hex string of cipher.
     */
    public String doCipherAsHexStr() {
        return BytesHelper.bytesToHexStr(doCipher());
    }

    /**
     * Do encrypt
     *
     * @param data The text to be encrypted
     * @param key  The key
     * @return The encrypted string
     */
    public static String AES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    /**
     * Do decrypt
     *
     * @param data The text to be decrypted
     * @param key  The key
     * @return The decrypted string
     */
    public static String AES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(BytesHelper.parseHexStr2Byte(data));

        return cryptography.doCipherAsStr();
    }

    public static String DES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    public static String DES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(BytesHelper.parseHexStr2Byte(data));

        return cryptography.doCipherAsStr();
    }

    public static byte[] tripleDES_encode(String data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.ENCRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setDataStr(data);

        return cryptography.doCipher();
    }

    public static String tripleDES_decode(byte[] data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    /**
     * 初始化盐（salt）
     *
     * @return 盐（salt）
     */
    public static byte[] initSalt() {
        byte[] salt = new byte[PBE_SALT_LENGTH];
        RandomTools.RANDOM.nextBytes(salt);

        return salt;
    }

    public static byte[] PBE_encode(String data, String key, byte[] salt, int iterationCount) {
        validatePbeParameters(salt, iterationCount);
        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.ENCRYPT_MODE);
        cryptography.setKey(derivePbeKey(key, salt, iterationCount));
        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        RandomTools.RANDOM.nextBytes(nonce);
        cryptography.setSpec(new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
        cryptography.setDataStr(data);

        byte[] encrypted = cryptography.doCipher();
        byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
        System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);

        return result;
    }

    public static String PBE_decode(byte[] data, String key, byte[] salt, int iterationCount) {
        validatePbeParameters(salt, iterationCount);
        if (data == null || data.length < GCM_NONCE_LENGTH + GCM_TAG_LENGTH / Byte.SIZE)
            throw new IllegalArgumentException("PBE ciphertext is missing or too short.");

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        cryptography.setKey(derivePbeKey(key, salt, iterationCount));
        cryptography.setSpec(new GCMParameterSpec(GCM_TAG_LENGTH, Arrays.copyOf(data, GCM_NONCE_LENGTH)));
        cryptography.setData(Arrays.copyOfRange(data, GCM_NONCE_LENGTH, data.length));

        return cryptography.doCipherAsStr();
    }

    /**
     * Decrypts data created by the former PBEWithMD5AndDES implementation.
     * This method must not be used to encrypt new data.
     */
    @Deprecated
    public static String PBE_legacy_decode(byte[] data, String key, byte[] salt, int iterationCount) {
        if (salt == null || salt.length != 8)
            throw new IllegalArgumentException("Legacy PBE salt must contain exactly 8 bytes.");

        if (iterationCount <= 0)
            throw new IllegalArgumentException("Legacy PBE iteration count must be greater than zero.");

        Cryptography cryptography = new Cryptography(Constant.PBE_LEGACY, Cipher.DECRYPT_MODE);
        PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());

        try {
            cryptography.setKey(SecretKeyMgr.getSecretKey(Constant.PBE_LEGACY, keySpec));
        } finally {
            keySpec.clearPassword();
        }

        cryptography.setSpec(new PBEParameterSpec(salt, iterationCount));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    private static SecretKeySpec derivePbeKey(String password, byte[] salt, int iterationCount) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("PBE password must not be empty.");

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, PBE_KEY_LENGTH);

        try {
            Key derivedKey = SecretKeyMgr.getSecretKey(Constant.PBE, keySpec);
            return new SecretKeySpec(derivedKey.getEncoded(), Constant.AES);
        } finally {
            keySpec.clearPassword();
        }
    }

    private static void validatePbeParameters(byte[] salt, int iterationCount) {
        if (salt == null || salt.length < PBE_SALT_LENGTH)
            throw new IllegalArgumentException("PBE salt must contain at least " + PBE_SALT_LENGTH + " bytes.");

        if (iterationCount < MIN_PBE_ITERATIONS)
            throw new IllegalArgumentException("PBE iteration count must be at least " + MIN_PBE_ITERATIONS + ".");
    }
}
