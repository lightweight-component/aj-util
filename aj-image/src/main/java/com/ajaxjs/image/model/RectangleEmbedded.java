package com.ajaxjs.image.model;

import com.ajaxjs.image.model.constant.GradientDirection;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

/**
 * 矩形元素，支持纯色、渐变、圆角和描边。
 * <p>绘制前设置宽高；borderSize 为 null 时填充内部，非 null 时只画边框。
 * 启用渐变时需要同时设置起止颜色及渐变方向。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RectangleEmbedded extends EmbeddedElement {
    /**
     * 矩形宽度，单位为像素；绘制前必须设置。
     */
    private Integer width;

    /**
     * 矩形高度，单位为像素；绘制前必须设置。
     */
    private Integer height;

    /**
     * 边框线宽，单位为像素；{@code null} 表示填充矩形，非 null 表示只画轮廓。
     */
    private Integer borderSize;

    /**
     * 圆角弧宽和弧高，单位为像素；默认 0，表示直角矩形，不应设为 null。
     */
    private Integer roundCorner = 0;

    /**
     * 纯色矩形或边框的颜色，默认白色；设置渐变起始颜色后使用渐变绘制。
     */
    private Color color = new Color(255, 255, 255);

    /* ---------渐变相关属性--------- */

    /**
     * 渐变起始颜色；{@code null} 表示关闭渐变，非 null 时还需设置结束颜色和方向。
     */
    private Color fromColor;

    /**
     * 渐变结束颜色；启用渐变时必须提供。
     */
    private Color toColor;

    /**
     * 渐变起点向矩形外延伸的配置值，默认 0。
     * <p>水平、垂直方向按像素延伸；当前对角线实现按此值的平方根调整两个坐标。</p>
     */
    private Integer fromExtend = 0;

    /**
     * 渐变终点向外延伸的配置值，默认 0；解释方式与 fromExtend 相同，应使用非负值。
     */
    private Integer toExtend = 0;

    /**
     * 渐变方向，没有默认值；启用渐变时必须设置。
     */
    private GradientDirection gradientDirection;
}
