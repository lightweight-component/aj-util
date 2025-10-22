package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

/**
 * AES/DES/3DES/PBE 对称加密/解密
 */
@Data
@RequiredArgsConstructor
public class Cryptography {
    /**
     * The name of the algorithm
     */
    private final String algorithmName;

    /**
     * 是解密模式还是加密模式？
     */
    private final int mode;

    private Key key;

    private byte[] keyData;

    public void setKeyData(byte[] keyData) {
        this.keyData = keyData;
        key = new SecretKeySpec(keyData, algorithmName);
    }

    private SecretKey secretKey;

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        key = new SecretKeySpec(secretKey.getEncoded(), algorithmName);
    }

    private byte[] data;

    private String dataStr;

    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
        data = StrUtil.getUTF8_Bytes(dataStr);
    }

    private String dataStrBase64;

    public void setDataStrBase64(String dataStrBase64) {
        this.dataStrBase64 = dataStrBase64;
        data = EncodeTools.base64Decode(dataStrBase64);
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
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("加密原串的长度不能超过214字节", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
        }
    }

    public String doCipherAsStr() {
        return StrUtil.byte2String(doCipher());
    }

    public String doCipherAsBase64Str() {
        return EncodeTools.base64EncodeToString(doCipher());
    }

    /**
     * Get hex string of cipher, which is good for encrypt.
     *
     * @return Hex string of cipher.
     */
    public String doCipherAsHexStr() {
        return BytesHelper.bytesToHexStr(doCipher());
    }

    public static String AES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

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
        byte[] salt = new byte[8];
        new Random().nextBytes(salt);

        return salt;
    }

    public static byte[] PBE_encode(String data, String key, byte[] salt) {
        Cryptography cryptography = new Cryptography(Constant.PBE, Cipher.ENCRYPT_MODE);
        cryptography.setKey(SecretKeyMgr.getSecretKey(Constant.PBE, new PBEKeySpec(key.toCharArray())));
        cryptography.setSpec(new PBEParameterSpec(salt, 100));
        cryptography.setDataStr(data);

        return cryptography.doCipher();
    }

    public static String PBE_decode(byte[] data, String key, byte[] salt) {
        Cryptography cryptography = new Cryptography(Constant.PBE, Cipher.DECRYPT_MODE);
        cryptography.setKey(SecretKeyMgr.getSecretKey(Constant.PBE, new PBEKeySpec(key.toCharArray())));
        cryptography.setSpec(new PBEParameterSpec(salt, 100));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }
}
