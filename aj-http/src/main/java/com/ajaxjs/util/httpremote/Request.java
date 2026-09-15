package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.MapTool;
import com.ajaxjs.util.UrlCodec;
import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.httpremote.model.HttpConstant;
import com.ajaxjs.util.httpremote.model.HttpMethod;
import com.ajaxjs.util.httpremote.model.Response;
import com.ajaxjs.util.io.DataReader;
import com.ajaxjs.util.log.TextBox;
import com.ajaxjs.util.log.Trace;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Core HTTP request implementation that handles creating, configuring, and executing HTTP requests.
 * This class provides functionality for setting request data in various formats, configuring timeouts,
 * initializing connections, and sending requests with proper logging and error handling.
 * Implements HttpConstant to provide access to common HTTP constants.
 */
@Getter
@Setter
@Slf4j
public class Request implements HttpConstant {
    /**
     * The URL to which the request will be sent
     */
    private final String url;

    /**
     * The HTTP method to be used for the request
     */
    private final HttpMethod method;

    /**
     * The Content-Type header value for the request
     */
    private String contentType;

    /**
     * Creates a new Request with the specified HTTP method and URL.
     *
     * @param method the HTTP method to use (GET, POST, PUT, DELETE, etc.)
     * @param url    the URL to which the request will be sent
     */
    public Request(HttpMethod method, String url) {
        this.method = Objects.requireNonNull(method, "Request.method");
        this.url = Objects.requireNonNull(url, "Request.url");
    }

    /**
     * The request data to be sent in bytes.
     */
    private byte[] data;

    private Consumer<OutputStream> outputStreamConsumer;

    /**
     * Sets the raw byte data for the request body.
     *
     * @param data the byte array containing the request body data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Sets request data from a string in query parameter format.
     * Automatically converts to the appropriate format based on a content type.
     *
     * @param data The request data in string format, like `a=foo&amp;b=bar`
     * @throws IllegalArgumentException if a content type is not set or data is invalid
     */
    public void setDataStr(String data) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (isContentType(CONTENT_TYPE_JSON)) {
            Map<String, String> map = MapTool.toMap(data);
            String json = JsonUtil.toJson(map);
            this.data = json.getBytes(StandardCharsets.UTF_8);
        } else if (isContentType(CONTENT_TYPE_FORM))
            this.data = data.getBytes(StandardCharsets.UTF_8); // directly send the string
        else
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

    /**
     * Sets request data from a JSON string.
     * Automatically converts to the appropriate format based on a content type.
     *
     * @param json The request data in JSON string format
     * @throws IllegalArgumentException if a content type is not set or JSON is invalid
     */
    public void setData(String json) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        String trimmed = json.trim();

        if (!trimmed.startsWith("[") && !trimmed.startsWith("{"))
            throw new IllegalArgumentException("Please input a valid JSON string.");

        if (isContentType(CONTENT_TYPE_JSON))
            this.data = json.getBytes(StandardCharsets.UTF_8);
        else if (isContentType(CONTENT_TYPE_FORM)) {
            Map<String, Object> map = JsonUtil.json2map(json);
            String str = MapTool.join(map, v -> v == null
                    ? null
                    : new UrlCodec(v.toString()).encodeForm()
            );

            this.data = str.getBytes(StandardCharsets.UTF_8);
        } else
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

    /**
     * Sets request data from a Map object.
     * Automatically converts to the appropriate format based on a content type.
     *
     * @param dataMap The request data as a Map of key-value pairs
     * @throws IllegalArgumentException if a content type is not set
     */
    public void setData(Map<String, Object> dataMap) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (isContentType(CONTENT_TYPE_JSON)) {
            String json = JsonUtil.toJson(dataMap);
            this.data = json.getBytes(StandardCharsets.UTF_8);
        } else if (isContentType(CONTENT_TYPE_FORM)) {
            String str = MapTool.join(dataMap,
                    v -> v == null ? null : new UrlCodec(v.toString()).encodeForm());

            this.data = str.getBytes(StandardCharsets.UTF_8);
        } else if (isContentType(CONTENT_TYPE_FORM_UPLOAD)) { // TODO
            throw new UnsupportedOperationException("Multipart form upload is not implemented yet.");
        } else
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

    /**
     * Sets request data from a Java Bean object.
     * Converts the bean to JSON or form data based on a content type.
     *
     * @param javaBean The request data as a Java Bean object
     * @throws IllegalArgumentException if a content type is not set
     */
    public void setData(Object javaBean) {
        if (contentType == null)
            throw new IllegalArgumentException("Please set the content type first, then call this method later.");

        if (isContentType(CONTENT_TYPE_JSON)) {
            String json = JsonUtil.toJson(javaBean);
            this.data = json.getBytes(StandardCharsets.UTF_8);
        } else if (isContentType(CONTENT_TYPE_FORM)) {
            Map<String, Object> map = JsonUtil.pojo2map(javaBean);
            String str = MapTool.join(map, v -> v == null
                    ? null
                    : new UrlCodec(v.toString()).encodeForm()
            );
            this.data = str.getBytes(StandardCharsets.UTF_8);
        } else
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

    /**
     * The timeout in milliseconds to wait for connection.
     */
    private int connectTimeout = 10000;

    /**
     * The timeout in milliseconds to wait for reading.
     */
    private int readTimeout = 15000;

    /**
     * The underlying HTTP connection object
     */
    @Setter(AccessLevel.NONE)
    private HttpURLConnection conn;

    /**
     * Initializes the HTTP connection with default settings.
     *
     * @return the initialized HttpURLConnection object
     */
    public HttpURLConnection init() {
        return init(null);
    }

    /**
     * Initializes the HTTP connection with custom settings.
     * Sets up the connection with the appropriate method, timeouts, and any custom initialization.
     *
     * @param initConnection function to be executed before the connection is established,
     *                       typically used to set custom request headers
     * @return the initialized HttpURLConnection object
     * @throws RuntimeException if there's an error creating or configuring the connection
     */
    public HttpURLConnection init(Consumer<HttpURLConnection> initConnection) {
        URL httpUrl;

        try {
            httpUrl = new URL(url);
        } catch (MalformedURLException e) {
            log.warn("Wrong format of this URL: {}", url, e);
            throw new RuntimeException("Wrong format on this URL: " + url, e);
        }

        HttpURLConnection conn;

        try {
            String protocol = httpUrl.getProtocol();

            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol))
                throw new IllegalArgumentException("Only HTTP and HTTPS URLs are supported: " + url);

            conn = (HttpURLConnection) httpUrl.openConnection();
        } catch (IOException e) {
            log.warn("Connected fail of this URL:{}", url, e);
            throw new UncheckedIOException("Connected fail on this URL: " + url, e);
        }

        try {
            if (contentType != null)
                conn.setRequestProperty(CONTENT_TYPE, contentType);

            conn.setRequestMethod(method.toString());
            conn.setConnectTimeout(connectTimeout);// Set connection timeout and read timeout
            conn.setReadTimeout(readTimeout);
        } catch (ProtocolException e) {
            log.warn("Protocol Exception on this URL: {}", url, e);
            throw new RuntimeException("Protocol Exception on this URL: " + url, e);
        }

        if (initConnection != null)
            initConnection.accept(conn);

        this.conn = conn;

        return conn;
    }

    /**
     * The response object created after sending the request
     */
    @Setter(AccessLevel.NONE)
    private Response resp;

    /**
     * Custom consumer for handling the input stream directly.
     * If not null, bypass the default behavior of converting the response to text.
     */
    private Consumer<InputStream> inputStreamConsumer;

    /**
     * Sends the HTTP request and returns the response.
     * Handles connection, reading the response, error processing, and logging.
     *
     * @return the Response object containing the server's response
     */
    public Response connect() {
        if (conn == null)
            throw new IllegalStateException("Connection has not been initialized. Call init() first.");

        Response resp = new Response();
        this.resp = resp;
        resp.setStartTime(System.currentTimeMillis());
        resp.setConnection(conn);
        resp.setUrl(conn.getURL().toString());
        resp.setHttpMethod(conn.getRequestMethod());
        InputStream in = null;
        String result = null;

        try {
            int responseCode = conn.getResponseCode(); // starts to connect
            resp.setHttpCode(responseCode);

            boolean ok = responseCode >= 200 && responseCode < 300;

            if (!ok) {
                /*
                 If the response code is 400 +, it indicates an error.
                 An error stream if any, null if there have been no errors, the connection is not connected or the server sent no useful data.
                 After connection is established, the server may not have sent data yet - stream is empty.
                 The data transmission is activated after getHeaderFields() is called. Let's test this.
                 https://blog.csdn.net/xia4820723/article/details/47804797
                 */
//                conn.getExpiration();
                resp.setOk(false);
                in = conn.getErrorStream(); // Errors are typically text
            } else {
                resp.setOk(true);
                in = conn.getInputStream();// Send request and receive response
            }

            if (in != null) {
                if (inputStreamConsumer == null) {
                    result = new DataReader(in).readAsString();
                    resp.setResponseText(result);
                } else
                    inputStreamConsumer.accept(in);
            }
        } catch (IOException e) {
            log.warn("Request failed. Method: {}, URL: {}", resp.getHttpMethod(), resp.getUrl(), e);
            resp.setOk(false);
            resp.setEx(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Close input stream failed.", e);
                }
            }

            String requestParams = "NONE";

            if (getData() != null)
                requestParams = new String(getData(), StandardCharsets.UTF_8);

            printLog(resp.isOk(), resp.getHttpMethod(), resp.getUrl(), requestParams, resp.getHttpCode(), result, resp.getStartTime());
        }

        return resp;
    }

    /**
     * Maximum length of a response text to print in logs
     */
    private static final int MAX_LENGTH_TO_PRINT = 460;

    private boolean isContentType(String expected) {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith(expected.toLowerCase(Locale.ROOT));
    }

    /**
     * Prints detailed log information about an HTTP request and response.
     * Formats the log with request/response details, timing information, and status.
     *
     * @param isOk       whether the request was successful
     * @param httpMethod the HTTP method used
     * @param url        the URL requested
     * @param data       the request data sent
     * @param httpCode   the HTTP status code received
     * @param returnText the response text (truncated if too long)
     * @param startTime  the timestamp when the request started
     */
    public static void printLog(boolean isOk, String httpMethod, String url, String data, Integer httpCode, String returnText, Long startTime) {
        if (returnText == null)
            returnText = "(ZERO byte returns OR controller by other process.)";

        returnText = (returnText.length() > MAX_LENGTH_TO_PRINT) ? returnText.substring(0, MAX_LENGTH_TO_PRINT) + " ..." : returnText;

        String title = isOk ? " HTTP ServerRequest " : " HTTP ServerRequest ErrResponse ";
        TextBox textBox = new TextBox();
        textBox.setBoxColor(isOk ? TextBox.ANSI_YELLOW : TextBox.ANSI_RED);

        textBox.boxStart(title)
                .line("Time:       ", DateTools.now())
                .line("TraceId:    ", MDC.get(Trace.TRACE_KEY))
                .line("Request:    ", httpMethod + " " + url)
                .line("Parameters: ", data)
                .line("ReturnCode: ", "HTTP status " + httpCode)
                .line("ReturnText: ", returnText.trim())
                .line("Execution:  ", (System.currentTimeMillis() - startTime) + "ms");

        String _log = textBox.boxEnd();
        Trace.saveLogToMDC(_log);
        log.info(_log);
    }

    /**
     * Initializes and writes data to the connection output stream.
     * Configures the connection to allow input and output, then write the request data
     * if any is available.
     *
     * @throws RuntimeException if there's error writing data to the connection
     */
    public void initData() {
        if (conn == null)
            throw new IllegalStateException("Connection has not been initialized. Call init() first.");

        if (outputStreamConsumer == null && (data == null || data.length == 0))
            return;

        conn.setDoOutput(true);

        if (outputStreamConsumer != null) {
            try (OutputStream out = conn.getOutputStream()) {
                outputStreamConsumer.accept(out);
                out.flush();
            } catch (IOException e) {
                throw new UncheckedIOException("Write data to connection failed!", e);
            }
        } else {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(data);
                out.flush();
            } catch (IOException e) {
                throw new UncheckedIOException("Write data to connection failed!", e);
            }
        }
    }
}
