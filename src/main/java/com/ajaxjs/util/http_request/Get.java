/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util.http_request;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.RegExpUtils;
import com.ajaxjs.util.http_request.model.ResponseEntity;
import com.ajaxjs.util.io.StreamHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * GET 请求
 *
 * @author Frank Cheung
 */
@Slf4j
public class Get extends Base {
    /**
     * 简单 GET 请求（原始 API 版），返回文本。
     *
     * @param url 请求目标地址
     * @return 响应内容（如 HTML，JSON 等）
     */
    public static String simpleGET(String url) {
        try {
            return StreamHelper.copyToString(new URL(url).openStream());
        } catch (IOException e) {
            log.warn("ERROR>>", e);

            return null;
        }
    }

    /**
     * GET 请求，返回文本内容
     *
     * @param url 请求目标地址
     * @param fn  自定义 HTTP 头的时候可设置，可选的
     * @return 响应的内容
     */
    public static ResponseEntity get(String url, Consumer<HttpURLConnection> fn) {
        return connect(url, "GET", fn);
    }

    /**
     * GET API，返回 JSON
     *
     * @param url 请求目标地址
     * @param fn  自定义 HTTP 头的时候可设置，可选的
     * @return 响应的 JSON，Map 格式
     */
    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> fn) {
        ResponseEntity resp = get(url, fn);

        return ResponseHandler.toJson(resp);
    }

    /**
     * GET API，返回 JSON
     *
     * @param url 请求目标地址
     * @return 响应的 JSON，Map 格式
     */
    public static Map<String, Object> api(String url) {
        return api(url, null);
    }

    public static <T> T api2bean(String url, Class<T> beanClz) {
        return JsonUtil.map2pojo(api(url), beanClz);
    }

    /**
     * GET API，返回 JSON List
     *
     * @param url 请求目标地址
     * @param fn  自定义 HTTP 头的时候可设置，可选的
     * @return 响应的 JSON，List Map 格式
     */
    public static List<Map<String, Object>> apiList(String url, Consumer<HttpURLConnection> fn) {
        ResponseEntity resp = get(url, fn);

        return ResponseHandler.toJsonList(resp);
    }

    /**
     * GET API，返回 JSON List
     *
     * @param url 请求目标地址
     * @return 响应的 JSON，List Map 格式
     */
    public static List<Map<String, Object>> apiList(String url) {
        return apiList(url, null);
    }

    /**
     * GET API，返回 XML
     *
     * @param url 请求目标地址
     * @param fn  自定义 HTTP 头的时候可设置，可选的
     * @return 响应的 XML，Map 格式
     */
    public static Map<String, String> apiXML(String url, Consumer<HttpURLConnection> fn) {
        ResponseEntity resp = get(url, fn);

        return ResponseHandler.toXML(resp);
    }

    /**
     * GET API，返回 XML
     *
     * @param url 请求目标地址
     * @return 响应的 XML，Map 格式
     */
    public static Map<String, String> apiXML(String url) {
        return apiXML(url, null);
    }

    /**
     * GET 请求，返回文本内容
     *
     * @param url 请求目标地址
     * @return 响应的文本内容
     */
    public static ResponseEntity get(String url) {
        return get(url, null);
    }

    /**
     * 下载二进制文件
     *
     * @param url         请求目标地址
     * @param fn          自定义 HTTP 头的时候可设置，可选的
     * @param saveDir     保存的目录
     * @param newFileName 是否有新的文件名，如无请传 null
     * @return 下载文件的完整磁盘路径
     */
    public static String download(String url, Consumer<HttpURLConnection> fn, String saveDir, String newFileName) {
        HttpURLConnection conn = initHttpConnection(url, "GET");
        SetConnection.SET_USER_AGENT_DEFAULT.accept(conn);
        conn.setDoInput(true);// for conn.getOutputStream().write(someBytes); 需要吗？
        conn.setDoOutput(true);

        if (fn != null)
            fn.accept(conn);

        String fileName = getFileNameFromUrl(url);

        if (newFileName != null)
            fileName = newFileName + RegExpUtils.regMatch("\\.\\w+$", fileName);// 新文件名 + 旧扩展名

        ResponseEntity resp = connect(conn);

        return ResponseHandler.download(resp, saveDir, fileName);
    }

    public static String getFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                return path; // return the whole path if no slash is found
            }
            return path.substring(lastSlashIndex + 1);
        } catch (MalformedURLException e) {
            log.warn("getFileNameFromUrl:", e);
            return null;
        }
    }

    /**
     * 下载二进制文件
     *
     * @param url     请求目标地址
     * @param saveDir 保存的目录
     * @return 下载文件的完整磁盘路径
     */
    public static String download(String url, String saveDir) {
        return download(url, null, saveDir, null);
    }
}
