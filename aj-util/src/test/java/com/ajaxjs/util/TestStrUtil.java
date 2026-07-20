package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ajaxjs.util.StrUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestStrUtil {
    final static String str = "中国";

    public static void main(String[] args) {
        System.out.println("你好 %s".replace("%s", "张三"));
    }

    @Test
    void testCharCount() {
        assertEquals(3, charCount("aaa", "a"));
        assertEquals(2, charCount("aaa", "aa"));
        assertEquals(2, charCount("abcabc", "abc"));
        assertEquals(0, charCount("abc", "x"));
        assertEquals(0, charCount("abc", ""));
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
    void messageFormat() {
        String result = MessageFormat.format("您好{0}，晚上好！您目前余额：{1,number,#.##}元，积分：{2}", "张三", 10.155, 10);
        System.out.println(result);
    }

    @Test
    void testJoin() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        System.out.println(join(list, "&"));
    }
}
