package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecretKeyMgr {
    /**
     * 获取对称加密用的 SecretKey
     *
     * @param algorithmName 加密算法
     * @param secure        可选的
     * @param keySize       可选的
     * @return SecretKey
     */
    public static SecretKey getSecretKey(String algorithmName, int keySize, SecureRandom secure) {
        KeyGenerator kg;

        try {
            kg = KeyGenerator.getInstance(algorithmName);

            if (keySize != 0 && secure != null)
                kg.init(keySize, secure);
            else if (keySize == 0 && secure != null)
                kg.init(secure);
            else if (keySize != 0 && secure == null)
                kg.init(keySize);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        return kg.generateKey();
    }

    /**
     * 获取指定算法的安全随机数生成器实例，并使用给定密钥作为种子初始化
     *
     * @param algorithmName 随机数生成算法名称，如"SHA1PRNG"
     * @param key           用于初始化随机数生成器的种子密钥字符串
     * @return 初始化完成的SecureRandom实例
     * @throws RuntimeException 当指定的算法不存在时抛出
     */
    public static SecureRandom getRandom(String algorithmName, String key) {
        SecureRandom random;

        try {
            random = SecureRandom.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        random.setSeed(StrUtil.getUTF8_Bytes(key));

        return random;
    }

    /**
     * 根据指定算法和安全随机数生成一个秘密密钥，并将其以 Base64 编码的字符串形式返回
     *
     * @param algorithmName 算法名称
     * @param keySize       The size of key
     * @param secure        安全随机数
     * @return Base64 编码后的秘密密钥字符串
     */
    public static String getSecretKeyAsStr(String algorithmName, int keySize, SecureRandom secure) {
        byte[] encoded = getSecretKey(algorithmName, keySize, secure).getEncoded();

        return EncodeTools.base64EncodeToString(encoded);
    }
}
