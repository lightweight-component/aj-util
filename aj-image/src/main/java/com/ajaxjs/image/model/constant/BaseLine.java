package com.ajaxjs.image.model.constant;

/**
 * 文字纵坐标的参考位置；drawY 未明确指定时，TextPainter 按此项换算 Java2D 基线。
 */
public enum BaseLine {
    /**
     * y 表示字体顶部；实际基线为 y 加字体上升高度。
     */
    Top,

    /**
     * y 表示字体上下升高度范围的中间位置。
     */
    Middle,

    /**
     * y 表示字体底部；实际基线为 y 减字体下降高度。
     */
    Bottom,

    /**
     * y 本身就是 Java2D 绘制文字使用的基线位置。
     */
    Base
}
