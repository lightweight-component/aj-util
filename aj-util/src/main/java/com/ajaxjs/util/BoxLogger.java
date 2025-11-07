package com.ajaxjs.util;

/**
 * Box Logger Utility
 * <p>
 * Provides methods for formatting and displaying log messages in a boxed layout with
 * support for ANSI color codes, string truncation, and proper handling of wide characters
 * (like Chinese characters which take double width in display).
 */
public class BoxLogger {
    /**
     * ANSI escape code for green text
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * ANSI escape code for yellow text
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * ANSI escape code for blue text
     */
    public static final String ANSI_BLUE = "\u001B[34m";

    /**
     * ANSI escape code for red text
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * ANSI escape code to reset text formatting
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * Constant representing no color or style
     */
    public static final String NONE = "none";

    /**
     * Default width of the log box
     */
    private static final int BOX_WIDTH = 97;

    /**
     * Key for trace ID in logs
     */
    public final static String TRACE_KEY = "traceId";

    /**
     * Key for business action in logs
     */
    public final static String BIZ_ACTION = "bizAction";

    /**
     * Creates a box borderline with a centered title
     *
     * @param left  the left border character
     * @param fill  the fill character for the border
     * @param right the right border character
     * @param title the title text to a center in the border
     * @return a formatted border line string
     */
    public static String boxLine(char left, char fill, char right, String title) {
        int fillLen = BOX_WIDTH - 2 - title.length();
        int leftFill = fillLen / 2;
        int rightFill = fillLen - leftFill;

        return left + repeat(fill, leftFill) + title + repeat(fill, rightFill) + right;
    }

    /**
     * Creates a box content line with a key-value pair
     *
     * @param key   the key text to display
     * @param value the value text to display, will be truncated if too long
     * @return a formatted content line string
     */
    public static String boxContent(String key, String value) {
        int maxLen = BOX_WIDTH - 2 - key.length();
        String val = truncate(value, maxLen);
        int pad = BOX_WIDTH - 2 - key.length() - getDisplayWidth(val);

        return "│ " + key + val + repeat(' ', pad - 1) + "│";
    }

    /**
     * Repeats a character a specified number of times
     *
     * @param c the character to repeat
     * @param n the number of times to repeat
     * @return a string with the character repeated n times
     */
    public static String repeat(char c, int n) {
        if (n <= 0)
            return CommonConstant.EMPTY_STRING;

        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++)
            sb.append(c);

        return sb.toString();
    }

    /**
     * Truncates a string to fit within a maximum display width, adding ellipsis if truncated
     * <p>
     * Properly handles wide characters (like CJK characters) which take double width in display
     *
     * @param s             the string to truncate
     * @param maxDisplayLen the maximum display width allowed
     * @return the truncated string with ellipsis if needed
     */
    public static String truncate(String s, int maxDisplayLen) {
        if (s == null)
            return "";
        int dLen = getDisplayWidth(s);

        if (dLen <= maxDisplayLen)
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

    /**
     * Calculates the display width of a string, accounting for wide characters
     * <p>
     * Wide characters (like CJK characters) count as two units, normal characters count as 1
     *
     * @param s the string to measure
     * @return the display width of the string
     */
    public static int getDisplayWidth(String s) {
        if (s == null)
            return 0;

        int width = 0;
        for (char c : s.toCharArray())
            width += isWideChar(c) ? 2 : 1;

        return width;
    }

    /**
     * Determines if a character is a wide character (takes double width in display)
     * <p>
     * Simplified implementation that checks for CJK characters, full-width characters,
     * and CJK symbols
     *
     * @param ch the character to check
     * @return true if the character is a wide character, false otherwise
     */
    public static boolean isWideChar(char ch) {
        return (ch >= 0x4E00 && ch <= 0x9FA5) || // CJK
                (ch >= 0xFF01 && ch <= 0xFF60) || // Full Width
                (ch >= 0x3000 && ch <= 0x303F);   // CJK Symbols
    }
}