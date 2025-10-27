package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
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
}