package com.ajaxjs.util.io;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * The writer class for writing data to an output stream.
 * OutputStream is used for writing raw byte data to a destination like a file, socket, or buffer.
 * If it does not meet the need, you can refer to the Spring StreamUtils/ResourceUtils/FileCopyUtils/FileSystemUtils.
 */
@Slf4j
@RequiredArgsConstructor
@Data
public class DataWriter {
    private final OutputStream out;

    /**
     * 1K 的数据块
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Add a buffer to the output stream. It's true by default.
     */
    private boolean isBuffered = true;

    /**
     * 带缓冲的一入一出 出是字节流，所以要缓冲（字符流自带缓冲，所以不需要额外缓冲）
     * 请注意，该方法不会关闭 input 流 close，你需要手动关闭
     *
     * @param in 输入流，无须手动关闭
     */
    public void write(InputStream in) {

        if (!isBuffered)
            log.warn("It's not recommended that NOT using BufferedOutputStream.");

        try {
            OutputStream _out = isBuffered ? new BufferedOutputStream(out) : out;// 加入缓冲功能

            new DataReader(in).readStreamAsBytes(BUFFER_SIZE, (readSize, buffer) -> {
                try {
                    _out.write(buffer, 0, readSize);
                } catch (IOException e) {
                    log.warn("Error occurred when copying input to output data.", e);
                    throw new UncheckedIOException("Error occurred when copying input to output data.", e);
                }
            });

//            int readSize; // 读取到的数据长度
//            byte[] buffer = new byte[BUFFER_SIZE]; // 通过 byte 作为数据中转，用于存放循环读取的临时数据
//            while ((readSize = in.read(buffer)) != -1)
//                _out.write(buffer, 0, readSize);

            _out.flush();
        } catch (IOException e) {
            log.warn("Error occurred when copying input to output data.", e);
            throw new UncheckedIOException("Error occurred when copying input to output data.", e);
        }
    }

    /*
     * 输入流复制到输出流
     * New JDK 9 新特性，直接复制流，不用自己写循环了
     *
     * @param in  输入流
     * @return 复制了多少字节

    public  long write(InputStream in) {
        try {
            return in.transferTo(out); // 等价 Spring StreamUtils.copy(in, out)
        } catch (IOException e) {
            throw new RuntimeException("复制流的时候失败", e);
        }
    }
 */

    /**
     * 送入的 byte[] 转换为输出流。可指定 byte[] 某一部分数据。注意这函数不会关闭输出流（特别在截取部分数据的时候），请记得在适当的时候将其关闭。
     *
     * @param data   输入的数据
     * @param off    偏移
     * @param length 长度
     */
    public void write(byte[] data, int off, int length) {
        try {
            OutputStream bOut = isBuffered ? new BufferedOutputStream(out, BUFFER_SIZE) : out;

            if (off == 0 && length == 0)
                bOut.write(data);
            else
                bOut.write(data, off, length);

            bOut.flush();
        } catch (IOException e) {
            log.warn("Error occurred when writing bytes to a OutputStream.", e);
            throw new UncheckedIOException("Error occurred when writing bytes to a OutputStream.", e);
        }
    }

    public void write(byte[] data) {
        write(data, 0, 0);
    }
}
