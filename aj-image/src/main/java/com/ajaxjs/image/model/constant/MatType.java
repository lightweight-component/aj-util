package com.ajaxjs.image.model.constant;

/**
 * 像素矩阵的通道格式；这些字节是像素值，不是 PNG、JPEG 等编码后的文件内容。
 * <p>通道数决定每个像素占用多少字节，Java byte 的负值可通过 {@code value & 0xff} 还原为 0 到 255。</p>
 */
public enum MatType {
    /**
     * NV21 半平面 YUV 格式的预留枚举；当前 ImageMatrix 不支持转换，调用会抛出异常。
     */
    NV21,

    /**
     * 每像素 3 字节，通道顺序为红、绿、蓝。
     */
    RGB,

    /**
     * 每像素 3 字节，通道顺序为蓝、绿、红。
     */
    BGR,

    /**
     * 每像素 4 字节，逻辑通道顺序为红、绿、蓝、透明度。
     * <p>当前 createRGBAImage 方法会丢弃透明度，不能用于透明图片的无损往返。</p>
     */
    RGBA,

    /**
     * 每像素 1 字节，表示灰度亮度。
     */
    GRAY
}
