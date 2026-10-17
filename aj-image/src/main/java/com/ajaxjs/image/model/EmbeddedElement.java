package com.ajaxjs.image.model;

import com.ajaxjs.image.model.constant.Direction;
import lombok.Data;

/**
 * 所有可绘制元素的公共属性，包括坐标、对齐方向、透明度和平铺设置。
 * <p>坐标单位为像素，画布左上角为原点，向右为 X 正方向，向下为 Y 正方向。
 * 具体宽高由子类提供，图片实际绘制尺寸还会受缩放模式影响。</p>
 * <p>元素是可变的配置对象；内置 Painter 绘制时保留原始坐标。</p>
 */
@Data
public abstract class EmbeddedElement {
    /**
     * 获取布局或平铺使用的元素宽度。
     *
     * @return 宽度，单位为像素；未配置尺寸的子类可能返回 {@code null}
     */
    public abstract Integer getWidth();

    /**
     * 获取布局或平铺使用的元素高度。
     *
     * @return 高度，单位为像素；未配置尺寸的子类可能返回 {@code null}
     */
    public abstract Integer getHeight();

    /**
     * 水平定位坐标，默认 0，单位为像素。
     * <p>左到右绘制时表示左边缘；右到左时表示右边缘；向两侧展开时表示水平中心。</p>
     */
    private int x;

    /**
     * 垂直定位坐标，默认 0，单位为像素。
     * <p>图片和矩形以顶边定位；文字的含义由基线设置决定。</p>
     */
    private int y;

    /**
     * 是否在画布宽度范围内水平居中，默认 {@code false}；启用后优先于绘制方向。
     */
    private boolean center;

    /**
     * 水平定位方式，默认 {@link Direction#LeftRight}；不会翻转图片或改变文字字符顺序。
     */
    private Direction direction = Direction.LeftRight;

    /**
     * 绘制透明度：0 表示完全透明，1 表示完全不透明；默认 1，合法范围为 0 到 1。
     */
    private float alpha = 1.0f;

    /**
     * 是否平铺，默认 {@code false}；ImageContainer 据此选择普通绘制或重复绘制。
     */
    private boolean repeat;

    /**
     * 相邻两列元素之间的水平间距，单位为像素，默认 0。
     * <p>与实际绘制宽度相加后必须为正数，负间距可使元素重叠。</p>
     */
    private int repeatPaddingHorizontal;

    /**
     * 相邻两行元素之间的垂直间距，单位为像素，默认 0。
     * <p>与实际绘制高度相加后必须为正数。</p>
     */
    private int repeatPaddingVertical;

    /**
     * 隔行平铺时向左错开的距离，单位为像素，默认 0。
     * <p>从平铺起始行算起，第 2、4、6 行应用此偏移。</p>
     */
    private int repeatRowOffset;
}
