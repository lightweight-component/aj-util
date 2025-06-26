package com.ajaxjs.util;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 字符串摘要（哈希）工具类
 */
@Data
@Accessors(chain = true)
@Slf4j
public class MessageDigestHelper {
    /**
     * 定义摘要的算法，可选以下多种算法
     *
     * <pre>
     * MD5
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    @SuppressWarnings("SpellCheckingInspection")
    private String algorithmName;

    /**
     * 密钥，用于 HMAC 摘要，否则可选的
     */
    private String key;

    /**
     * 被摘要的字符串
     */
    private String value;

    /**
     * true 返回 Hex 转换为字符串，false = Base64 编码字符串
     */
    private boolean isHexStr = true;

    /**
     * true 当 Base64 模式下，是否去掉末尾的 = 号，默认 false
     */
    private boolean isBase64withoutPadding;

    /**
     * 获取指定算法的 MessageDigest 对象
     *
     * @param algorithmName 算法名称
     * @param str           需要摘要的字符串
     * @return 摘要后的字节数组
     */
    private static byte[] getMessageDigest(String algorithmName, String str) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: {}", algorithmName, e);
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        }

        return md.digest(StrUtil.getUTF8_Bytes(str));
    }

    /**
     * 返回摘要的结果，可选择 Base64 编码或者转换未 Hex 字符串
     *
     * @return 摘要的结果
     */
    public String getResult() {
        byte[] result;

        if (StrUtil.hasText(key))
            result = getMac(algorithmName, key, value);
        else
            result = getMessageDigest(algorithmName, value);

        if (isHexStr)
            return BytesHelper.bytesToHexStr(result).toLowerCase();
        else { // base64
            if (isBase64withoutPadding)
                return Base64Helper.encode().input(result).withoutPadding().getString();
            else
                return EncodeTools.base64EncodeToString(result);
        }
    }

    /**
     * 生成字符串的 MD5 哈希值，等价于 DigestUtils.md5DigestAsHex()
     *
     * @param str 输入的字符串
     * @return 字符串的 MD5 哈希值，返回32位小写的字符串
     */
    public static String getMd5(String str) {
        return new MessageDigestHelper().setAlgorithmName("MD5").setValue(str).getResult();
    }

    /**
     * 生成字符串的 SHA1 哈希值
     *
     * @param str 输入的字符串
     * @return 字符串的 SHA1 哈希值
     */
    public static String getSHA1(String str) {
        return new MessageDigestHelper().setAlgorithmName("SHA1").setValue(str).getResult();
    }

    /**
     * 生成字符串的 SHA256 哈希值
     *
     * @param str 输入的字符串
     * @return 字符串的 SHA256 哈希值
     */
    public static String getSHA256(String str) {
        return new MessageDigestHelper().setAlgorithmName("SHA-256").setValue(str).getResult();
    }

    /**
     * 生成字符串的 MD5 哈希值，Base64 编码
     *
     * @param str 输入的字符串
     * @return 字符串的 MD5 哈希值，Base64 编码
     */
    public static String getMd5AsBase64(String str) {
        return new MessageDigestHelper().setAlgorithmName("MD5").setValue(str).setHexStr(false).getResult();
    }

    // ----------------------KEY--------------------------

    /**
     * 获取指定算法的 MAC 值（可设密钥）
     *
     * @param algorithmName 算法名称
     * @param key           用于生成 MAC 值的密钥
     * @param data          要进行 MAC 计算的数据
     * @return 生成的 MAC 值
     */
    public static byte[] getMac(String algorithmName, String key, String data) {
        SecretKey sk;

        try {
            if (key == null)
                sk = KeyGenerator.getInstance(algorithmName).generateKey();
            else
                sk = new SecretKeySpec(StrUtil.getUTF8_Bytes(key)/*Base64Utils.decodeFromString(key)*/, algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        }

        try {
            Mac mac = Mac.getInstance(algorithmName);// 获取指定算法的 Mac 对象
            mac.init(sk); // 使用指定算法初始化 Mac 对象

            return mac.doFinal(StrUtil.getUTF8_Bytes(data)); // 对指定数据进行MAC计算
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid Key: " + key, e);
        }
    }

    /**
     * 获取字符串的 MD5 值
     *
     * @param key 生成 MD5 值的密钥
     * @param str 要生成 MD5 值的字符串
     * @return 生成的 MD5 值
     */
    public static String getMd5(String key, String str) {
        return new MessageDigestHelper().setAlgorithmName("HmacMD5").setKey(key).setValue(str).getResult();
    }

    /**
     * 获取字符串的 MD5 值并转换为 Base64 编码
     *
     * @param key 生成 MD5 的密钥
     * @param str 需要生成 MD5 的字符串
     * @return MD5 值的 Base64 编码
     */
    public static String getMd5AsBase64(String key, String str) {
        return new MessageDigestHelper().setAlgorithmName("HmacMD5").setKey(key).setValue(str).setHexStr(false).getResult();
    }

    public static String getHmacSHA1AsBase64(String key, String str) {
        return new MessageDigestHelper().setAlgorithmName("HmacSHA1").setKey(key).setValue(str).setHexStr(false).getResult();
    }

    public static String getHmacSHA256AsBase64(String key, String str) {
        return new MessageDigestHelper().setAlgorithmName("HmacSHA256").setKey(key).setValue(str).setHexStr(false).getResult();
    }

    /**
     * 计算文件 MD5
     *
     * @param file  文件对象。该参数与 bytes 二选一
     * @param bytes 文件字节。该参数与 file 二选一
     * @return 返回文件的 md5 值，如果计算过程中任务的状态变为取消或暂停，返回 null， 如果有其他异常，返回空字符串
     */
    public static String calcFileMD5(File file, byte[] bytes) {
        byte[] buf = new byte[8192];
        int len;

        try (InputStream stream = file != null ? Files.newInputStream(file.toPath(), StandardOpenOption.READ) : new ByteArrayInputStream(bytes)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            while ((len = stream.read(buf)) > 0)
                digest.update(buf, 0, len);

            return BytesHelper.bytesToHexStr(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: MD5", e);
            throw new RuntimeException("No Such Algorithm: MD5", e);
        } catch (IOException e) {
            log.warn("Calc File MD5 IO Error.", e);
            throw new UncheckedIOException("Calc File MD5 IO Error.", e);
        }
    }

    /**
     * 计算一个字符串的 MD5 值
     *
     * @param str 待计算 MD5 的字符串
     * @return 计算结果
     */
    public static String md5(String str) {
        return getMd5(str);
    }
}
