package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * The reader class for reading data from an input stream.
 * InputStream is used for reading raw byte data from a source like a file, network socket, or memory buffer.
 * If it does not meet the need, you can refer to the Spring StreamUtils/ResourceUtils/FileCopyUtils/FileSystemUtils.
 */
@Slf4j
@RequiredArgsConstructor
public class DataReader {
    /**
     * The input stream to read data from.
     */
    private final InputStream in;

    /**
     * The character encoding to use when reading the input stream. UTF-8 is used by default.
     */
    private Charset encode = StandardCharsets.UTF_8;

    /**
     * Create a DataReader instance with the specified input stream and character encoding.
     *
     * @param in     The input stream to read data from.
     * @param encode The character encoding to use when reading the input stream.
     */
    public DataReader(InputStream in, Charset encode) {
        this(in);
        this.encode = encode;
    }

    /**
     * Read the data from the input stream as line by line.
     *
     * @param fn provided function to consume each line of data
     */
    public void readAsLineString(Consumer<String> fn) {
        /*
         * 装饰器模式，又称为包装器，可以在不修改被包装类的情况下动态添加功能（例如缓冲区功能）
         * 这里使用 BufferedReader 为输入流添加缓冲功能
         */
        try (InputStreamReader inReader = new InputStreamReader(in, encode);
             BufferedReader reader = new BufferedReader(inReader)) {
            String line;

            while ((line = reader.readLine()) != null) // 一次读入一行，直到读入 null 表示文件结束
                fn.accept(line);// 指定编码集的另外一种写法 line = new String(line.getBytes(), encodingSet);
        } catch (IOException e) {
            log.warn("Error occurred when reading the data from stream.", e);
            throw new UncheckedIOException("Error occurred when reading the data from stream.", e);
        }
    }

    /**
     * Read the data from the input stream as a string.
     *
     * @return String
     */
    public String readAsString() {
        StringBuffer result = new StringBuffer();

        readAsLineString(line -> {
            result.append(line);
            result.append(CommonConstant.NEW_LINE);
        });

        return result.toString();
    }

    /**
     * Read the data from the input stream as bytes.
     * This equivalent to using the DataWriter class to write the data to a ByteArrayOutputStream, that means the data is stored in memory.
     *
     * @return The bytes in RAM.
     */
    public byte[] readAsBytes() {// 等价于 Spring StreamUtils.copyToByteArray(InputStream in)：将输入流的内容复制到字节数组
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new DataWriter(out).write(in);

            return out.toByteArray();
        } catch (IOException e) {
            log.warn("Error occurred when reading the data from stream as bytes.", e);
            throw new UncheckedIOException("Error occurred when reading the data from stream as bytes.", e);
        }
    }

}
