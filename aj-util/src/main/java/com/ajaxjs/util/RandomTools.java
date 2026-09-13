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

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilities for generating random integers, alphanumeric strings, and version 7 UUIDs,
 * and extracting timestamps from version 7 UUIDs.
 * <p>
 * Number and string generation use {@link ThreadLocalRandom} and are not suitable for
 * passwords, authentication codes, or other security-sensitive values. UUID generation
 * uses the shared {@link SecureRandom} instance exposed as {@link #RANDOM}.
 * The random generators support concurrent use, but generated values are not guaranteed
 * to be unique.
 *
 * @see ThreadLocalRandom
 * @see SecureRandom
 * @see UUID
 */
public class RandomTools {
    /**
     * Generates a uniformly distributed six-digit positive random integer.
     * <p>
     * Equivalent to {@link #generateNumber(int) generateNumber(6)}.
     * This method does not provide cryptographically secure randomness.
     *
     * @return a random integer from {@code 100000} to {@code 999999}, inclusive
     */
    public static int generateNumber() {
        return generateNumber(6);
    }

    /**
     * Generates a uniformly distributed positive random integer with the specified number
     * of decimal digits.
     * <p>
     * The range is {@code 10^(numDigits - 1)} through {@code 10^numDigits - 1}, inclusive.
     * For example, three digits produce values from {@code 100} to {@code 999}; one digit
     * produces values from {@code 1} to {@code 9}, excluding zero.
     * This method does not provide cryptographically secure randomness.
     *
     * @param numDigits the number of decimal digits, from {@code 1} to {@code 9}, inclusive
     * @return a positive random integer with exactly {@code numDigits} decimal digits
     * @throws IllegalArgumentException if {@code numDigits} is less than {@code 1} or greater than {@code 9}
     */
    public static int generateNumber(int numDigits) {
        if (numDigits <= 0 || numDigits > 9)
            throw new IllegalArgumentException("The number of digits must be between 1 and 9.");

        int min = (int) Math.pow(10, numDigits - 1);
        int max = (int) Math.pow(10, numDigits) - 1;

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * The 62 ASCII characters available for random string generation:
     * lowercase letters {@code a-z}, uppercase letters {@code A-Z}, and digits {@code 0-9}.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generates a six-character random alphanumeric string.
     * <p>
     * Equivalent to {@link #generateRandomString(int) generateRandomString(6)}.
     * This method is not suitable for security-sensitive tokens.
     *
     * @return a six-character string containing only ASCII letters and digits
     */
    public static String generateRandomString() {
        return generateRandomString(6);
    }

    /**
     * Generates a random alphanumeric string of the specified length.
     * <p>
     * Each character is selected uniformly from {@code a-z}, {@code A-Z}, and {@code 0-9}.
     * Characters may repeat, and no particular character category is guaranteed to appear.
     * This method uses {@link ThreadLocalRandom} and is not suitable for passwords,
     * authentication codes, or security-sensitive tokens.
     *
     * @param length the number of characters to generate; must be positive
     * @return a string of exactly {@code length} ASCII letters and digits
     * @throws IllegalArgumentException if {@code length} is less than or equal to zero
     */
    public static String generateRandomString(int length) {
        if (length <= 0)
            throw new IllegalArgumentException("The string length must be greater than zero.");

        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(length);
        int strLength = STR.length();

        for (int i = 0; i < length; i++) {
            int number = random.nextInt(strLength);
            sb.append(STR.charAt(number));
        }

        return sb.toString();
    }

    /**
     * Shared, thread-safe cryptographically strong random number generator.
     * <p>
     * Used by {@link #uuidV7(boolean)} for the random portion of UUIDs and available to
     * other callers requiring secure randomness. Number and string generation methods
     * in this class do not use this instance.
     */
    public static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a version 7 UUID string using the current system time and secure random bits.
     * <p>
     * As defined by RFC 9562, the UUID contains a 48-bit Unix timestamp in milliseconds,
     * the version and variant bits, and 74 random bits supplied by {@link #RANDOM}.
     * The timestamp is obtained from {@link System#currentTimeMillis()}.
     * <p>
     * UUIDs are time-ordered by their timestamp portion, but this implementation does not
     * guarantee monotonic ordering within the same millisecond or when the system clock
     * moves backwards. UUIDs expose their generation timestamp and should not be treated
     * as secret authentication tokens.
     *
     * @param withHyphen {@code true} for the standard {@code 8-4-4-4-12} hyphenated format;
     *                   {@code false} to omit hyphens
     * @return a lowercase hexadecimal UUIDv7 string, 36 characters with hyphens or 32 without
     * @see #uuidV7()
     * @see #showTime(String)
     */
    public static String uuidV7(boolean withHyphen) {
        byte[] value = new byte[16];
        RANDOM.nextBytes(value);

        long timestamp = System.currentTimeMillis();

        value[0] = (byte) (timestamp >>> 40);
        value[1] = (byte) (timestamp >>> 32);
        value[2] = (byte) (timestamp >>> 24);
        value[3] = (byte) (timestamp >>> 16);
        value[4] = (byte) (timestamp >>> 8);
        value[5] = (byte) timestamp;

        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        ByteBuffer buf = ByteBuffer.wrap(value);
        String uuid = new UUID(buf.getLong(), buf.getLong()).toString();

        return withHyphen ? uuid : uuid.replace(CommonConstant.HYPHEN_STR, CommonConstant.EMPTY_STRING);
    }

    /**
     * Generates a version 7 UUID string without hyphens.
     * <p>
     * Equivalent to {@link #uuidV7(boolean) uuidV7(false)}.
     *
     * @return a 32-character lowercase hexadecimal UUIDv7 string
     */
    public static String uuidV7() {
        return uuidV7(false);
    }

    /**
     * Extracts the Unix timestamp embedded in a version 7 UUID.
     * <p>
     * Accepts the standard hyphenated UUID format or a 32-character hexadecimal string
     * without hyphens. For a 32-character input, hyphens are inserted before parsing with
     * {@link UUID#fromString(String)}. Whitespace is not trimmed.
     * <p>
     * The UUID version must be {@code 7}; the variant is not explicitly validated.
     * The returned timestamp is the value stored in the UUID and does not prove when
     * the UUID was actually generated.
     *
     * @param uuidStr the non-null UUIDv7 string to parse
     * @return a new {@link Date} representing the embedded milliseconds since the Unix epoch
     * @throws NullPointerException     if {@code uuidStr} is {@code null}
     * @throws IllegalArgumentException if the input cannot be parsed by {@link UUID#fromString(String)}
     *                                  or the UUID version is not {@code 7}
     */
    public static Date showTime(String uuidStr) {
        Objects.requireNonNull(uuidStr, "uuidStr");

        if (uuidStr.length() == 32) {
            uuidStr = uuidStr.substring(0, 8) + "-"
                    + uuidStr.substring(8, 12) + "-"
                    + uuidStr.substring(12, 16) + "-"
                    + uuidStr.substring(16, 20) + "-"
                    + uuidStr.substring(20);
        }

        UUID uuid = UUID.fromString(uuidStr);

        if (uuid.version() != 7)
            throw new IllegalArgumentException("Not a UUIDv7: " + uuidStr);

        long timestamp = (uuid.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;

        return new Date(timestamp);
    }
}
