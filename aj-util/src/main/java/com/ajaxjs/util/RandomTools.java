package com.ajaxjs.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random Tools.
 */
public class RandomTools {
    /**
     * Generate a six-digit random integer.
     *
     * @return Random integer
     */
    public static int generateNumber() {
        return generateNumber(6);
    }

    /**
     * Generate a specified-digit random integer.
     *
     * @param numDigits The number of digits
     * @return Random integer
     */
    public static int generateNumber(int numDigits) {
        if (numDigits <= 0)
            throw new IllegalArgumentException("The number of digits must be greater than zero.");

        int min = (int) Math.pow(10, numDigits - 1);
        int max = (int) Math.pow(10, numDigits) - 1;

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * noinspection SpellCheckingInspection
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generate a six-length random string.
     *
     * @return Random string
     */
    public static String generateRandomString() {
        return generateRandomString(6);
    }

    /**
     * Generate a specified-length random string.
     *
     * @param length The length of the string to be generated
     * @return Random string
     */
    public static String generateRandomString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(STR.charAt(number));
        }

        return sb.toString();
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a UUIDv7.
     *
     * @return UUIDv7
     */
    public static UUID uuid() {
        byte[] value = randomBytes();
        ByteBuffer buf = ByteBuffer.wrap(value);
        long high = buf.getLong();
        long low = buf.getLong();

        return new UUID(high, low);
    }

    /**
     * Generate a UUIDv7 string without hyphen.
     *
     * @return UUIDv7 string without hyphen.
     */

    public static String uuidStr() {
        return uuid().toString().replace(CommonConstant.HYPHEN_STR, CommonConstant.EMPTY_STRING);
    }

    private static byte[] randomBytes() {
        // random bytes
        byte[] value = new byte[16];
        RANDOM.nextBytes(value);

        // current timestamp in ms
        ByteBuffer timestamp = ByteBuffer.allocate(Long.BYTES);
        timestamp.putLong(System.currentTimeMillis());

        // timestamp
        System.arraycopy(timestamp.array(), 2, value, 0, 6);

        // version and variant
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        return value;
    }

    /**
     * Show the timestamp of a UUIDv7.
     *
     * @param uuidStr UUIDv7
     * @return Date
     */
    public static Date showTime(String uuidStr) {
        long msb = UUID.fromString(uuidStr).getMostSignificantBits();
        long timestamp = (msb >>> 16) & 0xFFFFFFFFFFFFL;

        return new Date(timestamp);
    }
}
