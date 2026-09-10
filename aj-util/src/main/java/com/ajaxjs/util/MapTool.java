package com.ajaxjs.util;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Map Conversion Utility - Provides comprehensive map manipulation operations including
 * joining maps to strings, converting between different map formats, XML serialization.
 */
@Slf4j
public class MapTool {
    /**
     * Map 转换为 String
     *
     * @param map Map 结构，Key 必须为 String 类型
     * @param div 分隔符
     * @param fn  对 Value 的处理函数，返回类型 T
     * @param <T> Key 的类型
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
        return join(map, div, v -> v == null ? null : v.toString());
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
     *
     * @param pairs 结对的字符串数组，包含 = 字符分隔 key 和 value
     * @param fn    对 Value 的处理函数，返回类型 Object
     * @return Map 对象
     */
    public static Map<String, Object> toMap(String[] pairs, Function<String, Object> fn) {
        if (ObjectHelper.isEmpty(pairs))
            return null;

        Map<String, Object> map = new HashMap<>();

        for (String pair : pairs) {
            if (!pair.contains("="))
                throw new IllegalArgumentException("没有 = 不能转化为 map");

            String[] column = pair.split("=", 2);

            if (column.length >= 2)
                map.put(column[0], fn == null ? column[1] : fn.apply(column[1]));
            else
                map.put(column[0], "");// 没有 等号后面的，那设为空字符串
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
        if (ObjectHelper.isEmpty(columns))
            return null;

        if (columns.length != values.length)
            throw new UnsupportedOperationException("两个数组 size 不一样");

        Map<String, Object> map = new HashMap<>();
        int i = 0;

        for (String column : columns)
            map.put(column, fn.apply(values[i++]));

        return map;
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
