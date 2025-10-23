package com.ajaxjs.util.httpremote;

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
    String AUTHORIZATION = "Authorization";

    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT
    }
}
