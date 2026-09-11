package com.ajaxjs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.ajaxjs.util.StringBytes.bytesToHex;
import static com.ajaxjs.util.StringBytes.hexToBytes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StringBytes类的单元测试
 * Unit tests for StringBytes class
 */
class TestStringBytes {
    private static final String TEST_STRING = "Hello World";
    private static final String CHINESE_STRING = "你好世界";
    private static final String SPECIAL_CHAR_STRING = "Hello@#$%^&*()世界";
    private static final String EMPTY_STRING = "";

    private byte[] testBytes;
    private byte[] chineseBytes;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        // Initialize test data
        testBytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
        chineseBytes = CHINESE_STRING.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 测试字符串构造方法
     * Test string constructor
     */
    @Test
    void testStringConstructor() {
        StringBytes stringBytes = new StringBytes(TEST_STRING);
        assertNotNull(stringBytes);
    }

    /**
     * 测试字节数组构造方法
     * Test byte array constructor
     */
    @Test
    void testByteArrayConstructor() {
        StringBytes stringBytes = new StringBytes(testBytes);
        assertNotNull(stringBytes);
    }

    /**
     * 测试getBytes(Charset)方法 - 正常编码分支
     * Test getBytes(Charset) method - normal encoding branch
     */
    @Test
    void testGetBytesWithCharset() {
        StringBytes stringBytes = new StringBytes(TEST_STRING);

        // 测试UTF-8编码
        // Test UTF-8 encoding
        byte[] utf8Bytes = stringBytes.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(testBytes, utf8Bytes);

        // 测试ISO-8859-1编码
        // Test ISO-8859-1 encoding
        byte[] isoBytes = stringBytes.getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(TEST_STRING.getBytes(StandardCharsets.ISO_8859_1), isoBytes);
    }

    /**
     * 测试getBytes(Charset)方法 - null编码分支
     * Test getBytes(Charset) method - null charset branch
     */
    @Test
    void testGetBytesWithNullCharset() {
        StringBytes stringBytes = new StringBytes(TEST_STRING);

        // 测试null编码（使用系统默认编码）
        // Test null charset (use system default encoding)
        byte[] defaultBytes = stringBytes.getBytes(null);
        assertArrayEquals(TEST_STRING.getBytes(), defaultBytes);
    }

    /**
     * 测试getBytes()方法
     * Test getBytes() method
     */
    @Test
    void testGetBytes() {
        StringBytes stringBytes = new StringBytes(TEST_STRING);

        // 测试默认getBytes方法
        // Test default getBytes method
        byte[] bytes = stringBytes.getBytes();
        assertArrayEquals(TEST_STRING.getBytes(), bytes);
    }

    /**
     * 测试getUTF8_Bytes()方法
     * Test getUTF8_Bytes() method
     */
    @Test
    void testGetUTF8_Bytes() {
        StringBytes stringBytes = new StringBytes(TEST_STRING);

        // 测试UTF-8专用方法
        // Test UTF-8 specific method
        byte[] utf8Bytes = stringBytes.getUTF8_Bytes();
        assertArrayEquals(testBytes, utf8Bytes);
    }

    /**
     * 测试getString(Charset)方法 - 正常解码分支
     * Test getString(Charset) method - normal decoding branch
     */
    @Test
    void testGetStringWithCharset() {
        StringBytes stringBytes = new StringBytes(testBytes);

        // 测试UTF-8解码
        // Test UTF-8 decoding
        String result = stringBytes.getString(StandardCharsets.UTF_8);
        assertEquals(TEST_STRING, result);

        // 测试中文字符串UTF-8解码
        // Test Chinese string UTF-8 decoding
        StringBytes chineseStringBytes = new StringBytes(chineseBytes);
        String chineseResult = chineseStringBytes.getString(StandardCharsets.UTF_8);
        assertEquals(CHINESE_STRING, chineseResult);
    }

    /**
     * 测试getString(Charset)方法 - null解码分支
     * Test getString(Charset) method - null charset branch
     */
    @Test
    void testGetStringWithNullCharset() {
        StringBytes stringBytes = new StringBytes(testBytes);

        // 测试null解码（使用系统默认解码）
        // Test null charset (use system default decoding)
        String result = stringBytes.getString(null);
        assertEquals(new String(testBytes), result);
    }

    /**
     * 测试getString()方法
     * Test getString() method
     */
    @Test
    void testGetString() {
        StringBytes stringBytes = new StringBytes(testBytes);

        // 测试默认getString方法
        // Test default getString method
        String result = stringBytes.getString();
        assertEquals(new String(testBytes), result);
    }

    /**
     * 测试getUTF8_String()方法
     * Test getUTF8_String() method
     */
    @Test
    void testGetUTF8_String() {
        StringBytes stringBytes = new StringBytes(testBytes);

        // 测试UTF-8专用解码方法
        // Test UTF-8 specific decoding method
        String result = stringBytes.getUTF8_String();
        assertEquals(TEST_STRING, result);
    }

    /**
     * 测试空字符串场景
     * Test empty string scenario
     */
    @Test
    void testEmptyString() {
        // 测试空字符串转字节
        // Test empty string to bytes
        StringBytes emptyStringBytes = new StringBytes(EMPTY_STRING);
        byte[] emptyBytes = emptyStringBytes.getUTF8_Bytes();
        assertArrayEquals(EMPTY_STRING.getBytes(StandardCharsets.UTF_8), emptyBytes);

        // 测试空字节转字符串
        // Test empty bytes to string
        StringBytes emptyBytesString = new StringBytes(new byte[0]);
        String result = emptyBytesString.getUTF8_String();
        assertEquals(EMPTY_STRING, result);
    }

    /**
     * 测试特殊字符场景
     * Test special characters scenario
     */
    @Test
    void testSpecialCharacters() {
        StringBytes specialStringBytes = new StringBytes(SPECIAL_CHAR_STRING);

        // 测试特殊字符转字节
        // Test special characters to bytes
        byte[] specialBytes = specialStringBytes.getUTF8_Bytes();
        assertArrayEquals(SPECIAL_CHAR_STRING.getBytes(StandardCharsets.UTF_8), specialBytes);

        // 测试特殊字符字节转回字符串
        // Test special characters bytes back to string
        StringBytes specialBytesString = new StringBytes(specialBytes);
        String result = specialBytesString.getUTF8_String();
        assertEquals(SPECIAL_CHAR_STRING, result);
    }

    /**
     * 在字节数组里查找某个字节数组，找到返回&lt;=0，未找到返回-1
     *
     * @param data   被搜索的内容
     * @param search 要搜索内容
     * @param start  搜索起始位置
     * @return 目标位置，找不到返回-1
     */
    public static int byteIndexOf(byte[] data, byte[] search, int start) {
        int len = search.length;

        for (int i = start; i < data.length; i++) {
            int temp = i, j = 0;

            while (data[temp] == search[j]) {
                temp++;
                j++;

                if (j == len)
                    return i;
            }
        }

        return -1;
    }

    /**
     * 在字节数组里查找某个字节数组，找到返回 &lt;=0，未找到返回 -1
     *
     * @param data   被搜索的内容
     * @param search 要搜索内容
     * @return 目标位置，找不到返回 -1
     */
    public static int byteIndexOf(byte[] data, byte[] search) {
        return byteIndexOf(data, search, 0);
    }

    /**
     * 在字节数组中截取指定长度数组
     *
     * @param data   输入的数据
     * @param off    偏移
     * @param length 长度
     * @return 指定 范围的字节数组
     */
    public static byte[] subBytes(byte[] data, int off, int length) {
        byte[] bs = new byte[length];
        System.arraycopy(data, off, bs, 0, length);

        return bs;
    }

    /**
     * char 数组转 byte 数组
     * 将 char 数组转换为 byte 数组需要考虑编码方式的问题
     * <a href="https://houbb.github.io/2023/06/05/java-perf-02-chars-to-bytes">...</a>
     *
     * @param chars 输入的字符数组。
     * @return 转换后的字节数组。
     */
    public static byte[] charToByte(char[] chars) {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    @Test
    void testHexToBytes() {
        byte[] bs = hexToBytes("1A2B3C");
        assertNotNull(bs);
        assertArrayEquals(new byte[]{0x1A, 0x2B, 0x3C}, bs);
    }

    @Test
    void parseHexRejectsOddLengthAndReturnsEmptyArrayForEmptyInput() {
        assertArrayEquals(new byte[0], hexToBytes(""));
        assertThrows(IllegalArgumentException.class, () -> hexToBytes("ABC"));
        assertThrows(IllegalArgumentException.class, () -> hexToBytes("0G"));
    }

    /**
     * Test case: Standard byte array input.
     * Verifies that the method correctly converts a standard byte array to its hexadecimal string representation.
     */
    @Test
    void testBytesToHex_StandardCase() {
        byte[] input = new byte[]{0x1A, 0x2B, 0x3C};
        String result = bytesToHex(input);
        assertEquals("1A2B3C", result);
    }

    /**
     * Test case: All zero bytes.
     * Verifies that the method correctly converts an array of zero bytes to the appropriate hexadecimal string.
     */
    @Test
    void testBytesToHex_AllZeros() {
        byte[] input = new byte[]{0x00, 0x00, 0x00};
        String result = bytesToHex(input);
        assertEquals("000000", result);
    }

    /**
     * Test case: Bytes with high bit set.
     * Verifies that the method correctly handles bytes where the high bit is set (negative values in Java).
     */
    @Test
    void testBytesToHex_HighBitSet() {
        byte[] input = new byte[]{(byte) 0x80, (byte) 0xFF};
        String result = bytesToHex(input);
        assertEquals("80FF", result);
    }

    /**
     * Test case: Empty byte array.
     * Verifies that the method returns an empty string when given an empty byte array.
     */
    @Test
    void testBytesToHex_EmptyArray() {
        byte[] input = new byte[0];
        String result = bytesToHex(input);
        assertEquals("", result);
    }

    /**
     * Test case: Single byte values.
     * Verifies several specific single byte conversions.
     */
    @Test
    void testBytesToHexStr_SingleBytes() {
        assertEquals("00", bytesToHex(new byte[]{0x00}));
        assertEquals("FF", bytesToHex(new byte[]{(byte) 0xFF}));
        assertEquals("7F", bytesToHex(new byte[]{0x7F}));
        assertEquals("80", bytesToHex(new byte[]{(byte) 0x80}));
    }

    /**
     * Test case: Long byte array.
     * Verifies that the method correctly converts a longer byte array without errors.
     */
    @Test
    void testBytesToHex_LongArray() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }

        String result = bytesToHex(input);
        assertEquals(200, result.length()); // Each byte should produce 2 hex chars

        // Verify first few values
        assertTrue(result.startsWith("000102030405"));
    }
}
