package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.*;
import com.ajaxjs.util.io.StreamHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Data
@Slf4j
public class RequestCore implements HttpConstant {
    private final String url;

    private final HttpMethod method;

    private String contentType;

    public RequestCore(HttpMethod method, String url) {
        this.method = method;
        this.url = url;
    }

    /**
     * The request data to be sent in bytes.
     */
    private byte[] data;

    /**
     * Set data in string.
     *
     * @param data The request data to be sent in string of the pair format, like `a=foo&b=bar`.
     * @return Request
     */
    public RequestCore setDataStr(String data) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (contentType.equals(CONTENT_TYPE_JSON)) {
            Map<String, String> map = UrlEncode.parseStringToMap(data);
            String json = JsonUtil.toJson(map);
            this.data = json.getBytes(StandardCharsets.UTF_8);
        } else if (contentType.equals(CONTENT_TYPE_FORM))
            this.data = data.getBytes(StandardCharsets.UTF_8); // directly send the string

        return this;
    }

    /**
     * Set data in Json.
     *
     * @param json The request data to be sent in Json.
     * @return Request
     */
    public RequestCore setData(String json) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (contentType.equals(CONTENT_TYPE_JSON))
            this.data = json.getBytes(StandardCharsets.UTF_8);// directly send the string
        else if (contentType.equals(CONTENT_TYPE_FORM)) {
            Map<String, Object> map = JsonUtil.json2map(json);
            String str = MapTool.join(map, v -> v == null ? null : new UrlEncode(v.toString()).encode());
            this.data = str.getBytes(StandardCharsets.UTF_8);
        }

        return this;
    }

    /**
     * Set data in Map.
     *
     * @param dataMap The request data to be sent in Map.
     * @return Request
     */
    public RequestCore setData(Map<String, Object> dataMap) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (contentType.equals(CONTENT_TYPE_JSON)) {
            String json = JsonUtil.toJson(dataMap);
            this.data = json.getBytes(StandardCharsets.UTF_8);
        } else if (contentType.equals(CONTENT_TYPE_FORM)) {
            String str = MapTool.join(dataMap, v -> v == null ? null : new UrlEncode(v.toString()).encode());
            this.data = str.getBytes(StandardCharsets.UTF_8);
        }

        return this;
    }


    /**
     * Set data in Java Bean.
     *
     * @param javaBean The request data to be sent in Java Bean.
     * @return Request
     */
    public RequestCore setData(Object javaBean) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (contentType.equals(CONTENT_TYPE_JSON)) {
            String json = JsonUtil.toJson(javaBean);
            this.data = json.getBytes(StandardCharsets.UTF_8);
        } else if (contentType.equals(CONTENT_TYPE_FORM)) {
            Map<String, Object> map = JsonUtil.pojo2map(javaBean);
            String str = MapTool.join(map, v -> v == null ? null : new UrlEncode(v.toString()).encode());
            this.data = str.getBytes(StandardCharsets.UTF_8);
        }

        return this;
    }

    /**
     * The timeout in milliseconds to wait for connection.
     */
    private int connectTimeout = 10000;

    /**
     * The timeout in milliseconds to wait for reading.
     */
    private int readTimeout = 15000;

    private HttpURLConnection conn;

    /**
     * Init the connection.
     *
     * @return HttpURLConnection
     */
    public HttpURLConnection init() {
        return init(null);
    }

    /**
     * Init the connection.
     *
     * @param initConnection The function to be executed before the connection is established. Used to set the request headers.
     * @return HttpURLConnection
     */
    public HttpURLConnection init(Consumer<HttpURLConnection> initConnection) {
//        log.info("准备连接： {} {}", method, url);
        URL httpUrl;

        try {
            httpUrl = new URL(url);
        } catch (MalformedURLException e) {
            log.warn("Wrong format of this URL: " + url, e);
            throw new RuntimeException("Wrong format on this URL: " + url, e);
        }

        HttpURLConnection conn;

        try {
            conn = (HttpURLConnection) httpUrl.openConnection();
        } catch (IOException e) {
            log.warn("Connected fail of this URL:" + url, e);
            throw new RuntimeException("Connected fail on this URL: " + url, e);
        }

        try {
            conn.setRequestMethod(method.toString());
            conn.setConnectTimeout(connectTimeout);// 设置链接超时时间和 read 超时时间
            conn.setReadTimeout(readTimeout);
        } catch (ProtocolException e) {
            log.warn("Protocol Exception on this URL: " + url, e);
            throw new RuntimeException("Protocol Exception on this URL: " + url, e);
        }

        if (initConnection != null)
            initConnection.accept(conn);

        this.conn = conn;

        return conn;
    }

    private Response resp;

    /**
     * 发送请求，返回响应信息
     *
     * @return 返回类型
     */
    public Response connect() {
        Response resp = new Response();
        this.resp = resp;
        resp.setStartTime(System.currentTimeMillis());
        resp.setConnection(conn);
        resp.setUrl(conn.getURL().toString());
        resp.setHttpMethod(conn.getRequestMethod());
//        LOGGER.info("开始请求 {0} {1}", resp.getHttpMethod(), resp.getUrl());

        try {
            int responseCode = conn.getResponseCode();
//            LOGGER.info("responseCode:" + responseCode);
            resp.setHttpCode(responseCode);

            InputStream in;
            if (responseCode >= 400) {// 如果返回的结果是 400 以上，那么就说明出问题了
                // an error stream if any, null if there have been no errors, the connection is
                // not connected or the server sent no useful data.
                // 在连接建立后服务器端并没有发数据，Stream是空的，只有在进行了getHeaderFields()操作后才会激活服务器进行数据发送，实验一下
                // https://blog.csdn.net/xia4820723/article/details/47804797
                conn.getExpiration();
                resp.setOk(false);

                // 错误通常为文本
                in = conn.getErrorStream();

                String result = null;
                if (in != null) {
                    result = StreamHelper.copyToString(in);
                    resp.setResponseText(result);
                }

                printErrorLog(resp.getHttpMethod(), resp.getUrl(), String.valueOf(resp.getHttpCode()), result == null ? "未知" : result);
                log.warn("{} 返回结果异常\n{}", resp.getUrl(), result == null ? "未知" : result);
            } else {
                resp.setOk(true);
                in = conn.getInputStream();// 发起请求，接收响应
                resp.setIn(in);
            }

            String result = StreamHelper.copyToString(resp.getIn());
            resp.setResponseText(result.trim());
            String resultMsg = (result.length() > MAX_LENGTH_TO_PRINT) ? result.substring(0, MAX_LENGTH_TO_PRINT) + " ..." : result;

            printLog(resp.getHttpMethod() + " " + resp.getUrl(), String.valueOf(resp.getHttpCode()), resultMsg, resp.getStartTime());
        } catch (IOException e) {
            log.warn("请求异常： {} {}", resp.getHttpMethod(), resp.getUrl(), e);
            resp.setOk(false);
            resp.setEx(e);
        }

        return resp;
    }

    /**
     * Get the response as JSON List
     *
     * @return JSON List
     */
    public List<Map<String, Object>> responseAsJsonList() {
        Response resp = connect();

        return resp.isOk() ? JsonUtil.json2mapList(resp.getResponseText()) : null;
    }

    /**
     * Get the response as JSON
     *
     * @return JSON
     */
    public Map<String, Object> responseAsJson() {
        Response resp = connect();

        return resp.isOk() ? JsonUtil.json2map(resp.getResponseText()) : null;
    }

    /**
     * Get the response as Java Bean
     *
     * @param clz The Java Bean class
     * @return Java Bean
     */
    public <T> T responseAsBean(Class<T> clz) {
        Map<String, Object> map = responseAsJson();

        return JsonUtil.map2pojo(map, clz);
    }

    public Map<String, String> responseAsXML() {
        Response resp = connect();

        return resp.isOk() ? MapTool.xmlToMap(resp.getResponseText()) : null;
    }

    private static final int MAX_LENGTH_TO_PRINT = 500;

    public static void printLog(String httpInfo, String httpCode, String returnText, Long startTime) {
        String title = " HTTP ServerRequest ";
        String sb = "\n" + BoxLogger.ANSI_YELLOW + BoxLogger.boxLine('┌', '─', '┐', title) + '\n' +
                BoxLogger.boxContent("Time:       ", ""/* TODO DateHelper.now()*/) + '\n' +
                BoxLogger.boxContent("TraceId:    ", MDC.get(BoxLogger.TRACE_KEY)) + '\n' +
                BoxLogger.boxContent("Request:    ", httpInfo) + '\n' +
                BoxLogger.boxContent("ReturnCode: ", "HTTP status " + httpCode) + '\n' +
                BoxLogger.boxContent("ReturnText: ", returnText.trim()) + '\n' +
                BoxLogger.boxContent("Execution:  ", (System.currentTimeMillis() - startTime) + "ms") + '\n' +
                BoxLogger.boxLine('└', '─', '┘', StrUtil.EMPTY_STRING) + BoxLogger.ANSI_RESET;

        log.info(sb);
    }

    public static void printErrorLog(String httpMethod, String url, String httpCode, String returnText) {
        String title = " HTTP ServerRequest ErrResponse ";
        String sb = "\n" + BoxLogger.ANSI_RED + BoxLogger.boxLine('┌', '─', '┐', title) + '\n' +
                BoxLogger.boxContent("Time:       ", ""/* TODO DateHelper.now()*/) + '\n' +
                BoxLogger.boxContent("TraceId:    ", MDC.get(BoxLogger.TRACE_KEY)) + '\n' +
                BoxLogger.boxContent("Request:    ", httpMethod + " " + url) + '\n' +
                BoxLogger.boxContent("ReturnCode: ", httpCode) + '\n' +
                BoxLogger.boxContent("ReturnText: ", returnText) + '\n' +
                BoxLogger.boxLine('└', '─', '┘', StrUtil.EMPTY_STRING) + BoxLogger.ANSI_RESET;

        log.info(sb);
    }

    public void initData() {
        conn.setDoOutput(true); // 允许写入输出流
        conn.setDoInput(true); // 允许读取输入流

        if (data != null && data.length > 0) {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(data); // 通过输出流写入字节数据
                out.flush();
            } catch (IOException e) {
                log.warn("Write data to connection failed!", e);
                throw new RuntimeException("Write data to connection failed!", e);
            }
        }
    }
}
