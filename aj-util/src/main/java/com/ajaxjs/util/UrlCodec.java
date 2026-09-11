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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * URL Encoding and Decoding Utility Class
 * <p>
 * This class provides methods for encoding and decoding URLs, handling Chinese characters in URLs,
 * and parsing query strings into maps.
 * It supports different encoding strategies and charset specifications.
 */
public class UrlCodec {
    /**
     * The string input
     */
    private final String input;

    /**
     * The charset of the input string
     */
    private final Charset charset;

    /**
     * Create a new UrlEncode with input string.
     * It's a default constructor.
     *
     * @param input The string input
     */
    public UrlCodec(String input) {
        this(input, StandardCharsets.UTF_8);
    }

    /**
     * Create a new UrlEncode with input string and specified charset
     *
     * @param input   The string input
     * @param charset The charset of the input string
     */
    public UrlCodec(String input, Charset charset) {
        this.input = Objects.requireNonNull(input, "input");
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    /**
     * Encode the url with specified charset
     * This is for `application/x-www-form-urlencoded` usage.
     *
     * @return The string being URL encoded
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
     * Spaces are encoded as {@code %20}, and {@code +} is encoded as {@code %2B}.
     *
     * @return the encoded query parameter value
     */
    public String encodeQueryValue() {
        return encodeForm().replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    /**
     * Decode the url.
     * This is for `application/x-www-form-urlencoded` usage.
     *
     * @return The string being URL decode.
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
     * Unlike {@link #decodeForm()}, the '+' character is treated
     * as a literal plus sign rather than a space.
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
     * Concatenates two URL parts while ensuring proper formatting.
     * Handles leading and trailing slashes to create a valid URL path.
     * <p>
     * Concatenates two URL directory strings, handling forward slashes appropriately.
     * If one has a trailing slash and the other has a leading slash, it removes one.
     * If neither has a slash, it adds one between them.
     *
     * @param url1 The base URL part
     * @param url2 The URL part to append
     * @return A properly formatted concatenated URL
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
     * Performs a simple HTTP GET request using the raw API and returns the response as text.
     * This method provides basic HTTP functionality without additional dependencies.
     *
     * @param url The target URL to request
     * @return The response content (HTML, JSON, etc.), or null if an error occurs
     */
    public static String simpleGET(String url) {
        try {
            return new DataReader(new URL(url).openStream()).readAsString();
        } catch (IOException e) {
            return null;
        }
    }
}
