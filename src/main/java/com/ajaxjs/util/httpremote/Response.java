package com.ajaxjs.util.httpremote;

import lombok.Data;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;

@Data
public class Response {
    /**
     * 连接对象
     */
    private HttpURLConnection connection;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求方法
     */
    private String httpMethod;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * 是否成功（http 200 即表示成功，4xx/500x 表示不成功）
     */
    private boolean isOk;

    /**
     * 程序异常，比 HTTP 请求靠前，例如非法网址，或者 dns 不存在的 UnknownHostException
     */
    private Exception ex;

    /**
     * HTTP 状态码
     */
    private Integer httpCode;

    /**
     * 响应消息字符串
     */
    private String responseText;

    /**
     * 结果的流
     */
    private InputStream in;

    /**
     * 开始请求时间
     */
    private Long startTime;
}
