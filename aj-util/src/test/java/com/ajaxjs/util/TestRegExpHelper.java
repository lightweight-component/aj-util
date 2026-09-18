package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TestRegExpHelper {
    @Test
    void stringConstructorsReuseTheSharedCompiledPatternCache() {
        RegExpHelper first = new RegExpHelper("(\\d+)");
        RegExpHelper second = new RegExpHelper("(\\d+)");

        assertSame(first.getPattern(), second.getPattern());
        assertSame(first.getPattern(), RegExpHelper.getPattern("(\\d+)"));
    }

    @Test
    void supportsTheObjectOrientedMatchingOperations() {
        RegExpHelper helper = new RegExpHelper(Pattern.compile("a(b)"));

        assertTrue(helper.contains("zabz"));
        assertTrue(helper.fullMatch("ab"));
        assertEquals("b", helper.match("ab", -1));
        assertArrayEquals(new String[]{"ab", "ab"}, helper.matchAll("ab-ab"));
    }

    @Test
    void testRegMatch() {
        assertEquals(RegExpHelper.regMatch("^a", "abc", 0), "a");// 匹配结果，只有匹配第一个
        assertEquals(RegExpHelper.regMatch("^a", "abc", 0), "a");// 可指定分组
        assertEquals(RegExpHelper.regMatch("^a(b)", "abc", 1), "b");
    }
}
