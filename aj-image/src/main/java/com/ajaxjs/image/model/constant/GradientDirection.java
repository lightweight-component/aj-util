package com.ajaxjs.image.model.constant;

/**
 * 矩形颜色从起始颜色过渡到结束颜色的方向。
 */
public enum GradientDirection {
    /**
     * 从矩形顶部向底部渐变。
     */
    TopBottom,

    /**
     * 从矩形左边向右边渐变。
     */
    LeftRight,

    /**
     * 从左上角向右下角渐变。
     */
    LeftTopRightBottom,

    /**
     * 从右上角向左下角渐变。
     */
    RightTopLeftBottom;
}
