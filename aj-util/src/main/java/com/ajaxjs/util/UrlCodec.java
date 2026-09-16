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

import com.ajaxjs.util.io.DataReader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utilities for form and query parameter encoding, URL path concatenation, and simple URL reads.
 * <p>
 * Each instance holds a non-null input string and charset; UTF-8 is used by default.
 * Encoding and decoding methods operate on individual values, not complete URLs or query strings.
 * Form encoding represents spaces as {@code +}, whereas RFC 3986-style query value encoding
 * represents spaces as {@code %20} and preserves literal plus signs when decoding.
 *
 * @see URLEncoder
 * @see URLDecoder
 */
public class UrlCodec {
    /**
     * The non-null input value to encode or decode.
     */
    private final String input;

    /**
     * The non-null charset used to convert between characters and percent-encoded bytes.
     */
    private final Charset charset;

    /**
     * Creates a codec for the given input using UTF-8.
     *
     * @param input the raw value to encode or the encoded value to decode; may be empty
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public UrlCodec(String input) {
        this(input, StandardCharsets.UTF_8);
    }

    /**
     * Creates a codec for the given input and charset.
     *
     * @param input   the raw value to encode or the encoded value to decode; may be empty
     * @param charset the charset used for encoding and decoding percent-encoded bytes
     * @throws NullPointerException if {@code input} or {@code charset} is {@code null}
     */
    public UrlCodec(String input, Charset charset) {
        this.input = Objects.requireNonNull(input, "input");
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    /**
     * Encodes the input as an {@code application/x-www-form-urlencoded} value using this codec's charset.
     * <p>
     * ASCII letters, digits, and {@code -}, {@code _}, {@code .}, {@code *} remain unchanged.
     * Spaces become {@code +}; other characters are converted to bytes and percent-encoded.
     * A literal {@code +} becomes {@code %2B}. Existing percent escapes are encoded again.
     *
     * @return the form-encoded value
     * @throws IllegalStateException if the charset name is not supported by {@link URLEncoder}
     * @see #decodeForm()
     */
    public String encodeForm() {
        try {
            return URLEncoder.encode(input, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported charset: " + charset.name(), e);
        }
    }

    /**
     * Encodes a raw query parameter value using RFC 3986 style.
     * <p>
     * Uses this codec's charset and leaves only ASCII letters, digits, and the unreserved
     * characters {@code -}, {@code .}, {@code _}, {@code ~} unchanged.
     * Spaces are encoded as {@code %20}, {@code +} as {@code %2B}, and {@code *} as {@code %2A}.
     * Existing percent escapes are encoded again; pass a raw value rather than a complete query string.
     *
     * @return the encoded query parameter value
     * @throws IllegalStateException if the charset name is not supported by {@link URLEncoder}
     * @see #decodeQueryValue()
     */
    public String encodeQueryValue() {
        return encodeForm().replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    /**
     * Decodes an {@code application/x-www-form-urlencoded} value using this codec's charset.
     * <p>
     * Converts {@code +} to a space and percent-encoded bytes to characters.
     * For example, {@code a+b%2Bc} becomes {@code a b+c}.
     *
     * @return the decoded form value
     * @throws IllegalArgumentException if a percent escape is incomplete or contains non-hexadecimal digits
     * @throws IllegalStateException    if the charset name is not supported by {@link URLDecoder}
     * @see #encodeForm()
     */
    public String decodeForm() {
        try {
            return URLDecoder.decode(input, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported charset: " + charset.name(), e);
        }
    }

    /**
     * Decodes an RFC 3986-style query parameter value.
     * <p>
     * Decodes percent-encoded bytes using this codec's charset. Unlike {@link #decodeForm()},
     * a literal {@code +} remains a plus sign rather than becoming a space.
     * For example, {@code a%20b+c} becomes {@code a b+c}.
     *
     * @return the decoded query parameter value
     * @throws IllegalArgumentException if a percent escape is incomplete or contains non-hexadecimal digits
     * @throws IllegalStateException    if the charset name is not supported by {@link URLDecoder}
     * @see #encodeQueryValue()
     */
    public String decodeQueryValue() {
        try {
            // URLDecoder 会把 '+' 解码为空格，但在 RFC 3986 query value 语义下 '+' 应保持为 '+'
            return URLDecoder.decode(input.replace("+", "%2B"), charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported charset: " + charset.name(), e);
        }
    }

    /**
     * Joins two URL path parts with a slash at the boundary.
     * <p>
     * Removes at most one trailing slash from {@code url1}, then inserts a slash only if
     * {@code url2} does not start with one. Repeated slashes are not fully normalized.
     * This method performs string concatenation only: it does not validate or encode URLs,
     * resolve relative paths, or handle query strings and fragments specially.
     * Empty parts are allowed; two empty parts produce {@code /}.
     *
     * @param url1 the non-null base URL or path part
     * @param url2 the non-null path part to append
     * @return the concatenated string
     * @throws NullPointerException if either part is {@code null}
     */
    public static String concatUrl(String url1, String url2) {
        if (url1.endsWith("/"))
            url1 = url1.substring(0, url1.length() - 1);

        if (url2.startsWith("/"))
            return url1 + url2;
        else
            return url1 + "/" + url2;
    }

    /**
     * Opens a URL with {@link URL#openStream()} and reads its content as UTF-8 text.
     * <p>
     * For HTTP URLs, this performs a basic GET request; other supported URL protocols may
     * also be used. No explicit timeouts or request headers are configured.
     * The response's declared charset is not consulted. Line endings are replaced with
     * {@link System#lineSeparator()}, which is also appended after each line read.
     * The stream is closed by {@link DataReader} after reading.
     *
     * @param url the URL string to open
     * @return the response text, an empty string for empty content, or {@code null} if URL
     * construction or opening the stream throws an {@link IOException}
     * @throws java.io.UncheckedIOException if an I/O error occurs while reading or closing the stream
     */
    public static String simpleGET(String url) {
        try {
            return new DataReader(new URL(url).openStream()).readAsString();
        } catch (IOException e) {
            return null;
        }
    }
}
