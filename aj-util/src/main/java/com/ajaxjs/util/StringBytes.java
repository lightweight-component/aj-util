/**
 * Copyright sp42 frank@ajaxjs.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ajaxjs.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Convert String to bytes and bytes to String
 * Actually, you can use `new String(bytes)`/`str.getBytes()` more easily.
 */
public class StringBytes {
    /**
     * String to convert.
     */
    private String string;

    /**
     * Constructor.
     *
     * @param string String to convert.
     */
    public StringBytes(String string) {
        this.string = string;
    }

    /**
     * Bytes to convert.
     */
    private byte[] bytes;

    /**
     * Constructor.
     *
     * @param bytes Bytes to convert.
     */
    public StringBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Convert String to bytes.
     *
     * @param charset Charset to use.
     * @return Bytes
     */
    public byte[] getBytes(Charset charset) {
        return charset == null ? string.getBytes() : string.getBytes(charset);
    }

    /**
     * Convert String to bytes.
     * Uses platform default charset.
     *
     * @return Bytes
     */
    public byte[] getBytes() {
        return getBytes(null);
    }

    /**
     * Convert String to bytes.
     * Uses UTF-8 charset.
     *
     * @return Bytes
     */
    public byte[] getUTF8_Bytes() {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convert bytes to String.
     *
     * @param charset Charset to use.
     * @return String with given charset.
     */
    public String getString(Charset charset) {
        return charset == null ? new String(bytes) : new String(bytes, charset);
    }

    /**
     * Convert bytes to String.
     * Uses platform default charset.
     *
     * @return String with platform default charset.
     */
    public String getString() {
        return getString(null);
    }

    /**
     * Convert bytes to String.
     * Uses UTF-8 charset.
     *
     * @return String with UTF-8 charset.
     */
    public String getUTF8_String() {
        return getString(StandardCharsets.UTF_8);
    }

    /**
     * Show platform default charset.
     *
     * @return Platform default charset.
     */
    public static String showPlatformDefaultCharset() {
        return Charset.defaultCharset().toString();
    }

    /**
     * Lookup table used to convert a nibble to its uppercase hex character.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * byte[] 转化为 16 进制字符串输出
     *
     * @param bytes 字节数组
     * @return 16 进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;

            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr 16进制字符串
     * @return 二进制数组
     */
    public static byte[] hexToBytes(String hexStr) {
        Objects.requireNonNull(hexStr, "hexStr");

        if (hexStr.isEmpty())
            return new byte[0];

        if ((hexStr.length() & 1) != 0)
            throw new IllegalArgumentException("Hex string must contain an even number of characters.");

        byte[] result = new byte[hexStr.length() / 2];

        for (int i = 0; i < result.length; i++) {
            int high = Character.digit(hexStr.charAt(i * 2), 16);
            int low = Character.digit(hexStr.charAt(i * 2 + 1), 16);

            if (high < 0 || low < 0)
                throw new IllegalArgumentException("Invalid hexadecimal character at index " + (high < 0 ? i * 2 : i * 2 + 1));

            result[i] = (byte) (high * 16 + low); // 计算二进制数
        }

        return result;
    }
}