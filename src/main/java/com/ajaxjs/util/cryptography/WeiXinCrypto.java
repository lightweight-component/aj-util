package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.io.Resources;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * 证书和回调报文解密
 */
@Deprecated
public class WeiXinCrypto {
    /**
     * AEAD_AES_256_GCM 解密
     *
     * @param aesKey         key 密钥，ApiV3Key，长度必须为32个字节
     * @param associatedData 相关数据
     * @param nonce          随机字符串
     * @param cipherText     密文
     * @return 解密后的文本
     */
    public static String aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
        if (aesKey.length != 32)
            throw new IllegalArgumentException("无效的 ApiV3Key，长度必须为32个字节");

        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);

        return doCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, aesKey, spec, cipherText, associatedData);
    }

    public static String aesDecryptToString2(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
        if (aesKey.length != 32)
            throw new IllegalArgumentException("无效的 ApiV3Key，长度必须为32个字节");

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(aesKey, Constant.AES)); // little odd, it's AES.
        cryptography.setSpec(new GCMParameterSpec(128, nonce));
        cryptography.setDataStrBase64(cipherText);
        cryptography.setAssociatedData(associatedData);

        return cryptography.doCipherAsStr();
    }

    //----------------- RSA 加密、解密 -------------------
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * 加密
     *
     * @param message     数据
     * @param certificate 证书
     * @return 加密后的文本
     */
    public static String encryptOAEP(String message, X509Certificate certificate) {
        byte[] bytes = doCipher(TRANSFORMATION, Cipher.ENCRYPT_MODE, certificate.getPublicKey(), message.getBytes(StandardCharsets.UTF_8));

        return EncodeTools.base64EncodeToString(bytes);
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param privateKey 商户私钥
     * @return 解密后的文本
     */
    public static String decryptOAEP(String cipherText, PrivateKey privateKey) {
        byte[] cipherData = doCipher(TRANSFORMATION, Cipher.DECRYPT_MODE, privateKey, Base64.getDecoder().decode(cipherText));

        return new String(cipherData, StandardCharsets.UTF_8);
    }

    /**
     * 使用 RSA 加密算法对消息进行加密
     *
     * @param message  待加密的消息
     * @param certPath 证书的路径，用于获取加密密钥
     * @return 加密后的消息
     * @throws UncheckedIOException 如果证书读取过程中发生 IO 错误，则抛出运行时异常
     */
    public static String rsaEncrypt(String message, String certPath) {
        try (InputStream in = Resources.getResource(certPath)) {// 从输入流中加载 X.509 证书
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(in);
            cert.checkValidity();

            return encryptOAEP(message, cert);
        } catch (IOException e) {
            throw new UncheckedIOException("IC 错误", e);
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("证书已过期", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("证书尚未生效", e);
        } catch (CertificateException e) {
            throw new RuntimeException("无效的证书", e);
        }
    }

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

    /**
     * 对签名数据进行签名。
     * <p>
     * 使用商户私钥对待签名串进行 SHA256 with RSA 签名，并对签名结果进行 Base64 编码得到签名值。
     *
     * @param privateKey 商户私钥
     * @param data       数据
     * @return 签名结果
     */
    public static String rsaSign(PrivateKey privateKey, byte[] data) {
        return EncodeTools.base64EncodeToString(sign("SHA256withRSA", privateKey, data));
    }


    public static byte[] sign(String algorithmName, PrivateKey privateKey, byte[] data) {
        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initSign(privateKey);
            signature.update(data);

            return signature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException("签名计算失败", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        }
    }
}
