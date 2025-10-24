package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

/**
 * Send POST request
 */
public class Post extends BasePost {
    public Post(HttpMethod method, String url) {
        super(method, url);
    }

    public Post(String url, Object data, String contentType, Consumer<HttpURLConnection> initConnection) {
        super(HttpMethod.POST, url, data, contentType, initConnection);
    }

    public Post(String url, Object data, String contentType) {
        super(HttpMethod.POST, url, data, contentType);
    }
}
