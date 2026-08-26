package com.freewayso.image.combiner.element;

import com.ajaxjs.image.constant.enums.Direction;
import lombok.Data;

/**
 * 合并元素父类
 */
@Data
public abstract class CombineElement<T> {
    private int x;                          //起始坐标x，相对左上角
    private int y;                          //起始坐标y，相对左上角
    private boolean center;                 //是否居中
    private Direction direction = Direction.LeftRight;    //绘制方向
    private float alpha = 1.0f;             //透明度
    private boolean repeat;                 //平铺
    private int repeatPaddingHorizontal;   //平铺水平间距
    private int repeatPaddingVertical;     //平铺垂直间距
    private int repeatRowOffset;           //平铺错位偏移量

    /**
     * 设置x坐标
     */
    public CombineElement setX(int x) {
        this.x = x;
        return   this;
    }

    /**
     * 设置y坐标
     */
    public T setY(int y) {
        this.y = y;
        return (T) this;
    }

    public boolean isCenter() {
        return center;
    }

    /**
     * 设置居中绘制（会忽略x坐标，改为自动计算）
     */
    public T setCenter(boolean center) {
        this.center = center;
        return (T) this;
    }

    /**
     * 设置绘制方向
     *
     * @param direction
     * @return
     */
    public T setDirection(Direction direction) {
        this.direction = direction;
        return (T) this;
    }

    /**
     * 设置透明度
     *
     * @param alpha
     * @return
     */
    public T setAlpha(float alpha) {
        this.alpha = alpha;
        return (T) this;
    }

    public boolean isRepeat() {
        return repeat;
    }

    /**
     * 设置循环绘制（常用于水印平铺）
     *
     * @param repeat
     * @return
     */
    public T setRepeat(boolean repeat) {
        this.repeat = repeat;
        return (T) this;
    }

    /**
     * 设置循环绘制（常用于水印平铺）
     *
     * @param repeat
     * @param repeatPadding 元素间距
     * @return
     */
    public T setRepeat(boolean repeat, int repeatPadding) {
        this.repeat = repeat;
        this.repeatPaddingHorizontal = repeatPadding;
        this.repeatPaddingVertical = repeatPadding;

        return (T) this;
    }

    /**
     * 设置循环绘制（常用于水印平铺）
     *
     * @param repeat
     * @param repeatPaddingHorizontal 元素水平间距
     * @param repeatPaddingVertical   元素垂直间距
     * @return
     */
    public T setRepeat(boolean repeat, int repeatPaddingHorizontal, int repeatPaddingVertical) {
        this.repeat = repeat;
        this.repeatPaddingHorizontal = repeatPaddingHorizontal;
        this.repeatPaddingVertical = repeatPaddingVertical;

        return (T) this;
    }

    /**
     * 设置循环绘制（常用于水印平铺）
     *
     * @param repeat
     * @param repeatPaddingHorizontal 元素水平间距
     * @param repeatPaddingVertical   元素垂直间距
     * @param repeatRowOffset         偶数行偏移量（一种错位效果）
     * @return
     */
    public T setRepeat(boolean repeat, int repeatPaddingHorizontal, int repeatPaddingVertical, int repeatRowOffset) {
        this.repeat = repeat;
        this.repeatPaddingHorizontal = repeatPaddingHorizontal;
        this.repeatPaddingVertical = repeatPaddingVertical;
        this.repeatRowOffset = repeatRowOffset;

        return (T) this;
    }
}
