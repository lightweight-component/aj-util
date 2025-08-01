package com.ajaxjs.util;

/**
 * Make log in a box
 */
public class BoxLogger {
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static final String NONE = "none";

    // 日志方框宽度
    private static final int BOX_WIDTH = 97;

    public final static String TRACE_KEY = "traceId";

    // 打印边框行
    public static String boxLine(char left, char fill, char right, String title) {
        int fillLen = BOX_WIDTH - 2 - title.length();
        int leftFill = fillLen / 2;
        int rightFill = fillLen - leftFill;

        return left + repeat(fill, leftFill) + title + repeat(fill, rightFill) + right;
    }

    // 打印内容行
    public static String boxContent(String key, String value) {
//         key + value + padding = BOX_WIDTH - 2
        int maxLen = BOX_WIDTH - 2 - key.length();
        String val = truncate(value, maxLen);
        int pad = BOX_WIDTH - 2 - key.length() - getDisplayWidth(val);

        return "│ " + key + val + repeat(' ', pad - 1) + "│";
    }

    // 重复字符
    public static String repeat(char c, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++)
            sb.append(c);

        return sb.toString();
    }

    // 截断字符串并加省略号
    public static String truncate(String s, int maxDisplayLen) {
        if (s == null)
            return "";
        int dlen = getDisplayWidth(s);

        if (dlen <= maxDisplayLen)
            return s;

        String ellipsis = "...";
        int keep = maxDisplayLen - ellipsis.length();

        if (keep <= 0)
            return ellipsis;

        int count = 0, i = 0;

        for (; i < s.length() && count < keep; i++) {
            char ch = s.charAt(i);
            count += isWideChar(ch) ? 2 : 1;
            if (count > keep) break;
        }

        return s.substring(0, i) + ellipsis;
    }

    // 获取字符串的显示宽度（中文2，英文1）
    public static int getDisplayWidth(String s) {
        if (s == null)
            return 0;

        int width = 0;
        for (char c : s.toCharArray())
            width += isWideChar(c) ? 2 : 1;

        return width;
    }

    // 判断是否宽字符（简化，只判断 CJK）
    public static boolean isWideChar(char ch) {
        return (ch >= 0x4E00 && ch <= 0x9FA5) || // CJK
                (ch >= 0xFF01 && ch <= 0xFF60) || // Fullwidth
                (ch >= 0x3000 && ch <= 0x303F);   // CJK Symbols
    }
}
