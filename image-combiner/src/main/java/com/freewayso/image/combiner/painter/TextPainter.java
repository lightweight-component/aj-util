package com.freewayso.image.combiner.painter;

import com.freewayso.image.combiner.element.CombineElement;
import com.freewayso.image.combiner.element.TextElement;
import com.ajaxjs.image.constant.enums.Direction;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本绘制器
 */
public class TextPainter implements IPainter {
    @Override
    public void draw(Graphics2D g, CombineElement element, int canvasWidth) {
        TextElement textElement = (TextElement) element;
        List<TextElement> textLineElements = new ArrayList<>();    // 首先计算是否要换行（由于拆行计算比较耗资源，不设置换行则直接用原始对象绘制）
        textLineElements.add(textElement);

        if (textElement.isAutoBreakLine())
            textLineElements = textElement.getBreakLineElements();

        for (int i = 0; i < textLineElements.size(); i++) {
            TextElement firstLineElement = textLineElements.get(0);
            TextElement currentLineElement = textLineElements.get(i);

            if (!textElement.isAutoBreakLine() && currentLineElement.getAutoFitMaxWidth() != null) { // 自适应宽度（若超出指定宽度，缩小字体适应之，若设置了自动换行，则忽略）
                int fontSize = currentLineElement.getFont().getSize();

                while (true) {
                    if (currentLineElement.getWidth() <= currentLineElement.getAutoFitMaxWidth())
                        break;
                    else {
                        if (fontSize <= currentLineElement.getAutoFitMinFontSize()) {    //字号减到最低要求，则开始减文本
                            String text = textElement.getText();

                            if (text.endsWith("..."))
                                text = text.substring(0, text.length() - 3);   //去掉"..."

                            textElement.setText(text.substring(0, text.length() - 1) + "...");
                        } else {
                            Font newFont = currentLineElement.getFont().deriveFont((float) fontSize--);
                            currentLineElement.setFont(newFont);

                            if (fontSize <= 1)
                                break;
                        }
                    }
                }
            }

            g.setFont(currentLineElement.getFont()); // 设置字体、颜色
            g.setColor(currentLineElement.getColor());

            if (currentLineElement.isCenter()) { // 设置居中（多行的时候，第一行居中，后续行按对齐方式计算）
                if (i == 0)
                    currentLineElement.setX((canvasWidth - currentLineElement.getWidth()) / 2);
                 else {
                    switch (textElement.getLineAlign()) {
                        case Left:
                            currentLineElement.setX(firstLineElement.getX());
                            break;
                        case Center:
                            currentLineElement.setX((canvasWidth - currentLineElement.getWidth()) / 2);
                            break;
                        case Right:
                            currentLineElement.setX(firstLineElement.getX() + firstLineElement.getWidth() - currentLineElement.getWidth());
                            break;
                    }
                }
            } else {
                if (i == 0) {
                    if (currentLineElement.getDirection() == Direction.RightLeft)  // 绘制方向（只处理第一个元素即可，后续元素会参照第一个元素的坐标来计算）
                        currentLineElement.setX(currentLineElement.getX() - currentLineElement.getWidth());
                    else if (currentLineElement.getDirection() == Direction.CenterLeftRight)
                        currentLineElement.setX(currentLineElement.getX() - currentLineElement.getWidth() / 2);
                } else {
                    switch (textElement.getLineAlign()) {
                        case Left:
                            currentLineElement.setX(firstLineElement.getX());
                            break;
                        case Center:
                            currentLineElement.setX(firstLineElement.getX() + (firstLineElement.getWidth() - currentLineElement.getWidth()) / 2);
                            break;
                        case Right:
                            currentLineElement.setX(firstLineElement.getX() + firstLineElement.getWidth() - currentLineElement.getWidth());
                            break;
                    }
                }
            }

            if (currentLineElement.getRotate() != null) // 旋转
                g.rotate(Math.toRadians(currentLineElement.getRotate()), currentLineElement.getX() + (double) currentLineElement.getWidth() / 2, currentLineElement.getDrawY());

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentLineElement.getAlpha())); // 设置透明度

            if (currentLineElement.isStrikeThrough()) { // 带删除线样式的文字要特殊处理
                AttributedString as = new AttributedString(currentLineElement.getText());
                as.addAttribute(TextAttribute.FONT, currentLineElement.getFont());
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, 0, currentLineElement.getText().length());
                g.drawString(as.getIterator(), currentLineElement.getX(), currentLineElement.getDrawY());
            } else
                g.drawString(currentLineElement.getText(), currentLineElement.getX(), currentLineElement.getDrawY());

            if (currentLineElement.getRotate() != null) // 绘制完后反向旋转，以免影响后续元素
                g.rotate(-Math.toRadians(currentLineElement.getRotate()), currentLineElement.getX() + currentLineElement.getWidth() / 2, currentLineElement.getDrawY());
        }
    }

    @Override
    public void drawRepeat(Graphics2D g, CombineElement element, int canvasWidth, int canvasHeight) {
        TextElement textElement = (TextElement) element;
        int currentX = element.getX(), currentY = element.getY();

        while (currentX > 0) // 起始坐标归位
            currentX = currentX - textElement.getRepeatPaddingHorizontal() - textElement.getWidth();

        while (currentY > 0)
            currentY = currentY - textElement.getRepeatPaddingVertical() - textElement.getHeight();

        int startY = currentY;

        while (currentX < canvasWidth) { // 从左往右绘制
            int i = 0;
            textElement.setX(currentX);
            currentX = currentX + textElement.getRepeatPaddingHorizontal() + textElement.getWidth();

            while (currentY < canvasHeight) {  // 从上往下绘制
                if (i % 2 != 0)// 偶数行错位效果
                    textElement.setX(textElement.getX() - textElement.getRepeatRowOffset());

                textElement.setY(currentY);
                currentY = currentY + textElement.getRepeatPaddingVertical() + textElement.getHeight();
                draw(g, textElement, canvasWidth);

                if (i % 2 != 0)// 还原偏移的x坐标
                    textElement.setX(textElement.getX() + textElement.getRepeatRowOffset());

                i++;
            }

            currentY = startY;  // 重置y坐标
        }
    }
}