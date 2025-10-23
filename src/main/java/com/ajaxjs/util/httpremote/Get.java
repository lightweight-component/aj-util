package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.RegExpUtils;
import com.ajaxjs.util.io.FileHelper;
import com.ajaxjs.util.io.StreamHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Send GET request
 */
@Slf4j
public class Get extends Request {
    public Get(HttpMethod method, String url) {
        super(method, url);
    }

    public Get(String url, Consumer<HttpURLConnection> initConnection) {
        this(HttpMethod.GET, url);

        init(initConnection);
        connect();
    }

    public static String text(String url) {
        return text(url, null);
    }

    public static String text(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().getResponseText();
    }

    public static Map<String, Object> api(String url, String token) {
        return api(url, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static Map<String, Object> api(String url) {
        return api(url, c -> {
        });
    }

    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsJson();
    }

    public static <T> T api(String url, Class<T> clz, String token) {
        return api(url, clz, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    public static <T> T api(String url, Class<T> clz, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsBean(clz);
    }

    public static Map<String, String> apiXml(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsXML();
    }

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
            return null;
        }
    }

    public static String download(String url, Consumer<HttpURLConnection> fn, String saveDir, String newFileName) {
        Get get = new Get(HttpMethod.GET, url);

        Consumer<HttpURLConnection> init = conn -> {
//            SetConnection.SET_USER_AGENT_DEFAULT.accept(conn);
            conn.setDoInput(true);// for conn.getOutputStream().write(someBytes); 需要吗？
            conn.setDoOutput(true);
        };
        get.init(fn == null ? init : fn.andThen(init));

        String fileName;
        String oldFileName = getFileNameFromUrl(url);

        if (newFileName == null)
            fileName = oldFileName;
        else
            fileName = newFileName + RegExpUtils.regMatch("\\.\\w+$", oldFileName);// 新文件名 + 旧扩展名

        assert fileName != null;
        File file = new File(saveDir, fileName);
        get.setInputStreamConsumer(in -> {
            FileHelper.createDirectory(saveDir);

            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                StreamHelper.write(in, out, true);
                log.info("文件[{}]写入成功", file);

                in.close();
            } catch (IOException e) {
                log.warn("ERROR>>", e);
            }
        });
        get.connect();

        return file.toString();
    }

    public static String getFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex == -1)
                return path; // return the whole path if no slash is found

            return path.substring(lastSlashIndex + 1);
        } catch (MalformedURLException e) {
            log.warn("getFileNameFromUrl:", e);
            return null;
        }
    }
}
