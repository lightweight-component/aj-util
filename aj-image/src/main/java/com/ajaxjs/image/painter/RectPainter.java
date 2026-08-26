package com.ajaxjs.image.painter;

import com.ajaxjs.image.constant.enums.Direction;
import com.ajaxjs.image.embedd.RectangleEmbedded;

import java.awt.*;

public class RectPainter implements Painter<RectangleEmbedded> {
    @Override
    public void draw(Graphics2D g, RectangleEmbedded element, int canvasWidth) {
        g.setColor(element.getColor()); // 设置颜色

        if (element.isCenter()) { // 设置居中（优先）和绘制方向
            int centerX = (canvasWidth - element.getWidth()) / 2;
            element.setX(centerX);
        } else if (element.getDirection() == Direction.RightLeft)
            element.setX(element.getX() - element.getWidth());
        else if (element.getDirection() == Direction.CenterLeftRight)
            element.setX(element.getX() - element.getWidth() / 2);

        if (element.getFromColor() != null) { // 设置渐变
            float fromX = 0, fromY = 0, toX = 0, toY = 0;

            switch (element.getGradientDirection()) {
                case TopBottom:
                    fromX = element.getX() + (float) element.getWidth() / 2;
                    fromY = element.getY() - element.getFromExtend();
                    toX = fromX;
                    toY = element.getY() + element.getHeight() + element.getToExtend();
                    break;
                case LeftRight:
                    fromX = element.getX() - element.getFromExtend();
                    fromY = element.getY() + (float) element.getHeight() / 2;
                    toX = element.getX() + element.getWidth() + element.getToExtend();
                    toY = fromY;
                    break;
                case LeftTopRightBottom:
                    fromX = element.getX() - (float) Math.sqrt(element.getFromExtend());
                    fromY = element.getY() - (float) Math.sqrt(element.getFromExtend());
                    toX = element.getX() + element.getWidth() + (float) Math.sqrt(element.getToExtend());
                    toY = element.getY() + element.getHeight() + (float) Math.sqrt(element.getToExtend());
                    break;
                case RightTopLeftBottom:
                    fromX = element.getX() + element.getWidth() + (float) Math.sqrt(element.getFromExtend());
                    fromY = element.getY() - (float) Math.sqrt(element.getFromExtend());
                    toX = element.getX() - (float) Math.sqrt(element.getToExtend());
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
                g.drawRoundRect(element.getX(), element.getY(), element.getWidth(), element.getHeight(), element.getRoundCorner(), element.getRoundCorner());
            else
                g.drawRect(element.getX(), element.getY(), element.getWidth(), element.getHeight());
        } else {
            if (element.getRoundCorner() != 0)
                g.fillRoundRect(element.getX(), element.getY(), element.getWidth(), element.getHeight(), element.getRoundCorner(), element.getRoundCorner());
            else
                g.fillRect(element.getX(), element.getY(), element.getWidth(), element.getHeight());
        }
    }

    @Override
    public void drawRepeat(Graphics2D g, RectangleEmbedded element, int canvasWidth, int canvasHeight) {
        int currentX = element.getX(),  currentY = element.getY();

        while (currentX > 0)  // 起始坐标归位
            currentX = currentX - element.getRepeatPaddingHorizontal() - element.getWidth();

        while (currentY > 0)
            currentY = currentY - element.getRepeatPaddingVertical() - element.getHeight();

        int startY = currentY;

        while (currentX < canvasWidth) { // 从左往右绘制
            int i = 0;
            element.setX(currentX);
            currentX = currentX + element.getRepeatPaddingHorizontal() + element.getWidth();

            while (currentY < canvasHeight) { // 从上往下绘制
                if (i % 2 != 0)  // 偶数行错位效果
                    element.setX(element.getX() - element.getRepeatRowOffset());

                element.setY(currentY);
                currentY = currentY + element.getRepeatPaddingVertical() + element.getHeight();
                draw(g, element, canvasWidth);

                if (i % 2 != 0) // 还原偏移的 x 坐标
                    element.setX(element.getX() + element.getRepeatRowOffset());

                i++;
            }

            currentY = startY; // 重置 y 坐标
        }
    }
}
