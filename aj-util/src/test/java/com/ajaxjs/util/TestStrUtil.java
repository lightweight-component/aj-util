package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static com.ajaxjs.util.StrUtil.join;
import static com.ajaxjs.util.StrUtil.simpleTpl;
import static org.junit.jupiter.api.Assertions.*;

class TestStrUtil {
    /**
     * 简单模板替换方法。根据 Map 中的数据进行替换。
     * 与 simpleTpl 方法的区别在于这里将 null 值替换为字符串 "null"。
     *
     * @param template 待替换的字符串模板
     * @param data     存放替换数据的 Map
     * @return 替换后的字符串
     */
    public static String simpleTpl2(String template, Map<String, Object> data) {
        String result = template;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null)
                value = "null";

            String placeholder = "#{" + key + "}";
            result = result.replace(placeholder, value.toString());
        }

        return result;
    }

    /**
     * 统计文本中某个字符串出现的次数
     * <p>
     * 支持重叠匹配，例如 {@code charCount("aaa", "aa")} 返回 2。
     * 空匹配串返回 0。
     *
     * @param str   输入的字符串
     * @param _char 待统计的字符串
     * @return 出现次数
     */
    public static int charCount(String str, String _char) {
        if (_char.isEmpty())
            return 0;

        int count = 0, index = 0;

        while ((index = str.indexOf(_char, index)) >= 0) {
            count++;
            index++;
        }

        return count;
    }

    /**
     * 判断一个字符串是否属于指定的字符串数组中
     *
     * @param word 待判断字符串
     * @param arr  指定字符串数组
     * @return 如果字符串属于数组中，则返回 true；否则返回 false
     */
    public static boolean isWordOneOfThem(String word, String[] arr) {
        for (String str : arr) {
            if (word.equals(str))
                return true;
        }

        return false;
    }

    /**
     * 判断一个字符串是否属于指定的字符串列表中
     *
     * @param word 待判断字符串
     * @param list 指定字符串列表
     * @return 如果字符串属于列表中，则返回 true；否则返回 false
     */
    public static boolean isWordOneOfThem(String word, List<String> list) {
        return isWordOneOfThem(word, list.toArray(new String[0]));
    }

    final static String str = "中国";

    public static class FailingBean {
        public String getValue() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void testCharCount() {
        assertEquals(3, charCount("aaa", "a"));
        assertEquals(2, charCount("aaa", "aa"));
        assertEquals(2, charCount("abcabc", "abc"));
        assertEquals(0, charCount("abc", "x"));
        assertEquals(0, charCount("abc", ""));
    }

    /**
     * 字符串左填充方法
     * <p>
     * 例如: leftPad("12345", 10, "@")，输出："@@@@@12345"
     *
     * @param str   待填充字符串
     * @param len   总长度
     * @param _char 填充字符
     * @return 左填充后的字符串
     */
    public static String leftPad(String str, int len, String _char) {
        if (str.length() >= len)
            return str;
        if (_char == null || _char.isEmpty())
            throw new IllegalArgumentException("Padding string must not be null or empty.");

        int paddingLength = len - str.length();
        StringBuilder result = new StringBuilder(len);
        while (result.length() < paddingLength)
            result.append(_char);

        if (result.length() > paddingLength)
            result.setLength(paddingLength);

        return result.append(str).toString();
    }

    /**
     * 将列表中的元素使用指定的分隔符连接成一个字符串，并返回连接后的字符串
     *
     * @param <T>  数组类型
     * @param list 任何类型的列表
     * @param str  字符串类型的分隔符
     * @return 连接后的字符串
     */
    public static <T> String joinAnyList(List<T> list, String str) {
        Object[] objectArray = list.toArray();
        @SuppressWarnings("unchecked")
        T[] array = Arrays.copyOf(objectArray, objectArray.length, (Class<? extends T[]>) objectArray.getClass());

        return join(array, str);
    }

    @Test
    void testLeftPad() {
        assertEquals("@@@@@12345", leftPad("12345", 10, "@"));
        assertEquals("$$$a b", leftPad("a b", 6, "$"));
        assertEquals("\\\\a b", leftPad("a b", 5, "\\"));
        assertEquals("abaX", leftPad("X", 4, "ab"));
        assertThrows(IllegalArgumentException.class, () -> leftPad("x", 2, ""));
    }

    @Test
    void testSimpleTplWithReplacementSpecialCharacters() {
        Map<String, Object> params = new HashMap<>();
        params.put("value", "$1\\path");

        assertEquals("value=$1\\path", simpleTpl("value=${value}", params));
    }

    @Test
    void simpleTplSkipsWriteOnlyBeanProperties() {
        class WriteOnlyBean {
            @SuppressWarnings("unused")
            public void setSecret(String secret) {
            }
        }

        assertEquals("unchanged", simpleTpl("unchanged", new WriteOnlyBean()));
    }

    @Test
    void simpleTplReportsFailingBeanPropertyAndCause() {
        RuntimeException error =
                assertThrows(RuntimeException.class, () -> simpleTpl("#{value}", new FailingBean()));
        assertTrue(error.getMessage().contains("value"));
        assertSame(IllegalStateException.class, error.getCause().getClass());
        assertEquals("boom", error.getCause().getMessage());
    }

    @Test
    void testJoin() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals("a&b&c", join(list, "&"));
        assertEquals("[a], [b], [c]", join(list, "[%s]", ", "));
        assertEquals("a&&c", join(new String[]{"a", null, "c"}, "&"));
        assertEquals("a&&c", join(Arrays.asList("a", null, "c"), "&"));
        assertEquals("[a], [], [c]", join(Arrays.asList("a", null, "c"), "[%s]", ", "));
    }
}
