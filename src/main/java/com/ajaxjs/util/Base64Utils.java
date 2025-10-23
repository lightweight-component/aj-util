package com.ajaxjs.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Base64 encode/decode
 */
@Data
@Accessors(chain = true)
public class Base64Utils {
    private byte[] input;

    public Base64Utils(byte[] input) {
        this.input = input;
    }

    public Base64Utils(String input) {
        this(input, StandardCharsets.UTF_8);
    }

    public Base64Utils(String input, Charset charset) {
        this.input = input.getBytes(charset);
    }

    /**
     * Only for encoding.
     */
    private boolean withoutPadding = false;

    /**
     * 是否 URL safe
     */
    private boolean urlSafe = false;

    /**
     * Encode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] encode() {
        if (input == null)
            throw new IllegalStateException("Input is not set");

        Encoder encoder;

        if (urlSafe)
            encoder = withoutPadding ? Base64.getUrlEncoder().withoutPadding() : Base64.getUrlEncoder();
        else
            encoder = withoutPadding ? Base64.getEncoder().withoutPadding() : Base64.getEncoder();

        return encoder.encode(input);
    }


    /**
     * Decode the input in BASE64 then returns the string.
     * Charset ISO_8859_1 is enough for BASE64 result.
     *
     * @return The result in string.
     */
    public String encodeAsString() {
        return new String(encode(), StandardCharsets.ISO_8859_1);
    }

    /**
     * Decode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] decode() {
        if (input == null)
            throw new IllegalStateException("Input is not set");

        Decoder decoder = urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder();

        return decoder.decode(input);
    }

    /**
     * Decode the input in BASE64 then returns the string with specified charset
     *
     * @param charset you can specify the charset of the result
     * @return The result in string.
     */
    public String decodeAsString(Charset charset) {
        return new String(decode(), charset);
    }

    /**
     * Decode the input in BASE64 then returns the string in UTF-8 charset
     *
     * @return The result in string.
     */
    public String decodeAsString() {
        return new String(decode(), StandardCharsets.UTF_8);
    }

    /**
     * 格式化 Base64 字符串，每 64 个字符换行
     */
    public static String formatBase64String(String base64) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < base64.length(); i += 64)
            sb.append(base64, i, Math.min(i + 64, base64.length())).append("\n");

        return sb.toString().trim();
    }
}
