package com.ajaxjs.image.model.constant;

/**
 * 元素相对 X 坐标的水平定位方式。
 * <p>只调整起点，不翻转像素，也不改变文字的字符顺序；水平居中设置优先于本枚举。</p>
 */
public enum Direction {
    /**
     * 从 x 向右绘制，x 对应元素左边缘。
     */
    LeftRight,

    /**
     * 从 x 向左展开，绘制左边缘为 x 减去元素实际宽度。
     */
    RightLeft,

    /**
     * 以 x 为水平中心向两侧展开，绘制左边缘为 x 减去一半实际宽度。
     */
    CenterLeftRight
}
