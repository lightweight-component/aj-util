package com.ajaxjs.image.embedd;

import com.ajaxjs.image.constant.enums.Direction;
import lombok.Data;

@Data
public abstract class EmbeddedElement {
    /**
     * 起始坐标x，相对左上角
     */
    private int x;

    /**
     * 起始坐标y，相对左上角
     */
    private int y;

    /**
     * 是否居中
     */
    private boolean center;

    /**
     * 绘制方向
     */
    private Direction direction = Direction.LeftRight;

    /**
     * 透明度
     */
    private float alpha = 1.0f;

    /**
     * 平铺
     */
    private boolean repeat;

    /**
     * 平铺水平间距
     */
    private int repeatPaddingHorizontal;

    /**
     * 平铺垂直间距
     */
    private int repeatPaddingVertical;

    /**
     * 平铺错位偏移量
     */
    private int repeatRowOffset;
}
