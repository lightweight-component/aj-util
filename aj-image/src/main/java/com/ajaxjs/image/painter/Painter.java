package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.EmbeddedElement;

import java.awt.*;

/**
 * 绘制指定类型元素的统一接口。
 * <p>类型参数将画笔与元素配对，例如文字画笔只接收 TextEmbedded。
 * 调用方持有 Graphics2D 的生命周期，内置实现用子上下文绘制，不关闭传入的画笔。</p>
 *
 * @param <T> 此画笔能够绘制的元素类型
 */
public interface Painter<T extends EmbeddedElement> {
    /**
     * 绘制一次元素。
     *
     * @param g           目标绘图上下文，由调用方创建和释放
     * @param element     与画笔类型匹配的元素，绘制所需属性应已设置
     * @param canvasWidth 画布宽度，单位为像素，用于计算水平居中
     */
    void draw(Graphics2D g, T element, int canvasWidth);

    /**
     * 以元素配置的宽高作为单元尺寸进行平铺。
     * <p>间距和隔行偏移取自元素配置，实际循环交给带单元尺寸参数的重载。
     * 文字和图片画笔可以重写本方法，以计算字体尺寸或缩放后的图片尺寸。</p>
     *
     * @param g            目标绘图上下文，不会被关闭
     * @param element      平铺元素，宽高须可用
     * @param canvasWidth  画布宽度，单位为像素
     * @param canvasHeight 画布高度，单位为像素
     * @throws IllegalArgumentException 单元尺寸加间距后的步长不合法时抛出
     */
    default void drawRepeat(Graphics2D g, T element, int canvasWidth, int canvasHeight) {
        // 文字、矩形直接使用元素宽高；缩放图片可调用下面的重载，传入实际绘制尺寸。
        drawRepeat(g, element, canvasWidth, canvasHeight, element.getWidth(), element.getHeight());
    }

    /**
     * 按给定的实际单元尺寸平铺，支持缩放图片和文字的测量尺寸。
     * <p>从原位置向左、向上回退至画布边缘或边缘外，再逐列逐行绘制。
     * 第 2、4、6 行按 repeatRowOffset 向左偏移。绘制通过移动画笔完成，
     * 不修改元素坐标；循环范围外的像素由目标画布的裁剪区处理。</p>
     *
     * @param g            目标绘图上下文，不会被关闭或改变状态
     * @param element      要平铺的元素
     * @param canvasWidth  平铺终止的画布宽度，单位为像素
     * @param canvasHeight 平铺终止的画布高度，单位为像素
     * @param tileWidth    单个元素的实际绘制宽度，单位为像素
     * @param tileHeight   单个元素的实际绘制高度，单位为像素
     * @throws IllegalArgumentException 水平或垂直步长非正，或超过整数范围时抛出
     */
    default void drawRepeat(Graphics2D g, T element, int canvasWidth, int canvasHeight, int tileWidth, int tileHeight) {
        int originalX = element.getX(), originalY = element.getY();
        int currentX = originalX, currentY = originalY;
        long stepX = (long) element.getRepeatPaddingHorizontal() + tileWidth;
        long stepY = (long) element.getRepeatPaddingVertical() + tileHeight;

        // 步长必须能向前推进，使用 long 避免尺寸与间距相加时溢出。
        if (stepX <= 0 || stepY <= 0 || stepX > Integer.MAX_VALUE || stepY > Integer.MAX_VALUE)
            throw new IllegalArgumentException("平铺步长必须大于 0 且不能超出整数范围");

        // 回退到左边、上边的起点，保留元素原来的间隔规律。
        if (currentX > 0)
            currentX -= (int) (((currentX + stepX - 1) / stepX) * stepX);
        if (currentY > 0)
            currentY -= (int) (((currentY + stepY - 1) / stepY) * stepY);
        int startY = currentY;

        // 外层逐列向右，内层逐行向下。用 long 循环变量避免最后一次推进时溢出。
        for (long x = currentX; x < canvasWidth; x += stepX) {
            int row = 0;

            for (long y = startY; y < canvasHeight; y += stepY) {
                int offset = row % 2 == 0 ? 0 : element.getRepeatRowOffset();
                Graphics2D tileGraphics = (Graphics2D) g.create();

                try {
                    // 移动画笔而不是修改元素，文字基线也随画笔移动。
                    // 每格使用独立上下文，旋转、透明度等设置不会影响下一格。
                    tileGraphics.translate((double) x - originalX - offset, (double) y - originalY);
                    draw(tileGraphics, element, canvasWidth);
                } finally {
                    tileGraphics.dispose();
                }

                row++;
            }
        }
    }
}
