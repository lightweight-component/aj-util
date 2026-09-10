/**
 * Copyright sp42 frank@ajaxjs.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ajaxjs.util;

import java.util.*;

/**
 * Object Utility Helper - Provides common utility methods for working with Java objects,
 * collections, maps, and various data structures.
 *
 * <p>This class includes helper methods for:
 * - Text content checking and validation
 * - Collection and array emptiness checks
 * - Convenient factory methods for creating collections (maps, lists, sets)
 * - Capacity calculation utilities
 * <p>
 * It simplifies common object manipulation patterns across Java applications.
 */
public class ObjectHelper {
    /**
     * Checks if the given String has actual text content.
     * More specifically, returns {@code true} if the string is not {@code null},
     * its length is greater than 0, and it contains at least one non-whitespace character.
     *
     * @param str the String to check
     * @return {@code true} if the String has non-whitespace text content
     */
    public static boolean hasText(String str) {
        return str != null && !str.isEmpty() && containsText(str);
    }

    /**
     * Checks if the given String is empty or contains only whitespace.
     * This is the opposite of {@link #hasText(String)}.
     *
     * @param str the String to check
     * @return {@code true} if the String is empty or contains only whitespace
     */
    public static boolean isEmptyText(String str) {
        return !hasText(str);
    }

    /**
     * Checks if the given CharSequence contains any non-whitespace characters.
     *
     * @param str the CharSequence to check
     * @return {@code true} if the CharSequence contains at least one non-whitespace character
     */
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
     * @return {@code true} if the array is {@code null} or of zero lengths
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
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

    /**
     * An empty, immutable Map. Useful as a default or sentinel value when no parameters are needed.
     */
    public static final Map<String, Object> EMPTY_PARAMS_MAP = Collections.emptyMap();

    /**
     * Creates a new HashMap with a single key-value pair.
     *
     * @param k1  the first key
     * @param v1  the first value
     * @param <K> the key type
     * @param <V> the value type
     * @return the newly created HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = mapOf(1);
        map.put(k1, v1);

        return map;
    }

    /**
     * Creates a new HashMap with two key-value pairs.
     *
     * @param k1  the first key
     * @param v1  the first value
     * @param k2  the second key
     * @param v2  the second value
     * @param <K> the key type
     * @param <V> the value type
     * @return the newly created HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = mapOf(2);
        map.put(k1, v1);
        map.put(k2, v2);

        return map;
    }

    /**
     * Creates a new HashMap with three key-value pairs.
     *
     * @param k1  the first key
     * @param v1  the first value
     * @param k2  the second key
     * @param v2  the second value
     * @param k3  the third key
     * @param v3  the third value
     * @param <K> the key type
     * @param <V> the value type
     * @return the newly created HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = mapOf(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return map;
    }

    /**
     * Default load factor for hash maps, optimized for time and space efficiency.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Calculates the initial capacity for a HashMap based on the expected size.
     *
     * @param expectedSize the expected number of entries
     * @return the recommended initial capacity
     * @throws IllegalArgumentException if expectedSize is negative
     */
    public static int getInitialCapacity(int expectedSize) {
        if (expectedSize < 0)
            throw new IllegalArgumentException("Expected size must not be negative.");

        if (expectedSize < 3)
            return expectedSize + 1;

        if (expectedSize < (1 << 30))
            return (int) (expectedSize / 0.75f + 1.0f);

        return Integer.MAX_VALUE;
    }

    /**
     * Creates a HashMap optimized for the expected number of entries.
     * The initial capacity is calculated to minimize resizing operations.
     *
     * @param expectedSize the expected number of entries in the map
     * @param <K>          the key type
     * @param <V>          the value type
     * @return a new HashMap with optimal initial capacity
     */
    public static <K, V> Map<K, V> mapOf(int expectedSize) {
        return new HashMap<>(getInitialCapacity(expectedSize), DEFAULT_LOAD_FACTOR);
    }

    /**
     * Input multiple elements and returns a list of those elements.
     *
     * @param arr The elements
     * @param <T> The type of the elements
     * @return A new list containing the elements of the array
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... arr) {
        Objects.requireNonNull(arr, "arr");

        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(arr)));
    }

    /**
     * Creates an immutable Set containing the specified elements.
     * Similar to Java 9+ Set.of() but available in earlier Java versions.
     * Automatically removes duplicates and returns an unmodifiable collection.
     *
     * @param elements the elements to include in the set
     * @param <T>      the element type
     * @return an immutable Set containing the specified elements
     * @throws NullPointerException     if the elements array is null
     * @throws IllegalArgumentException if any element is null
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        Objects.requireNonNull(elements, "elements");
        Set<T> set = new HashSet<>(getInitialCapacity(elements.length));

        for (T element : elements) {
            if (element == null)
                throw new IllegalArgumentException("Set elements cannot contain null");

            set.add(element);
        }

        return Collections.unmodifiableSet(set);
    }

    /**
     * 合并两个字节数组
     *
     * @param a 数组a
     * @param b 数组b
     * @return 新合并的数组
     */
    public static byte[] concat(byte[] a, byte[] b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");

        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }
}
