package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static com.ajaxjs.util.StrUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TestStrUtil {
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
