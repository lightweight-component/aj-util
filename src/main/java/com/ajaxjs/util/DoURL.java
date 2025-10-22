package com.ajaxjs.util;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Data
@Accessors(chain = true)
public class DoURL {
    /**
     * The string input
     */
    private String input;

    /**
     * UTF-8 字符串而已
     */
    public static final String CHARSET = "UTF-8";

    /**
     * The charset of the input string
     */
    private String charset = CHARSET;

    /**
     * Do the url encode with specified charset
     *
     * @return The string being URL encoded
     */
    public String encode() {
        try {
            return URLEncoder.encode(input, charset);
        } catch (UnsupportedEncodingException e) {
            log.warn("Unsupported URL Encoding.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Do the url encode, best for GET request
     * <p>
     * <a href="https://www.cnblogs.com/del88/p/6496825.html">...</a>
     *
     * @return The string being URL encoded
     */
    public String encodeQuery() {
        input = input.replaceAll(" ", "%20");

        return encode();
    }

    /**
     * Do the url encode
     *
     * @return The string being URL encoded
     */
    public String encodeSafe() {
        String encoded = encode();

        return encoded.replace("+", "%20").replace("*", "%2A").replace("~", "%7E").replace("/", "%2F");
    }

    /**
     * Do the url decode
     *
     * @return The string being URL decode
     */
    public String decode() {
        try {
            return URLDecoder.decode(input, charset);
        } catch (UnsupportedEncodingException e) {
            log.warn("Unsupported URL Encoding.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * URL 网址的中文乱码处理。 如果 Tomcat 过滤器设置了 UTF-8 那么这里就不用重复转码了
     *
     * @param str 通常是 URL 的 Query String 参数
     * @return 中文
     */
    public static String urlChinese(String str) {
        return StrUtil.byte2String(str.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static String encode(String str) {
        return new DoURL().setInput(str).encode();
    }

    public static String decode(String str) {
        return new DoURL().setInput(str).decode();
    }
}
