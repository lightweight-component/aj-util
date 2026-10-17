package com.ajaxjs.image.model.constant;

/**
 * 图片尺寸的计算方式；单边缩放保留宽高比，双边指定可能拉伸图片。
 */
public enum ZoomMode {
    /**
     * 使用图片原始宽高，不采用配置中的目标宽高。
     */
    Origin,

    /**
     * 采用指定宽度，按原图宽高比计算高度。
     */
    Width,

    /**
     * 采用指定高度，按原图宽高比计算宽度。
     */
    Height,

    /**
     * 同时采用指定宽度和高度，必要时拉伸图片，不保证原始宽高比。
     */
    WidthHeight;
}
