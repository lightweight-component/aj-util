package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BasePost extends Request {
    public BasePost(HttpMethod method, String url) {
        super(method, url);
    }

    public BasePost(HttpMethod method, String url, Object data, Consumer<HttpURLConnection> initConnection) {
        this(method, url, data, null, initConnection);
    }

    public BasePost(HttpMethod method, String url, Object data, String contentType, Consumer<HttpURLConnection> initConnection) {
        this(method, url);
        contentType = contentType == null ? HttpConstant.CONTENT_TYPE_JSON : contentType;
        this.setContentType(contentType);

        if (contentType.equals(HttpConstant.CONTENT_TYPE_JSON)) // auto add header
            initConnection = initConnection.andThen(conn -> conn.setRequestProperty(CONTENT_TYPE, HttpConstant.CONTENT_TYPE_JSON));

        if (data != null) {
            if (data instanceof String) {
                String str = (String) data;

                if (isJson(str)) // json
                    this.setData((String) data);
                else if (str.contains("="))// pair str
                    this.setDataStr(str);
            } else if (data instanceof Map)
                this.setData((Map<String, Object>) data);
            else if (data instanceof byte[])
                this.setData((byte[]) data);
            else // object, a java bean
                this.setData(data);
        }

        init(initConnection);
        initData();
        connect();
    }

    public BasePost(HttpMethod method, String url, Object data, String contentType) {
        this(method, url, data, contentType, null);
    }

    static boolean isJson(String str) {
        return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
    }
}
