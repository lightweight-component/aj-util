package com.ajaxjs.image.constant.enums;


public enum OutputFormat {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    BMP("bmp");

    public final String name;

    OutputFormat(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
