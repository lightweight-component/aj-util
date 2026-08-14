package com.ajaxjs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpHelper {
    private String inputStr;

    private final Pattern inputRegexp;

    public RegExpHelper(String inputStr) {
        this(Pattern.compile(inputStr));
        this.inputStr = inputStr;
    }

    public RegExpHelper(Pattern inputRegexp) {
        this.inputRegexp = inputRegexp;
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
}
