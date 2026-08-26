package com.freewayso.image.combiner.painter;

import com.freewayso.image.combiner.element.CombineElement;
import com.freewayso.image.combiner.element.RectangleElement;
import com.ajaxjs.image.constant.enums.Direction;

import java.awt.*;

/**
 * 矩形绘制器
 */
public class RectanglePainter implements IPainter {
    @Override
    public void draw(Graphics2D g, CombineElement element, int canvasWidth) {
        RectangleElement rectangleElement = (RectangleElement) element;
        g.setColor(rectangleElement.getColor()); // 设置颜色

        if (rectangleElement.isCenter()) { // 设置居中（优先）和绘制方向
            int centerX = (canvasWidth - rectangleElement.getWidth()) / 2;
            rectangleElement.setX(centerX);
        } else if (rectangleElement.getDirection() == Direction.RightLeft)
            rectangleElement.setX(rectangleElement.getX() - rectangleElement.getWidth());
        else if (rectangleElement.getDirection() == Direction.CenterLeftRight)
            rectangleElement.setX(rectangleElement.getX() - rectangleElement.getWidth() / 2);

        if (rectangleElement.getFromColor() != null) { // 设置渐变
            float fromX = 0, fromY = 0, toX = 0, toY = 0;

            switch (rectangleElement.getGradientDirection()) {
                case TopBottom:
                    fromX = rectangleElement.getX() + (float) rectangleElement.getWidth() / 2;
                    fromY = rectangleElement.getY() - rectangleElement.getFromExtend();
                    toX = fromX;
                    toY = rectangleElement.getY() + rectangleElement.getHeight() + rectangleElement.getToExtend();
                    break;
                case LeftRight:
                    fromX = rectangleElement.getX() - rectangleElement.getFromExtend();
                    fromY = rectangleElement.getY() + (float) rectangleElement.getHeight() / 2;
                    toX = rectangleElement.getX() + rectangleElement.getWidth() + rectangleElement.getToExtend();
                    toY = fromY;
                    break;
                case LeftTopRightBottom:
                    fromX = rectangleElement.getX() - (float) Math.sqrt(rectangleElement.getFromExtend());
                    fromY = rectangleElement.getY() - (float) Math.sqrt(rectangleElement.getFromExtend());
                    toX = rectangleElement.getX() + rectangleElement.getWidth() + (float) Math.sqrt(rectangleElement.getToExtend());
                    toY = rectangleElement.getY() + rectangleElement.getHeight() + (float) Math.sqrt(rectangleElement.getToExtend());
                    break;
                case RightTopLeftBottom:
                    fromX = rectangleElement.getX() + rectangleElement.getWidth() + (float) Math.sqrt(rectangleElement.getFromExtend());
                    fromY = rectangleElement.getY() - (float) Math.sqrt(rectangleElement.getFromExtend());
                    toX = rectangleElement.getX() - (float) Math.sqrt(rectangleElement.getToExtend());
                    toY = rectangleElement.getY() + rectangleElement.getHeight() + (float) Math.sqrt(rectangleElement.getToExtend());
                    break;
            }

            g.setPaint(new GradientPaint(fromX, fromY, rectangleElement.getFromColor(), toX, toY, rectangleElement.getToColor()));
        } else
            g.setPaint(null);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rectangleElement.getAlpha()));// 设置透明度
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (rectangleElement.getBorderSize() != null) { // 根据是否有边框和圆角，采用不同的绘制方法
            g.setStroke(new BasicStroke(rectangleElement.getBorderSize()));

            if (rectangleElement.getRoundCorner() != 0)  // 圆角是0要用drawRect，否则border大的时候会看出顶角线条不闭合
                g.drawRoundRect(rectangleElement.getX(), rectangleElement.getY(), rectangleElement.getWidth(), rectangleElement.getHeight(), rectangleElement.getRoundCorner(), rectangleElement.getRoundCorner());
            else
                g.drawRect(rectangleElement.getX(), rectangleElement.getY(), rectangleElement.getWidth(), rectangleElement.getHeight());
        } else {
            if (rectangleElement.getRoundCorner() != 0)
                g.fillRoundRect(rectangleElement.getX(), rectangleElement.getY(), rectangleElement.getWidth(), rectangleElement.getHeight(), rectangleElement.getRoundCorner(), rectangleElement.getRoundCorner());
            else
                g.fillRect(rectangleElement.getX(), rectangleElement.getY(), rectangleElement.getWidth(), rectangleElement.getHeight());
        }
    }

    @Override
    public void drawRepeat(Graphics2D g, CombineElement element, int canvasWidth, int canvasHeight) {
        RectangleElement rectangleElement = (RectangleElement) element;
        int currentX = element.getX(),  currentY = element.getY();

        while (currentX > 0)  // 起始坐标归位
            currentX = currentX - rectangleElement.getRepeatPaddingHorizontal() - rectangleElement.getWidth();

        while (currentY > 0)
            currentY = currentY - rectangleElement.getRepeatPaddingVertical() - rectangleElement.getHeight();

        int startY = currentY;

        while (currentX < canvasWidth) { // 从左往右绘制
            int i = 0;
            rectangleElement.setX(currentX);
            currentX = currentX + rectangleElement.getRepeatPaddingHorizontal() + rectangleElement.getWidth();

            while (currentY < canvasHeight) { // 从上往下绘制
                if (i % 2 != 0)  // 偶数行错位效果
                    rectangleElement.setX(rectangleElement.getX() - rectangleElement.getRepeatRowOffset());

                rectangleElement.setY(currentY);
                currentY = currentY + rectangleElement.getRepeatPaddingVertical() + rectangleElement.getHeight();
                draw(g, rectangleElement, canvasWidth);

                if (i % 2 != 0) // 还原偏移的 x 坐标
                    rectangleElement.setX(rectangleElement.getX() + rectangleElement.getRepeatRowOffset());

                i++;
            }

            currentY = startY; // 重置 y 坐标
        }
    }
}