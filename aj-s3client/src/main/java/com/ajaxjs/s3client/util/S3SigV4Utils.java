package com.ajaxjs.s3client.util;

import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.HashHelper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cryptographic and formatting helpers used by the Signature Version 4 implementation.
 */
public abstract class S3SigV4Utils {
    /**
     * Returns the current UTC time in the SigV4 basic ISO-8601 format.
     *
     * @return timestamp formatted as {@code yyyyMMdd'T'HHmmss'Z'}
     */
    public static String now() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    }

    /**
     * @param key   HMAC key bytes
     * @param value UTF-8 text to sign
     * @return HMAC-SHA256 bytes
     * @throws IllegalArgumentException if the key or value is {@code null}
     * @throws IllegalStateException    if the runtime cannot calculate HMAC-SHA256
     */
    public static byte[] hmacSha256(byte[] key, String value) {
        if (key == null || value == null)
            throw new IllegalArgumentException("HMAC key and value are required.");

        try {
            Mac mac = Mac.getInstance(HashHelper.HMAC_SHA256);
            mac.init(new SecretKeySpec(key, HashHelper.HMAC_SHA256));

            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate the SigV4 HMAC.", e);
        }
    }

    /**
     * @param bytes payload bytes
     * @return lowercase SHA-256 hexadecimal digest
     * @throws IllegalArgumentException if the payload is {@code null}
     */
    public static String calcFileSHA256(byte[] bytes) {
        if (bytes == null)
            throw new IllegalArgumentException("Payload bytes are required.");

        return new HashHelper(HashHelper.SHA256, bytes).hashAsStr();
    }

    /**
     * Returns lowercase hexadecimal using the shared ajaxjs-util implementation.
     *
     * @param data digest bytes
     * @return lowercase hexadecimal text
     * @throws IllegalArgumentException if the digest is {@code null}
     */
    public static String toHex(byte[] data) {
        if (data == null)
            throw new IllegalArgumentException("Digest bytes are required.");

        return BytesHelper.bytesToHexStr(data).toLowerCase(java.util.Locale.ROOT);
    }
}
