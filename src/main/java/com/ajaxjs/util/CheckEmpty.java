package com.ajaxjs.util;

import java.util.Collection;
import java.util.Map;

/**
 * Check the value if it is empty.
 * Supports data type: String, Collection, Map, Array.
 */
public class CheckEmpty {
    public static boolean hasText(String str) {
        return str != null && !str.isEmpty() && containsText(str);
    }

    public static boolean isEmptyText(String str) {
        return !hasText(str);
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i)))
                return true;
        }

        return false;
    }

    /**
     * Determine whether the given array is empty:
     * i.e. {@code null} or of zero length.
     *
     * @param array the array to check
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
}
