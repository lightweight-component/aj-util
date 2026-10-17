package com.ajaxjs.image.model.constant;

/**
 * 输出图片的编码格式；实际支持范围取决于运行环境中的 ImageIO 编码器。
 */
public enum ImageFormat {
    /**
     * BMP 位图格式；常用于不需要透明背景的像素图片。
     */
    BMP,
    /**
     * GIF 格式，颜色数量有限；当前写入接口输出单帧，不创建动画。
     */
    GIF,
    /**
     * JPEG 有损压缩格式，适合照片，不保留透明通道。
     */
    JPG,
    /**
     * PNG 无损压缩格式，支持透明通道，适合圆角图片或透明背景。
     */
    PNG,
}
