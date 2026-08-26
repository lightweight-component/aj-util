package com.ajaxjs.image.embedd;

import com.ajaxjs.image.constant.enums.GradientDirection;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class RectangleEmbedded extends EmbeddedElement {
    /**
     * 绘制宽度
     */
    private Integer width;

    /**
     * 绘制高度
     */
    private Integer height;

    /**
     * 边框大小
     */
    private Integer borderSize;

    /**
     * 圆角大小
     */
    private Integer roundCorner = 0;

    /**
     * 颜色，默认白色
     */
    private Color color = new Color(255, 255, 255);

    /* ---------渐变相关属性--------- */

    /**
     * 开始颜色
     */
    private Color fromColor;

    /**
     * 结束颜色
     */
    private Color toColor;

    /**
     * 开始位置延长（反向，影响渐变效果）
     */
    private Integer fromExtend = 0;

    /**
     * 结束位置延长（正向，影响渐变效果）
     */
    private Integer toExtend = 0;

    /**
     * 渐变方向
     */
    private GradientDirection gradientDirection;
}
