package com.ajaxjs.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * Base64Helper: 支持链式调用、类型安全、可配置的 Base64 编解码工具（支持 URL-safe）
 */
public class Base64Helper {
    public enum Mode {ENCODE, DECODE}

    private Mode mode = Mode.ENCODE;
    private byte[] inputBytes;
    private final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private boolean withoutPadding = false;
    private boolean urlSafe = false; // 新增：是否 URL-safe

    // 1. 设置编码/解码
    public Base64Helper encoder() {
        this.mode = Mode.ENCODE;
        return this;
    }

    public Base64Helper decoder() {
        this.mode = Mode.DECODE;
        return this;
    }

    // 2. 输入 String
    public Base64Helper input(String input) {
        return input(input, UTF8_CHARSET);
    }

    public Base64Helper input(String input, Charset charset) {
        Objects.requireNonNull(input, "input string");
        Objects.requireNonNull(charset, "charset");
        this.inputBytes = input.getBytes(charset);

        return this;
    }

    // 2. 输入 bytes
    public Base64Helper input(byte[] input) {
        Objects.requireNonNull(input, "input bytes");
        this.inputBytes = input;

        return this;
    }

    // 4. 编码时无 padding
    public Base64Helper withoutPadding() {
        this.withoutPadding = true;
        return this;
    }

    // 5. URL-safe 模式（编码和解码都适用）
    public Base64Helper urlSafe() {
        this.urlSafe = true;
        return this;
    }

    // 6. 结果获取（类型安全）
    public String getString() {
        byte[] result = process();
        return new String(result, UTF8_CHARSET);
    }

    public String getString(Charset charset) {
        byte[] result = process();

        return new String(result, charset);
    }

    public byte[] getBytes() {
        return process();
    }

    // 内部处理
    private byte[] process() {
        if (inputBytes == null)
            throw new IllegalStateException("Input is not set");

        if (mode == Mode.ENCODE) {
            Base64.Encoder encoder = urlSafe ?
                    (withoutPadding ? Base64.getUrlEncoder().withoutPadding() : Base64.getUrlEncoder())
                    : (withoutPadding ? Base64.getEncoder().withoutPadding() : Base64.getEncoder());

            return encoder.encode(inputBytes);
        } else {
            Base64.Decoder decoder = urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder();

            return decoder.decode(inputBytes);
        }
    }

    // 工厂

    public static Base64Helper encode() {
        return new Base64Helper().encoder();
    }

    public static Base64Helper decode() {
        return new Base64Helper().decoder();
    }
}