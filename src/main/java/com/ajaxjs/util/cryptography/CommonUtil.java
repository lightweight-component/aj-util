package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public class CommonUtil {
    /**
     * 进行加密或解密，三步走
     *
     * @param algorithmName 选择的算法
     * @param mode          是解密模式还是加密模式？
     * @param key           密钥
     * @param data          输入的内容
     * @return 结果
     */
    public static byte[] doCipher(String algorithmName, int mode, Key key, byte[] data) {
        return doCipher(algorithmName, mode, key, data, null);
    }

    /**
     * 进行加密或解密，三步走
     *
     * @param algorithmName 选择的算法
     * @param mode          是解密模式还是加密模式？
     * @param key           密钥
     * @param data          输入的内容
     * @param spec          参数，可选的
     * @return 结果
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static byte[] doCipher(String algorithmName, int mode, Key key, byte[] data, AlgorithmParameterSpec spec) {
        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            /*
             * 为了防止解密时报 javax.crypto.IllegalBlockSizeException: Input length must be
             * multiple of 8 when decrypting with padded cipher 异常， 不能把加密后的字节数组直接转换成字符串
             */
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前 Java 环境不支持 RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("加密原串的长度不能超过214字节", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("无效的算法参数", e);
        }
    }

    public static String doCipher(String algorithmName, int mode, byte[] keyData, AlgorithmParameterSpec spec, String cipherText, byte[] associatedData) {
        SecretKeySpec key = new SecretKeySpec(keyData, "AES");

        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            if (associatedData != null)
                cipher.updateAAD(associatedData);

            return StrUtil.byte2String(cipher.doFinal(EncodeTools.base64Decode(cipherText)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + algorithmName, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("加密原串的长度不能超过214字节", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("无效的算法参数", e);
        }
    }
}
