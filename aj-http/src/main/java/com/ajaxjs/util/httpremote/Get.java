package com.ajaxjs.util.httpremote;


import com.ajaxjs.util.io.DataReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Send GET request
 */
@Slf4j
public class Get extends Request {
    public Get(HttpMethod method, String url) {
        super(method, url);
    }

    public Get(String url, Consumer<HttpURLConnection> initConnection) {
        this(HttpMethod.GET, url);

        init(initConnection);
        connect();
    }

    public static String text(String url) {
        return text(url, null);
    }

    public static String text(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().getResponseText();
    }

    public static Map<String, Object> api(String url, String token) {
        return api(url, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static Map<String, Object> api(String url) {
        return api(url, EMPTY_INIT);
    }

    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsJson();
    }

    public static <T> T api(String url, Class<T> clz, String token) {
        return api(url, clz, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static <T> T api(String url, Class<T> clz, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsBean(clz);
    }

    public static <T> T api(String url, Class<T> clz) {
        return new Get(url, EMPTY_INIT).getResp().responseAsBean(clz);
    }

    public static Map<String, String> apiXml(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsXML();
    }

    /**
     * 简单 GET 请求（原始 API 版），返回文本。
     *
     * @param url 请求目标地址
     * @return 响应内容（如 HTML，JSON 等）
     */
    public static String simpleGET(String url) {
        try {
            return new DataReader(new URL(url).openStream()).readAsString();
        } catch (IOException e) {
            return null;
        }
    }
}
