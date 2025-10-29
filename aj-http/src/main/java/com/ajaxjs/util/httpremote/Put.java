package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Send PUT request
 */
public class Put extends BasePost {
    public Put(HttpMethod method, String url) {
        super(method, url);
    }

    public Put(String url, Object data, String contentType, Consumer<HttpURLConnection> initConnection) {
        super(HttpMethod.PUT, url, data, contentType, initConnection);
    }

    public Put(String url, Object data, String contentType) {
        super(HttpMethod.PUT, url, data, contentType);
    }

    /**
     * PUT request to api using Raw body as Json
     *
     * @param url  URL
     * @param data Map data.
     */
    public static Response api(String url, Object data) {
        return new Put(url, data, CONTENT_TYPE_JSON).getResp();
    }

    public static Response api(String url, Object data, Consumer<HttpURLConnection> initConnection) {
        return new Put(url, data, CONTENT_TYPE_JSON, initConnection).getResp();
    }
}