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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 批量下载
 */
@Slf4j
public class BatchDownload {
    /**
     * 闭锁。另外可参考栅栏 CyclicBarrier
     */
    private final CountDownLatch latch;

    /**
     * 下载列表
     */
    private final String[] arr;

    /**
     * 保存目录
     */
    private final String saveFolder;

    /**
     * 如何命名文件名的函数。若为 null 则使用原文件名
     */
    private final Supplier<String> newFileNameFn;

    /**
     * 创建图片批量下载
     *
     * @param arr           下载列表
     * @param saveFolder    保存目录
     * @param newFileNameFn 如何命名文件名的函数。若为 null 则使用原文件名
     */
    public BatchDownload(String[] arr, String saveFolder, Supplier<String> newFileNameFn) {
        latch = new CountDownLatch(arr.length);

        this.arr = arr;
        this.saveFolder = saveFolder;
        this.newFileNameFn = newFileNameFn;
    }

    /**
     * 单个下载
     *
     * @param url 下载地址
     * @param i   索引
     */
    private void exec(String url, int i) {
        String newFileName;

        try {
            if (newFileNameFn == null)
                newFileName = download(HttpConstant.HttpMethod.GET, url, null, saveFolder, null);
            else
                newFileName = download(HttpConstant.HttpMethod.GET, url, null, saveFolder, newFileNameFn.get());

            String[] _arr = newFileName.split("\\\\");
            String f = _arr[_arr.length - 1];
            arr[i] = f;
        } finally {
            latch.countDown();// 每个子线程中，不管是否成功，是否有异常
        }
    }

    /**
     * 开始下载
     */
    public void start() {
        for (int i = 0; i < arr.length; i++) {
            final int j = i;
            new Thread(() -> exec(arr[j], j)).start();
        }

        try {
            latch.await(20, TimeUnit.SECONDS); // 给主线程设置一个最大等待超时时间 20秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String download(HttpConstant.HttpMethod method, String url, Consumer<HttpURLConnection> fn, String saveDir, String newFileName) {
        Request get = new Request(method, url);

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
