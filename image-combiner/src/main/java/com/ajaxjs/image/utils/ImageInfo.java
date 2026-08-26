package com.ajaxjs.image.utils;

import lombok.Data;

/**
 * 图片基本的信息
 */
@Data
public class ImageInfo {
    Integer width;

    Integer height;

    String suffix;

    String mime;
}
