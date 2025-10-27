package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.MapTool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
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


    /**
     * Get the response as JSON List
     *
     * @return JSON List
     */
    public List<Map<String, Object>> responseAsJsonList() {
        return JsonUtil.json2mapList(responseText);
    }

    /**
     * Get the response as JSON
     *
     * @return JSON
     */
    public Map<String, Object> responseAsJson() {
        return JsonUtil.json2map(responseText);
    }

    /**
     * Get the response as Java Bean
     *
     * @param clz The Java Bean class
     * @return Java Bean
     */
    public <T> T responseAsBean(Class<T> clz) {
        if (!isOk)
            return null;

        Map<String, Object> map = responseAsJson();

        return JsonUtil.map2pojo(map, clz);
    }

    public Map<String, String> responseAsXML() {
        return MapTool.xmlToMap(responseText);
    }

    /**
     * 返回 JSON 时候的 Map 的 key
     */
    public final static String ERR_MSG = "errMsg";
    public final static String STATUS = "status";

    /**
     * Check the result of remote request that if it's ok.
     * ONLY for AJ-Spring Response Structure.
     *
     * @param result Request result
     * @return if it's ok
     */
    public static boolean isOk(Map<String, Object> result) {
        return result != null && result.containsKey(STATUS) && "1".equals(result.get(STATUS).toString());
    }
}
