package com.ajaxjs.image.constant.enums;

/**
 * 缩放模式枚举
 */
public enum ZoomMode {
    /**
     * 原始比例，不缩放
     */
    Origin,
    
    /**
     * 指定宽度，高度按比例自动计算
     */
    Width,

    /**
     * 指定高度，宽度按比例自动计算
     */
    Height,

    /**
     * 指定高度和宽度，强制缩放
     */
    WidthHeight;
}
