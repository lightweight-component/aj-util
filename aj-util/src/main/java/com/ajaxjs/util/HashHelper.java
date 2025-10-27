package com.ajaxjs.util;

import com.ajaxjs.util.io.DataReader;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Data
@Accessors(chain = true)
public class HashHelper {
    private final String algorithmName;

    private byte[] input;

    public HashHelper(String algorithmName, byte[] input) {
        this.algorithmName = algorithmName;
        this.input = input;
    }

    public HashHelper(String algorithmName, String input) {
        this(algorithmName, new StringBytes(input).getUTF8_Bytes());
    }

    public byte[] getMessageDigest() {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: {}", algorithmName, e);
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        }

        return md.digest(input);
    }

    private byte[] key;

    public HashHelper setKey(String key) {
        this.key = new StringBytes(key).getUTF8_Bytes();
        return this;
    }

    public HashHelper setKeyBase64(String key) {
        this.key = new Base64Utils(key).decode();
        return this;
    }

    /**
     * 获取指定算法的 MAC 值（可设密钥）
     *
     * @return 生成的 MAC 值
     */
    public byte[] getMac() {
        SecretKey sk;

        try {
            if (key == null)
                sk = KeyGenerator.getInstance(algorithmName).generateKey();
            else
                sk = new SecretKeySpec(key, algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        }

        try {
            Mac mac = Mac.getInstance(algorithmName);
            mac.init(sk);

            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid Key.", e);
        }
    }

    public byte[] hash() {
        return key == null ? getMessageDigest() : getMac();
    }

    public String hashAsStr() {
        return BytesHelper.bytesToHexStr(hash()).toLowerCase();
    }

    /**
     * Get the result of hashed in BASE64
     *
     * @param isWithoutPadding 是否去掉末尾的 = 号
     * @return The result of hashed in BASE64
     */
    public String hashAsBase64(boolean isWithoutPadding) {
        return isWithoutPadding ? null : new Base64Utils(hash()).encodeAsString();// todo
    }

    public String hashAsBase64() {
        return hashAsBase64(false);
    }

    public static final String MD5 = "MD5";

    public static final String SHA1 = "SHA1";

    public static final String SHA256 = "SHA-256";

    /**
     * 生成字符串的 MD5 哈希值，等价于 Spring 的 DigestUtils.md5DigestAsHex()
     *
     * @param str 输入的字符串
     * @return 字符串的 MD5 哈希值，返回32位小写的字符串
     */
    public static String md5(String str) {
        return new HashHelper(MD5, str).hashAsStr();
    }

    /**
     * 生成字符串的 SHA1 哈希值
     *
     * @param str 输入的字符串
     * @return 字符串的 SHA1 哈希值
     */
    public static String getSHA1(String str) {
        return new HashHelper(SHA1, str).hashAsStr();
    }

    /**
     * 生成字符串的 SHA256 哈希值
     *
     * @param str 输入的字符串
     * @return 字符串的 SHA256 哈希值
     */
    public static String getSHA256(String str) {
        return new HashHelper(SHA256, str).hashAsStr();
    }

    public static HashHelper getHmacMD5(String str, String key) {
        return new HashHelper("HmacMD5", str).setKey(key);
    }

    /**
     * Calculate the MD5 of a file.
     *
     * @param in The file stream.
     * @return The MD5 result in lowercase.
     */
    public static String calcFileMD5(InputStream in) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            new DataReader(in).readStreamAsBytes(8192, (readSize, buffer) -> digest.update(buffer, 0, readSize));

            return BytesHelper.bytesToHexStr(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: MD5", e);
            throw new RuntimeException("No Such Algorithm: MD5", e);
        }
    }

    /**
     * Calculate the MD5 of a file.
     *
     * @param bytes The file bytes.
     * @return The MD5 result in lowercase.
     */
    public static String calcFileMD5(byte[] bytes) {
        return calcFileMD5(new ByteArrayInputStream(bytes));
    }
}
