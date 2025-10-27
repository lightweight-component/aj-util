package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Send DELETE request
 */
public class Delete extends Request {
    public Delete(HttpMethod method, String url) {
        super(method, url);
    }

    public Delete(String url, Consumer<HttpURLConnection> initConnection) {
        super(HttpMethod.DELETE, url);

        init(initConnection);
        connect();
    }

    public static Map<String, Object> api(String url, String token) {
        return api(url, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> initConnection) {
        return new Delete(url, initConnection).getResp().responseAsJson();
    }

    public static <T> T api(String url, Class<T> clz, String token) {
        return api(url, clz, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static <T> T api(String url, Class<T> clz, Consumer<HttpURLConnection> initConnection) {
        return new Delete(url, initConnection).getResp().responseAsBean(clz);
    }
}
