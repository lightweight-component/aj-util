package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.Cryptography;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

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

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName);
            generator.initialize(keySize);

            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }
    }

    /**
     * 生成一对密钥，并返回密钥对的 byte[]
     *
     * @return Map
     */
    public Map<String, byte[]> getKeyPairAsMapBytes() {
        KeyPair keyPair = generateKeyPair();

        return ObjectHelper.mapOf(PUBLIC_KEY_RSA, keyPair.getPublic().getEncoded(), PRIVATE_KEY_RSA, keyPair.getPrivate().getEncoded());
    }

    /**
     * 生成一对密钥，并返回密钥对的 Base64 编码
     *
     * @return Map
     */
    public Map<String, String> getKeyPairAsMap() {
        KeyPair keyPair = generateKeyPair();

        return ObjectHelper.mapOf(PUBLIC_KEY_RSA, EncodeTools.base64EncodeToString(keyPair.getPublic().getEncoded()),
                PRIVATE_KEY_RSA, EncodeTools.base64EncodeToString(keyPair.getPrivate().getEncoded()));
    }
    /* ------------------------- Restore Key ------------------------ */

    /**
     * 还原公钥/私钥
     *
     * @param isPublic 是否公钥，反之私钥
     * @param key      公钥或私钥的字符串表示
     * @return 还原后的公钥或私钥对象，如果还原失败则返回 null
     */
    public static Key restoreKey(boolean isPublic, String key) {
        byte[] bytes = EncodeTools.base64Decode(key);

        try {
            KeyFactory f = KeyFactory.getInstance(KEY_RSA);

            return isPublic ? f.generatePublic(new X509EncodedKeySpec(bytes)) : f.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid Key. " + key, e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + KEY_RSA, e);
        }
    }

    /* ------------------------- PEM ------------------------ */

    /**
     * 将私钥转换为 PEM 格式字符串
     *
     * @param privateKey 待转换的私钥对象
     * @return PEM 格式的私钥字符串
     */
    public static String privateKeyToPem(PrivateKey privateKey) {
        String encoded = EncodeTools.base64EncodeToString(privateKey.getEncoded());

        return privateKeyToPem(encoded);
    }

    /**
     * 将私钥转换为 PEM 格式字符串
     *
     * @param encoded 待转换的私钥对象
     * @return PEM 格式的私钥字符串
     */
    public static String privateKeyToPem(String encoded) {
        return "-----BEGIN PRIVATE KEY-----\n" +
                EncodeTools.formatBase64String(encoded) +
                "\n-----END PRIVATE KEY-----";
    }

    /**
     * 将公钥转换为 PEM 格式字符串
     *
     * @param publicKey 公钥对象
     * @return PEM 格式的公钥字符串
     */
    public static String publicKeyToPem(PublicKey publicKey) {
        String encoded = EncodeTools.base64EncodeToString(publicKey.getEncoded());

        return publicKeyToPem(encoded);
    }

    /**
     * 将公钥转换为 PEM 格式字符串
     *
     * @param encoded 公钥
     * @return PEM 格式的公钥字符串
     */
    public static String publicKeyToPem(String encoded) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                EncodeTools.formatBase64String(encoded) +
                "\n-----END PUBLIC KEY-----";
    }

    /* ------------------------- encrypt/decrypt ------------------------ */

    /**
     * 处理公钥
     *
     * @param isEncrypt 是否加密(true)，反之为解密（false）
     * @param data      需要加密或解密的数据
     * @param key       公钥或私钥的字符串表示
     * @return 加密或解密后的字节数据
     */
    private static byte[] action(boolean isEncrypt, boolean isPublic, byte[] data, String key) {
        int mode = isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        Cryptography cryptography = new Cryptography(KEY_RSA, mode);
        cryptography.setKey(restoreKey(isPublic, key));
        cryptography.setData(data);

//        return CommonUtil.doCipher(KEY_RSA, mode, restoreKey(isPublic, key), data, null);
        return cryptography.doCipher();
    }

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key  公钥
     * @return 加密后的字节数组
     */
    public static byte[] encryptByPublicKey(byte[] data, String key) {
        return action(true, true, data, key);
    }

    /**
     * 公钥解密
     *
     * @param data 加密数据
     * @param key  公钥
     * @return 解密后的字节数组
     */
    public static byte[] decryptByPublicKey(byte[] data, String key) {
        return action(false, true, data, key);
    }

    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @param key  私钥
     * @return 加密后的字节数组
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key) {
        return action(true, false, data, key);
    }

    /**
     * 私钥解密
     *
     * @param data 加密数据
     * @param key  私钥
     * @return 解密后的字节数组
     */
    public static byte[] decryptByPrivateKey(byte[] data, String key) {
        return action(false, false, data, key);
    }
}
