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
    /**
     * The algorithm name.
     */
    private final String algorithmName;

    /**
     * The input data.
     */
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

    /**
     * Do hash
     *
     * @return The hash value in bytes.
     */
    public byte[] hash() {
        return key == null ? getMessageDigest() : getMac();
    }

    /**
     * Do hash
     *
     * @return The hash value in hex string, lowercase.
     */
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
        return new Base64Utils(hash()).setWithoutPadding(isWithoutPadding).encodeAsString();
    }

    /**
     * Get the result of hashed in BASE64
     *
     * @return The result of hashed in BASE64
     */
    public String hashAsBase64() {
        return hashAsBase64(false);
    }

    public static final String MD5 = "MD5";

    public static final String SHA1 = "SHA1";

    public static final String SHA256 = "SHA-256";

    /**
     * Generates MD5 hash value for a string. It's equivalent to Spring's DigestUtils.md5DigestAsHex()
     *
     * @param str Input string
     * @return MD5 hash value
     */
    public static String md5(String str) {
        return new HashHelper(MD5, str).hashAsStr();
    }

    /**
     * Generates SHA1 hash value for a string.
     *
     * @param str Input string
     * @return SHA1 hash value
     */
    public static String getSHA1(String str) {
        return new HashHelper(SHA1, str).hashAsStr();
    }

    /**
     * Generates SHA256 hash value for a string.
     *
     * @param str Input string
     * @return SHA256 hash value
     */
    public static String getSHA256(String str) {
        return new HashHelper(SHA256, str).hashAsStr();
    }

    /**
     * Generates HMAC-MD5 hash value for a string.
     *
     * @param str Input string
     * @param key Key string
     * @return HMAC-MD5 hash value
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static HashHelper getHmacMD5(String str, String key) {
        return new HashHelper("HmacMD5", str).setKey(key);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static final String HMAC_SHA1 = "HmacSHA1";

    @SuppressWarnings("SpellCheckingInspection")
    public static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates HMAC-SHA256 hash value for a string.
     *
     * @param str Input string
     * @param key Key string
     * @return HMAC-SHA256 hash value
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static String getHmacSHA256(String str, String key, boolean isWithoutPadding) {
        return new HashHelper(HMAC_SHA256, str).setKey(key).hashAsBase64(isWithoutPadding);
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
