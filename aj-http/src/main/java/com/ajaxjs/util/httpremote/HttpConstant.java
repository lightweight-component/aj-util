package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface HttpConstant {
    String GET = "GET";

    String POST = "POST";

    String PUT = "PUT";

    String DELETE = "DELETE";

    String CONTENT_TYPE = "Content-Type";

    String CONTENT_TYPE_JSON = "application/json";
    String CONTENT_TYPE_XML = "application/xml";

    String CONTENT_TYPE_JSON_UTF8 = CONTENT_TYPE_JSON + ";charset=utf-8";

    String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    String CONTENT_TYPE_FORM_UTF8 = CONTENT_TYPE_FORM + ";charset=utf-8";

    String CONTENT_TYPE_FORM_UPLOAD = "multipart/form-data";
    String FILE_TYPE = "application/octet-stream";
    String AUTHORIZATION = "Authorization";

    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT
    }

    Consumer<HttpURLConnection> EMPTY_INIT = conn -> {
    };

    /**
     * 设置客户端识别
     */
    BiConsumer<HttpURLConnection, String> SET_USER_AGENT = (conn, url) -> conn.addRequestProperty("User-Agent", url);

    /**
     * 默认的客户端识别
     */
    Consumer<HttpURLConnection> SET_USER_AGENT_DEFAULT = conn -> SET_USER_AGENT.accept(conn, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

}
