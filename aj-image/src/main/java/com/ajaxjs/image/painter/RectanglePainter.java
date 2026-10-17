package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.constant.Direction;

import java.awt.*;

/**
 * 矩形画笔，绘制纯色或渐变的矩形填充及边框。
 * <p>公共平铺方法负责重复定位，本类只处理一次绘制；不修改元素坐标或调用方画笔状态。</p>
 */
public class RectanglePainter implements Painter<RectangleEmbedded> {
    /**
     * 根据宽高、颜色、圆角和边框配置绘制矩形。
     * <p>启用居中时优先使用居中位置；fromColor 非 null 时采用渐变。
     * borderSize 非 null 时只描边，否则填充内部。</p>
     *
     * @param g           目标画笔
     * @param element     矩形配置，必须设置宽高；渐变时还须提供结束颜色和方向
     * @param canvasWidth 画布宽度，单位为像素，用于水平居中
     */
    @Override
    public void draw(Graphics2D g, RectangleEmbedded element, int canvasWidth) {
        int x = element.getX();
        Graphics2D originalGraphics = g; // 独立的绘图上下文负责保存颜色、透明度和旋转状态，结束时统一释放。
        g = (Graphics2D) originalGraphics.create();

        try {
            g.setColor(element.getColor()); // 设置颜色

            if (element.isCenter())  // 设置居中（优先）和绘制方向
                x = (canvasWidth - element.getWidth()) / 2;
            else if (element.getDirection() == Direction.RightLeft)
                x = x - element.getWidth();
            else if (element.getDirection() == Direction.CenterLeftRight)
                x = x - element.getWidth() / 2;

            if (element.getFromColor() != null) { // 设置渐变
                float fromX = 0, fromY = 0, toX = 0, toY = 0;

                switch (element.getGradientDirection()) {
                    case TopBottom:
                        fromX = x + (float) element.getWidth() / 2;
                        fromY = element.getY() - element.getFromExtend();
                        toX = fromX;
                        toY = element.getY() + element.getHeight() + element.getToExtend();
                        break;
                    case LeftRight:
                        fromX = x - element.getFromExtend();
                        fromY = element.getY() + (float) element.getHeight() / 2;
                        toX = x + element.getWidth() + element.getToExtend();
                        toY = fromY;
                        break;
                    case LeftTopRightBottom:
                        fromX = x - (float) Math.sqrt(element.getFromExtend());
                        fromY = element.getY() - (float) Math.sqrt(element.getFromExtend());
                        toX = x + element.getWidth() + (float) Math.sqrt(element.getToExtend());
                        toY = element.getY() + element.getHeight() + (float) Math.sqrt(element.getToExtend());
                        break;
                    case RightTopLeftBottom:
                        fromX = x + element.getWidth() + (float) Math.sqrt(element.getFromExtend());
                        fromY = element.getY() - (float) Math.sqrt(element.getFromExtend());
                        toX = x - (float) Math.sqrt(element.getToExtend());
                        toY = element.getY() + element.getHeight() + (float) Math.sqrt(element.getToExtend());
                        break;
                }

                g.setPaint(new GradientPaint(fromX, fromY, element.getFromColor(), toX, toY, element.getToColor()));
            } else
                g.setPaint(null);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, element.getAlpha()));// 设置透明度
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (element.getBorderSize() != null) { // 根据是否有边框和圆角，采用不同的绘制方法
                g.setStroke(new BasicStroke(element.getBorderSize()));

                if (element.getRoundCorner() != 0)  // 圆角是 0 要用 drawRect，否则 border 大的时候会看出顶角线条不闭合
                    g.drawRoundRect(x, element.getY(), element.getWidth(), element.getHeight(), element.getRoundCorner(), element.getRoundCorner());
                else
                    g.drawRect(x, element.getY(), element.getWidth(), element.getHeight());
            } else {
                if (element.getRoundCorner() != 0)
                    g.fillRoundRect(x, element.getY(), element.getWidth(), element.getHeight(), element.getRoundCorner(), element.getRoundCorner());
                else
                    g.fillRect(x, element.getY(), element.getWidth(), element.getHeight());
            }
        } finally {
            g.dispose();
        }
    }

}
