package com.ajaxjs.util;

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
}
