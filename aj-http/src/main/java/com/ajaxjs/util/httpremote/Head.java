package com.ajaxjs.util.httpremote;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

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
     * 得到 HTTP 302 的跳转地址
     *
     * @return 跳转地址
     */
    public String get302redirect() {
        return getConn().getHeaderField("Location");
    }

    /**
     * 检测资源是否存在
     *
     * @return true 表示 404 不存在
     */
    public boolean is404() {
        try {
            return getConn().getResponseCode() == 404;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 得到资源的文件大小
     *
     * @return 文件大小
     */
    public long getFileSize() {
        return getConn().getContentLength();
    }
    /**
     * Map 转化到 HTTP HEAD。 这是高阶函数
     *
     * @param map 头数据
     * @return 函数
     */
    public static Consumer<HttpURLConnection> map2header(Map<String, ?> map) {
        return conn -> {
            for (String key : map.keySet())
                conn.setRequestProperty(key, map.get(key).toString());
        };
    }
}
