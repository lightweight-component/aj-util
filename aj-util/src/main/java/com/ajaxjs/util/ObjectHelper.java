/*
 * Copyright (C) 2025 Frank Cheung<frank@ajaxjs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.util;

import java.util.*;

/**
 * A helper for Java Object.
 */
public class ObjectHelper {
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
     * Dummy Map
     */
    public static final Map<String, Object> EMPTY_PARAMS_MAP = Collections.unmodifiableMap(new HashMap<>());

    /**
     * 创建一个新的 HashMap
     *
     * @param k1  键1
     * @param v1  值1
     * @param <K> k1 类型
     * @param <V> v1 类型
     * @return 新创建的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);

        return map;
    }

    /**
     * 创建一个新的 HashMap
     *
     * @param k1  键1
     * @param v1  值1
     * @param k2  键2
     * @param v2  值2
     * @param <K> k1 类型
     * @param <V> v1 类型
     * @return 新创建的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);

        return map;
    }

    /**
     * 创建一个新的 HashMap
     *
     * @param k1  键1
     * @param v1  值1
     * @param k2  键2
     * @param v2  值2
     * @param k3  键3
     * @param v3  值3
     * @param <K> k1 类型
     * @param <V> v1 类型
     * @return 新创建的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = mapOf(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return map;
    }

    /**
     * Creates a HashMap with a specified expected number of entries.
     * The initial capacity and load factor are calculated to minimize resizing.
     *
     * @param expectedSize the expected number of entries in the map
     * @return a new HashMap with optimal initial capacity and load factor
     */
    public static <K, V> Map<K, V> mapOf(int expectedSize) {
        // Calculate the initial capacity as the next power of two greater than or equal to expectedSize / default_load_factor
        float defaultLoadFactor = 0.75f;
        int initialCapacity = (int) Math.ceil(expectedSize / defaultLoadFactor);
        initialCapacity = Integer.highestOneBit(initialCapacity - 1) << 1;

        // Ensure that the initial capacity is at least 16 (the default capacity)
        if (initialCapacity < 16)
            initialCapacity = 16;

        return new HashMap<>(initialCapacity, defaultLoadFactor);// Create and return the HashMap with the calculated initial capacity and default load factor
    }

    /**
     * Input multiple elements and returns a list of those elements.
     * Just like `List.of()` in Java 9.
     *
     * @param arr The elements
     * @param <T> The type of the elements
     * @return A new list containing the elements of the array
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... arr) {
        return Collections.unmodifiableList(Arrays.asList(arr));
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
