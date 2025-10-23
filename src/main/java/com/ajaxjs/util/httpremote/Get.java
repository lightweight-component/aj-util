package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

public class Get extends RequestCore {

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
        Get get = new Get(url, initConnection);

        return get.getResp().getResponseText();
    }

    public static Map<String, Object> api(String url, String token) {
        return api(url, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> initConnection) {
        Get get = new Get(url, initConnection);

        return get.responseAsJson();
    }

    public static <T> T api(String url, Class<T> clz, String token) {
        return api(url, clz, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static <T> T api(String url, Class<T> clz, Consumer<HttpURLConnection> initConnection) {
        Get get = new Get(url, initConnection);

        return get.responseAsBean(clz);
    }

    public static Map<String, String> apiXml(String url, Consumer<HttpURLConnection> initConnection) {
        Get get = new Get(url, initConnection);

        return get.responseAsXML();
    }
}
