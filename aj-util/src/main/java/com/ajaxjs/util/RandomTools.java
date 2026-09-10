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
 * Random Tools - Utility class for generating various types of random values including
 * numbers, strings, and UUIDs with version 7 support.
 *
 * <p>This class provides thread-safe random generation using ThreadLocalRandom
 * for performance and SecureRandom for cryptographic-strength randomness.
 */
public class RandomTools {
    /**
     * Generate a six-digit random integer.
     * This method is a convenience wrapper for generateNumber(6).
     *
     * @return Random integer in the range 100 thousand to 999999
     */
    public static int generateNumber() {
        return generateNumber(6);
    }

    /**
     * Generate a specified-digit random integer.
     * For example, if numDigits is 3, the result will be between 100 and 999.
     *
     * @param numDigits The number of digits (must be between 1 and 9)
     * @return Random integer with exactly the specified number of digits
     * @throws IllegalArgumentException if numDigits is outside the supported range
     */
    public static int generateNumber(int numDigits) {
        if (numDigits <= 0 || numDigits > 9)
            throw new IllegalArgumentException("The number of digits must be between 1 and 9.");

        int min = (int) Math.pow(10, numDigits - 1);
        int max = (int) Math.pow(10, numDigits) - 1;

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Character pool containing alphanumeric characters for random string generation.
     * This includes both uppercase and lowercase letters and digits 0-9.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generate a six-character random string containing alphanumeric characters.
     * This method is a convenience wrapper for generateRandomString(6).
     *
     * @return Random string of six characters from the alphanumeric character pool
     */
    public static String generateRandomString() {
        return generateRandomString(6);
    }

    /**
     * Generate a random string of specified length containing alphanumeric characters.
     * The string consists of uppercase letters, lowercase letters, and digits.
     *
     * @param length The length of the string to be generated (must be greater than 0)
     * @return Random string containing characters from the alphanumeric character pool
     * @throws IllegalArgumentException if length is less than or equal to 0
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
     * Shared cryptographically strong random number generator.
     * <p>
     * This instance may be reused by other utility classes that require
     * cryptographically strong random values.
     */
    public static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a UUIDv7 string.
     * <p>
     * The UUID contains a 48-bit Unix timestamp in milliseconds
     * followed by random bits, as defined by RFC 9562.
     *
     * @param withHyphen whether the UUID string contains hyphens
     * @return UUIDv7 string, 36 characters with hyphens or 32 characters without hyphens.
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
     * Generate a UUIDv7 string without hyphen.
     *
     * @return UUIDv7 string without hyphens, 32 characters long.
     */
    public static String uuidV7() {
        return uuidV7(false);
    }

    /**
     * Show the timestamp of a UUIDv7.
     *
     * @param uuidStr UUIDv7
     * @return Date
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
