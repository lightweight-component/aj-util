/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;

/**
 * Utility class for Base64 encoding and decoding operations.
 * Provides convenient methods for converting between byte arrays and strings
 * using standard Base64 encoding, with support for URL-safe variant and
 * customizable padding options.
 */
@Setter
@Accessors(chain = true)
public class Base64Utils {
    /**
     * The input byte array to encode or decode.
     */
    private final byte[] input;

    /**
     * Creates a new Base64Utils instance with byte array input.
     *
     * @param input the byte array to encode or decode
     */
    public Base64Utils(byte[] input) {
        this.input = Objects.requireNonNull(input, "input");
    }

    /**
     * Creates a new Base64Utils instance with string input using UTF-8 charset.
     *
     * @param input the string to encode or decode
     */
    public Base64Utils(String input) {
        this(input, StandardCharsets.UTF_8);
    }

    /**
     * Creates an instance from a string using the specified charset.
     * <p>
     * The charset is intended for converting raw text before Base64 encoding.
     * For decoding a Base64 string, use {@link #Base64Utils(String)}, since Base64 text is ASCII-compatible.
     *
     * @param input   the string to encode or decode
     * @param charset the charset to use for string to byte conversion
     */
    public Base64Utils(String input, Charset charset) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(charset, "charset");

        this.input = input.getBytes(charset);
    }

    /**
     * Flag indicating whether to omit padding characters in the encoded output.
     * This option is only relevant for encoding operations.
     * There two advantages if it's to be true: shorter for strings and without URL encoding for the character '='.
     * For example, in the use of JWT, it's not necessary to use padding characters.
     */
    private boolean withoutPadding = false;

    /**
     * Flag indicating whether to use URL-safe Base64 encoding and decoding.
     * URL-safe variant uses '-' instead of '+' and '_' instead of '/'.
     */
    private boolean urlSafe = false;

    /**
     * Encode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] encode() {
        Encoder encoder = urlSafe ? Base64.getUrlEncoder() : Base64.getEncoder();

        if (withoutPadding)
            encoder = encoder.withoutPadding();

        return encoder.encode(input);
    }

    /**
     * Encodes the input as a Base64 string.
     *
     * @return the Base64 encoded string
     */
    public String encodeAsString() {
        return new String(encode(), StandardCharsets.US_ASCII);
    }

    /**
     * Decode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] decode() {
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
        return decodeAsString(StandardCharsets.UTF_8);
    }

    /**
     * Formats a Base64 string using 64 characters per line,
     * as commonly used in PEM-style Base64 data.
     *
     * @param base64 the Base64 string to format
     * @return the formatted Base64 string with line breaks
     */
    public static String formatPemBase64(String base64) {
        int len = base64.length();

        if (len <= 64)
            return base64;

        int lineBreaks = (len - 1) / 64;
        StringBuilder sb = new StringBuilder(len + lineBreaks);

        for (int i = 0; i < len; i += 64) {
            if (i > 0)
                sb.append('\n');

            sb.append(base64, i, Math.min(i + 64, len));
        }

        return sb.toString();
    }
}