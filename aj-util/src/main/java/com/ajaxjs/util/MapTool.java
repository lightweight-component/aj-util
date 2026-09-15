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

import java.util.*;
import java.util.function.Function;

/**
 * Map Conversion Utility - Provides comprehensive map manipulation operations including
 * joining maps to strings, converting between different map formats.
 */
public class MapTool {
    /**
     * Map 转换为 String
     *
     * @param map Map 结构，Key 必须为 String 类型
     * @param div 分隔符
     * @param fn  对 Value 的处理函数，返回类型 T
     * @param <T> Value 的类型
     * @return Map 序列化字符串
     */
    public static <T> String join(Map<String, T> map, String div, Function<T, String> fn) {
        String[] pairs = new String[map.size()];
        int i = 0;

        for (String key : map.keySet())
            pairs[i++] = key + "=" + fn.apply(map.get(key));

        return String.join(div, pairs);
    }

    /**
     * 将指定的 Map 对象转换为字符串，使用指定的分隔符分隔每个元素。
     *
     * @param map 要转换的 Map 对象
     * @param fn  将 Map 中的值转换为字符串的函数
     * @param <T> Map 中的值的类型
     * @return 转换后的字符串
     */
    public static <T> String join(Map<String, T> map, Function<T, String> fn) {
        return join(map, "&", fn);
    }

    /**
     * 将 Map 中的值使用指定的分隔符进行拼接。
     *
     * @param map 要拼接的 Map 对象
     * @param div 分隔符
     * @param <T> Map 中元素的类型
     * @return 拼接后的字符串
     */
    public static <T> String join(Map<String, T> map, String div) {
        return join(map, div, v -> v == null ? CommonConstant.EMPTY_STRING : v.toString());
    }

    /**
     * 将给定的 Map 对象转换为字符串，使用指定的分隔符将键值对连接起来
     *
     * @param map 要转换的 Map 对象
     * @param <T> 键值对的类型
     * @return 连接后的字符串
     */
    public static <T> String join(Map<String, T> map) {
        return join(map, "&");
    }

    /**
     * String[] 转换为 Map
     * Null entries are ignored.
     *
     * @param pairs 结对的字符串数组，包含 = 字符分隔 key 和 value
     * @param fn    对 Value 的处理函数，返回类型 Object
     * @return Map 对象
     */
    public static Map<String, Object> toMap(String[] pairs, Function<String, Object> fn) {
        if (ObjectHelper.isEmpty(pairs))
            return new HashMap<>();

        Map<String, Object> map = new HashMap<>(ObjectHelper.getInitialCapacity(pairs.length));

        for (String pair : pairs) {
            if (pair == null)
                continue;

            if (!pair.contains("="))
                throw new IllegalArgumentException("Pair must contain '=': " + pair);

            String[] column = pair.split("=", 2);
            map.put(column[0], fn == null ? column[1] : fn.apply(column[1]));

        }

        return map;
    }

    /**
     * String[] 转换为 Map，key 与 value 分别一个数组
     *
     * @param columns 结对的键数组
     * @param values  结对的值数组
     * @param fn      对 Value 的处理函数，返回类型 Object
     * @return Map 对象
     */
    public static Map<String, Object> toMap(String[] columns, String[] values, Function<String, Object> fn) {
        Objects.requireNonNull(columns, "toMap.columns");
        Objects.requireNonNull(values, "toMap.values");

        if (ObjectHelper.isEmpty(columns))
            return new HashMap<>();

        if (columns.length != values.length)
            throw new IllegalArgumentException("columns and values must have the same length");

        Map<String, Object> map = new HashMap<>(ObjectHelper.getInitialCapacity(columns.length));

        for (int i = 0; i < columns.length; i++) {
            Object value = fn == null ? values[i] : fn.apply(values[i]);
            map.put(columns[i], value);
        }

        return map;
    }

    /**
     * Parses an application/x-www-form-urlencoded query string.
     * <p>
     * Duplicate keys are overwritten by later values.
     *
     * @param query the string to be a map
     * @return map
     */
    public static Map<String, String> toMap(String query) {
        Objects.requireNonNull(query, "toMap.query");
        String[] fields = query.split("&");
        Map<String, String> res = new HashMap<>(ObjectHelper.getInitialCapacity(fields.length));

        for (String field : fields) {
            String[] keyValue = field.split("=", 2);

            if (keyValue.length == 2) {
                String key = new UrlCodec(keyValue[0]).decodeForm();
                String value = new UrlCodec(keyValue[1]).decodeForm();

                res.put(key, value);
            }
        }

        return res;
    }

    /**
     * 万能 Map 转换器，为了泛型的转换而设的一个方法，怎么转换在 fn 中处理
     *
     * @param map 原始 Map，key 必须为 String 类型
     * @param fn  转换函数
     * @param <K> Key 的类型
     * @param <T> 返回 value 的类型
     * @return 转换后的 map
     */
    public static <T, K> Map<String, T> as(Map<String, K> map, Function<K, T> fn) {
        Map<String, T> _map = new HashMap<>();
        map.forEach((k, v) -> _map.put(k, v == null ? null : fn.apply(v)));

        return _map;
    }

    /**
     * Flattens a nested Map into a single-level Map.
     * <p>
     * Nested keys are joined with '.'.
     * Empty maps are preserved as values.
     * Circular references are rejected.
     *
     * @param nestedMap the nested map
     * @return flattened map
     */
    public static Map<String, Object> flatten(Map<?, ?> nestedMap) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (nestedMap == null)
            return result;

        Set<Map<?, ?>> visiting = Collections.newSetFromMap(new IdentityHashMap<>());
        flatten(nestedMap, "", result, visiting);

        return result;
    }

    private static void flatten(Map<?, ?> current, String prefix, Map<String, Object> result, Set<Map<?, ?>> visiting) {
        if (!visiting.add(current))
            throw new IllegalArgumentException("Circular Map reference detected at: " + prefix);

        try {
            for (Map.Entry<?, ?> entry : current.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String escapedKey = key.replace("\\", "\\\\").replace(".", "\\.");
                String path = prefix.isEmpty() ? escapedKey : prefix + "." + escapedKey;
                Object value = entry.getValue();

                if (value instanceof Map) {
                    Map<?, ?> child = (Map<?, ?>) value;

                    if (child.isEmpty())
                        result.put(path, child);
                    else
                        flatten(child, path, result, visiting);
                } else
                    result.put(path, value);
            }
        } finally {
            visiting.remove(current);
        }
    }
}
