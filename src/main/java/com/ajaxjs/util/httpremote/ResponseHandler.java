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
package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.io.FileHelper;
import com.ajaxjs.util.io.StreamHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

/**
 * 最初结果为 InputStream，怎么处理？这里提供一些常见的处理手段。
 */
@Slf4j
public abstract class ResponseHandler extends BoxLogger {

    /**
     * 下载文件
     *
     * @param resp     响应消息体
     * @param saveDir  保存的目录
     * @param fileName 保存的文件名
     * @return 下载文件的完整磁盘路径
     */
    public static String download(Response resp, String saveDir, String fileName) {
        FileHelper.createDirectory(saveDir);
        File file = new File(saveDir, fileName);

        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            StreamHelper.write(resp.getIn(), out, true);
            log.info("文件 [{}]写入成功", file);

            return file.toString();
        } catch (IOException e) {
            log.warn("ERROR>>", e);
        } finally {
            try {
                resp.getIn().close();
            } catch (IOException e) {
                log.warn("ERROR>>", e);
            }
        }

        return null;
    }

    /**
     * 判断是否为 GZip 格式的输入流并返回相应的输入流
     * 有些网站强制加入 Content-Encoding:gzip，而不管之前的是否有 GZip 的请求
     *
     * @param conn HTTP 连接
     * @param in   输入流
     * @return 如果Content-Encoding为gzip，则返回  GZIPInputStream 输入流，否则返回 null
     */
    public static InputStream gzip(HttpURLConnection conn, InputStream in) {
        if ("gzip".equals(conn.getHeaderField("Content-Encoding"))) {
            try {
                return new GZIPInputStream(in);
            } catch (IOException e) {
                log.warn("ERROR>>", e);
            }
        }

        return null;
    }

}
