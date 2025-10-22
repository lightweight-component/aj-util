package com.ajaxjs.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class DoBase64 {
    private byte[] input;

    private String inputStr;

    /**
     * Only for encoding.
     */
    private boolean withoutPadding = false;

    public DoBase64 setInput(String input) {
        return setInput(input, StandardCharsets.UTF_8);
    }

    public DoBase64 setInput(String input, Charset charset) {
        Objects.requireNonNull(input, "input string");
        Objects.requireNonNull(charset, "charset");
        inputStr = input;
        this.input = input.getBytes(charset);

        return this;
    }

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

        Base64.Encoder encoder;

        if (urlSafe)
            encoder = withoutPadding ? Base64.getUrlEncoder().withoutPadding() : Base64.getUrlEncoder();
        else
            encoder = withoutPadding ? Base64.getEncoder().withoutPadding() : Base64.getEncoder();

        return encoder.encode(input);
    }

    /**
     * Decode the input in BASE64 then returns the string with specified charset
     *
     * @param charset you can specify the charset of the result
     * @return The result in string.
     */
    public String encodeAsString(Charset charset) {
        return new String(encode(), charset);
    }

    /**
     * Decode the input in BASE64 then returns the string in UTF-8 charset
     *
     * @return The result in string.
     */
    public String encodeAsString() {
        return new String(encode(), StandardCharsets.UTF_8);
    }

    /**
     * Decode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] decode() {
        if (input == null)
            throw new IllegalStateException("Input is not set");

        Base64.Decoder decoder = urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder();

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
}
