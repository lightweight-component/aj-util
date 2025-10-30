package com.ajaxjs.util.httpremote;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * Send HEAD request
 * HTTP HEAD doesn't have a response body, just cares about the response header.
 * Obtains the response headers from HttpURLConnection.
 */
public class Head extends Request {
    public Head(HttpMethod method, String url) {
        super(method, url);
    }

    public Head(String url) {
        super(HttpMethod.HEAD, url);
        this.setInputStreamConsumer(in -> {// 不需要转化响应文本，节省资源
        });
    }

    @Override
    public HttpURLConnection init(Consumer<HttpURLConnection> initConnection) {
        Consumer<HttpURLConnection> beforeInit = conn -> {
            conn.setInstanceFollowRedirects(false); // 必须设置 false，否则会自动 redirect 到 Location 的地址
        };

        return super.init(initConnection == null ? beforeInit : beforeInit.andThen(initConnection));
    }

    @Override
    public HttpURLConnection init() {
        return init(null);
    }

    /**
     * Obtain the redirect address of HTTP 302.
     *
     * @return The redirect address.
     */
    public String get302redirect() {
        return getConn().getHeaderField("Location");
    }

    /**
     * Detect whether the resource exists
     *
     * @return true = 404
     */
    public boolean is404() {
        try {
            return getConn().getResponseCode() == 404;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtain the file size.
     *
     * @return The file size.
     */
    public long getFileSize() {
        return getConn().getContentLength();
    }

    /**
     * 判断是否为 GZip 格式的输入流并返回相应的输入流
     * 有些网站强制加入 Content-Encoding:gzip，而不管之前的是否有 GZip 的请求
     *
     * @param conn HTTP 连接
     * @param in   输入流
     * @return 如果Content-Encoding为gzip，则返回  GZIPInputStream 输入流，否则返回 null
     */
    public static InputStream gzip(HttpURLConnection conn, InputStream in) {
        if ("gzip".equals(conn.getHeaderField("Content-Encoding"))) {
            try {
                return new GZIPInputStream(in);
            } catch (IOException e) {
//                log.warn("ERROR>>", e);
            }
        }

        return null;
    }

    /**
     * Transform Map to HTTP HEAD.
     * This is a higher-order function
     *
     * @param map The head data
     * @return A lambda function
     */
    public static Consumer<HttpURLConnection> map2header(Map<String, ?> map) {
        return conn -> {
            for (String key : map.keySet())
                conn.setRequestProperty(key, map.get(key).toString());
        };
    }
}
