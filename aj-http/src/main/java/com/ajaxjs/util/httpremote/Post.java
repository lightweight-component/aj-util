package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
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

    /**
     * POST request to api using Raw body as Json
     *
     * @param url  URL
     * @param data Map data.
     */
    public static Map<String, Object> api(String url, Object data) {
        return new Post(url, data, CONTENT_TYPE_JSON).getResp().responseAsJson();
    }

    public static Map<String, Object> api(String url, Object data, Consumer<HttpURLConnection> initConnection) {
        return new Post(url, data, CONTENT_TYPE_JSON, initConnection).getResp().responseAsJson();
    }
}
