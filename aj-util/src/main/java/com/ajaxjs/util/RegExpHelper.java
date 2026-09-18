package com.ajaxjs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object-oriented helper for a compiled regular expression.
 *
 * <p>Creating an instance from a regular-expression string reuses a shared,
 * thread-safe cache of compiled {@link Pattern Patterns}. Supplying a
 * {@link Pattern} directly never changes that cache.</p>
 */
public class RegExpHelper {
    /**
     * Shared cache for patterns created from regular-expression strings.
     */
    private static final ConcurrentMap<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    /**
     * Compiled pattern used by this helper.
     */
    private final Pattern inputRegexp;

    /**
     * Creates a helper from a regular-expression string, reusing its cached
     * compiled pattern when available.
     *
     * @param regexp regular-expression string
     */
    public RegExpHelper(String regexp) {
        this(getPattern(regexp));
    }

    /**
     * Creates a helper from an already compiled pattern.
     *
     * @param inputRegexp compiled pattern
     */
    public RegExpHelper(Pattern inputRegexp) {
        this.inputRegexp = Objects.requireNonNull(inputRegexp, "inputRegexp");
    }

    /**
     * Gets a compiled pattern from the shared cache, compiling it only once
     * for each distinct regular-expression string.
     *
     * @param regexp regular-expression string
     * @return cached compiled pattern
     */
    public static Pattern getPattern(String regexp) {
        Objects.requireNonNull(regexp, "regexp");

        return PATTERN_CACHE.computeIfAbsent(regexp, Pattern::compile);
    }

    /**
     * Returns the compiled pattern used by this helper.
     *
     * @return this helper's compiled pattern
     */
    public Pattern getPattern() {
        return inputRegexp;
    }

    /**
     * Tests if the string contains a match of the given regular expression
     * <p>
     * Uses find() which searches for any occurrence of the pattern in the string
     *
     * @param str the string to test
     * @return true if the string contains a match of the pattern, false otherwise
     */
    public boolean contains(String str) {
        return getMatcher(str).find();
    }

    /**
     * Determines if the entire string matches the given regular expression
     * <p>
     * Uses matches() which requires the pattern to match the entire input string
     *
     * @param str the string to test
     * @return true if the entire string matches the pattern, false otherwise
     */
    public boolean fullMatch(String str) {
        return getMatcher(str).matches();
    }

    public Matcher getMatcher(String str) {
        return inputRegexp.matcher(str);
    }

    /**
     * 使用正则的快捷方式。可指定分组
     *
     * @param str        测试的字符串
     * @param groupIndex 分组 id，若为 -1 则取最后一个分组
     * @return 匹配结果
     */
    public String match(String str, int groupIndex) {
        Matcher m = getMatcher(str);

        if (groupIndex == -1)
            groupIndex = m.groupCount();

        return m.find() ? m.group(groupIndex) : null;
    }

    /**
     * 使用正则的快捷方式
     *
     * @param str 测试的字符串
     * @return 匹配结果，只有匹配第一个
     */
    public String match(String str) {
        return match(str, 0);
    }

    /**
     * 返回所有匹配项
     *
     * @param str 测试的字符串
     * @return 匹配结果
     */
    public String[] matchAll(String str) {
        Matcher m = getMatcher(str);
        List<String> list = new ArrayList<>();

        while (m.find())
            list.add(m.group());

        return list.toArray(new String[0]);
    }

    /**
     * Determines if the entire string matches the given regular expression
     * <p>
     * Uses matches() which requires the pattern to match the entire input string
     *
     * @param regexp the regular expression to compile and use for matching
     * @param str    the string to test
     * @return true if the entire string matches the pattern, false otherwise
     */
    public static boolean match(String regexp, String str) {
        return new RegExpHelper(regexp).fullMatch(str);
    }

    /**
     * 使用正则的快捷方式。可指定分组
     *
     * @param regexp     正则
     * @param str        测试的字符串
     * @param groupIndex 分组 id，若为 -1 则取最后一个分组
     * @return 匹配结果
     */
    public static String regMatch(String regexp, String str, int groupIndex) {
        return new RegExpHelper(regexp).match(str, groupIndex);
    }
}
