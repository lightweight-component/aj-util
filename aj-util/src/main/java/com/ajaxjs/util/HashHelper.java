/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util;

import com.ajaxjs.util.io.DataReader;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for cryptographic hash operations, supporting various hash algorithms
 * (MD5, SHA-1, SHA-256) and HMAC operations.
 * Provides methods for generating hashes from strings and byte arrays,
 * with options for hexadecimal and Base64 output formats.
 */
@Slf4j
public class HashHelper {
    /**
     * The name of the hash algorithm to use (e.g., "MD5", "SHA-1", "SHA-256").
     */
    private final String algorithmName;

    /**
     * The input data to hash.
     */
    private final byte[] input;

    /**
     * Creates a new HashHelper instance with the specified algorithm and byte array input.
     *
     * @param algorithmName the hash algorithm name (e.g., "MD5", "SHA-1", "SHA-256", "HmacMD5")
     * @param input         the input data to hash
     */
    public HashHelper(String algorithmName, byte[] input) {
        this.algorithmName = Objects.requireNonNull(algorithmName, "algorithmName");
        this.input = Objects.requireNonNull(input, "input");
    }

    /**
     * Creates a new HashHelper instance with the specified algorithm and string input.
     * The string is converted to bytes using UTF-8 encoding.
     *
     * @param algorithmName the hash algorithm name
     * @param input         the input string to hash
     */
    public HashHelper(String algorithmName, String input) {
        this(algorithmName, new StringBytes(input).getUTF8_Bytes());
    }

    /**
     * Gets the message digest using the specified algorithm.
     *
     * @return the message digest as a byte array
     */
    public byte[] getMessageDigest() {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: {}", algorithmName, e);
            throw new IllegalStateException("No Such Algorithm: " + algorithmName, e);
        }

        return md.digest(input);
    }

    /**
     * The secret key used for HMAC operations.
     */
    private byte[] key;

    /**
     * Sets the secret key for HMAC operations using UTF-8 encoding.
     *
     * @param key the secret key as a string
     * @return this HashHelper instance for method chaining
     */
    public HashHelper setKey(String key) {
        Objects.requireNonNull(key, "key");
        this.key = new StringBytes(key).getUTF8_Bytes();

        return this;
    }

    /**
     * Sets the secret key for HMAC operations by decoding a Base64 encoded string.
     *
     * @param key the Base64 encoded secret key
     * @return this HashHelper instance for method chaining
     */
    public HashHelper setKeyBase64(String key) {
        Objects.requireNonNull(key, "key");
        this.key = new Base64Utils(key).decode();

        return this;
    }

    /**
     * Gets the Message Authentication Code (MAC) value using the specified algorithm.
     *
     * @return the generated MAC value as a byte array
     * @throws IllegalStateException    if no HMAC key has been set
     *                                  or the algorithm is not supported
     * @throws IllegalArgumentException if the HMAC key is invalid
     */
    public byte[] getMac() {
        if (key == null)
            throw new IllegalStateException("HMAC key is required.");

        try {
            Mac mac = Mac.getInstance(algorithmName);
            mac.init(new SecretKeySpec(key, algorithmName));

            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No Such Algorithm: " + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid HMAC key.", e);
        }
    }

    /**
     * Performs the hash or HMAC operation based on the configured algorithm.
     * <p>
     * Algorithms whose names start with {@code Hmac} are processed as HMAC;
     * other algorithms are processed using {@link MessageDigest}.
     *
     * @return the hash or MAC value as a byte array
     */
    public byte[] hash() {
        return algorithmName.startsWith("Hmac") ? getMac() : getMessageDigest();
    }

    /**
     * Performs the hash operation and returns the result as a lowercase hexadecimal string.
     *
     * @return the hash value as a lowercase hexadecimal string
     */
    public String hashAsStr() {
        return StringBytes.bytesToHex(hash()).toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the result of the hash operation encoded in Base64 format.
     *
     * @param isWithoutPadding whether to omit the padding '=' characters
     * @return the hash value as a Base64 encoded string
     */
    public String hashAsBase64(boolean isWithoutPadding) {
        return new Base64Utils(hash()).setWithoutPadding(isWithoutPadding).encodeAsString();
    }

    /**
     * Gets the result of the hash operation encoded in Base64 format with padding.
     *
     * @return the hash value as a Base64 encoded string with padding
     */
    public String hashAsBase64() {
        return hashAsBase64(false);
    }

    /**
     * Constant for MD5 hash algorithm.
     */
    public static final String MD5 = "MD5";

    /**
     * Constant for SHA-1 hash algorithm.
     */
    public static final String SHA1 = "SHA-1";

    /**
     * Constant for SHA-256 hash algorithm.
     */
    public static final String SHA256 = "SHA-256";

    /**
     * Generates an MD5 hash value for a string.
     * This is equivalent to Spring's DigestUtils.md5DigestAsHex() method.
     *
     * @param str the input string
     * @return the MD5 hash value as a lowercase hexadecimal string
     */
    public static String md5(String str) {
        return new HashHelper(MD5, str).hashAsStr();
    }

    /**
     * Calculates the MD5 hash of a file from an input stream.
     * The file is processed in chunks to handle large files efficiently.
     * This method does not close the input stream.
     *
     * @param in the input stream containing the file data
     * @return the MD5 hash value as a lowercase hexadecimal string
     * @throws RuntimeException if MD5 algorithm is not available
     */
    public static String md5(InputStream in) {
        Objects.requireNonNull(in, "in");

        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            new DataReader(in).readStreamAsBytes(CommonConstant.BUFFER_SIZE,
                    (readSize, buffer) -> digest.update(buffer, 0, readSize));

            return StringBytes.bytesToHex(digest.digest()).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: MD5", e);
            throw new RuntimeException("No Such Algorithm: MD5", e);
        }
    }

    /**
     * Calculates the MD5 hash of a byte array.
     *
     * @param bytes the input byte array
     * @return the MD5 hash value as a lowercase hexadecimal string
     */
    public static String md5(byte[] bytes) {
        return new HashHelper(MD5, bytes).hashAsStr();
    }

    /**
     * Generates an SHA-1 hash value for a string.
     *
     * @param str the input string
     * @return the SHA-1 hash value as a lowercase hexadecimal string
     */
    public static String sha1(String str) {
        return new HashHelper(SHA1, str).hashAsStr();
    }

    /**
     * Generates an SHA-256 hash value for a string.
     *
     * @param str the input string
     * @return the SHA-256 hash value as a lowercase hexadecimal string
     */
    public static String sha256(String str) {
        return new HashHelper(SHA256, str).hashAsStr();
    }

    /**
     * Constant for HMAC-MD5 message authentication code algorithm.
     */
    public static final String HMAC_MD5 = "HmacMD5";

    /**
     * Constant for HMAC-SHA1 message authentication code algorithm.
     */
    public static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * Constant for HMAC-SHA256 message authentication code algorithm.
     */
    public static final String HMAC_SHA256 = "HmacSHA256";

    static String hmac(String algorithmName, String str, String key, boolean isWithoutPadding) {
        return new HashHelper(algorithmName, str).setKey(key).hashAsBase64(isWithoutPadding);
    }

    /**
     * Creates a HashHelper configured for HMAC-MD5 operations with the specified input and key.
     *
     * @param str              the input string
     * @param key              the secret key
     * @param isWithoutPadding whether to omit padding in the Base64 output
     * @return the HMAC-MD5 value as a Base64 encoded string
     */
    public static String hmacMD5(String str, String key, boolean isWithoutPadding) {
        return hmac(HMAC_MD5, str, key, isWithoutPadding);
    }

    /**
     * Generates an HMAC-SHA1 hash value for a string and returns it as a Base64 encoded string.
     *
     * @param str              the input string
     * @param key              the secret key
     * @param isWithoutPadding whether to omit padding in the Base64 output
     * @return the HMAC-SHA1 hash value as a Base64 encoded string
     */
    public static String hmacSHA1(String str, String key, boolean isWithoutPadding) {
        return hmac(HMAC_SHA1, str, key, isWithoutPadding);
    }

    /**
     * Generates an HMAC-SHA256 hash value for a string and returns it as a Base64 encoded string.
     *
     * @param str              the input string
     * @param key              the secret key
     * @param isWithoutPadding whether to omit padding in the Base64 output
     * @return the HMAC-SHA256 hash value as a Base64 encoded string
     */
    public static String hmacSHA256(String str, String key, boolean isWithoutPadding) {
        return hmac(HMAC_SHA256, str, key, isWithoutPadding);
    }
}