package com.ajaxjs.util;

import com.ajaxjs.util.io.DataReader;

import java.io.IOException;
import java.net.URL;

public class UrlHelper {
    public static String concatUrl(String url1, String url2) {
        if (url1.endsWith("/"))
            url1 = url1.substring(0, url1.length() - 1);

        if (url2.startsWith("/"))
            return url1 + url2;
        else
            return url1 + "/" + url2;
    }

    /**
     * 简单 GET 请求（原始 API 版），返回文本。
     *
     * @param url 请求目标地址
     * @return 响应内容（如 HTML，JSON 等）
     */
    public static String simpleGET(String url) {
        try {
            return new DataReader(new URL(url).openStream()).readAsString();
        } catch (IOException e) {
            return null;
        }
    }
}
