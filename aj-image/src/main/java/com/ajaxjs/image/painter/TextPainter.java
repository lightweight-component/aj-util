package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.Direction;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Collections;
import java.util.List;

/**
 * 文字画笔，支持字体、颜色、基线定位、删除线、旋转、缩小及省略。
 * <p>普通绘制使用实时字体测量，不依赖缓存宽度；调整字体或文本只作用于本次绘制，
 * 保留元素原配置。多行模式读取预先拆分好的元素，不负责自动分行。</p>
 */
public class TextPainter implements Painter<TextEmbedded> {
    /**
     * 绘制单行文字或配置好的多行文字。
     * <p>单行超宽时先缩小字号，再尝试省略；连省略号都放不下时绘制为空。
     * 多行按第一行的边界对齐，每行的纵向位置由该行元素提供。</p>
     *
     * @param g           目标画笔，绘制过程保留其原始状态
     * @param element     文字元素，文本和字体必须提供，多行模式还需提供 breakLineElements
     * @param canvasWidth 画布宽度，单位为像素，用于计算居中
     * @throws IllegalArgumentException 缺少文本、字体、多行列表，或最大宽度为负数时抛出
     */
    @Override
    public void draw(Graphics2D g, TextEmbedded element, int canvasWidth) {
        List<TextEmbedded> lines = element.isAutoBreakLine()
                ? element.getBreakLineElements() : Collections.singletonList(element);
        if (lines == null)
            throw new IllegalArgumentException("自动换行绘制前需要提供 breakLineElements");

        int firstX = 0, firstWidth = 0;
        for (int i = 0; i < lines.size(); i++) {
            TextEmbedded line = lines.get(i);
            Graphics2D lineGraphics = (Graphics2D) g.create();
            try {
                Font font = line.getFont();
                String text = line.getText();
                if (font == null || text == null)
                    throw new IllegalArgumentException("文字和字体不能为空");

                FontMetrics metrics = lineGraphics.getFontMetrics(font);
                if (!element.isAutoBreakLine() && line.getAutoFitMaxWidth() != null) {
                    int maxWidth = line.getAutoFitMaxWidth();
                    if (maxWidth < 0)
                        throw new IllegalArgumentException("文字最大宽度不能小于 0");
                    int minSize = Math.max(1, line.getAutoFitMinFontSize());

                    // 每次缩小后重新测量，不能使用元素中缓存的旧宽度。
                    while (metrics.stringWidth(text) > maxWidth && font.getSize2D() > minSize) {
                        font = font.deriveFont(Math.max(minSize, font.getSize2D() - 1));
                        metrics = lineGraphics.getFontMetrics(font);
                    }
                    if (metrics.stringWidth(text) > maxWidth) {
                        String suffix = "...";
                        // 连省略号都放不下时输出空文字，避免删到负数长度。
                        if (metrics.stringWidth(suffix) > maxWidth)
                            text = "";
                        else {
                            while (!text.isEmpty() && metrics.stringWidth(text + suffix) > maxWidth)
                                text = text.substring(0, text.offsetByCodePoints(text.length(), -1));
                            text += suffix;
                        }
                    }
                }

                int width = metrics.stringWidth(text);
                int x = line.getX();
                if (line.isCenter())
                    x = (canvasWidth - width) / 2;
                else if (i == 0 && line.getDirection() == Direction.RightLeft)
                    x -= width;
                else if (i == 0 && line.getDirection() == Direction.CenterLeftRight)
                    x -= width / 2;

                if (i == 0) {
                    firstX = x;
                    firstWidth = width;
                } else {
                    switch (element.getLineAlign()) {
                        case Left:
                            x = firstX;
                            break;
                        case Center:
                            x = firstX + (firstWidth - width) / 2;
                            break;
                        case Right:
                            x = firstX + firstWidth - width;
                            break;
                    }
                }

                int drawY = line.getDrawY() == null ? getBaseline(line, metrics) : line.getDrawY();
                lineGraphics.setFont(font);
                lineGraphics.setColor(line.getColor());
                lineGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, line.getAlpha()));
                if (line.getRotate() != null)
                    lineGraphics.rotate(Math.toRadians(line.getRotate()), x + width / 2.0, drawY);

                if (line.isStrikeThrough() && !text.isEmpty()) {
                    AttributedString attributed = new AttributedString(text);
                    attributed.addAttribute(TextAttribute.FONT, font);
                    attributed.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                    lineGraphics.drawString(attributed.getIterator(), x, drawY);
                } else
                    lineGraphics.drawString(text, x, drawY);
            } finally {
                lineGraphics.dispose();
            }
        }
    }

    /**
     * 按文字单元的宽高平铺，未指定尺寸时使用字体测量结果。
     * <p>高度依次取 height、lineHeight、字体测量高度；基线随平铺画笔移动。</p>
     *
     * @param g            目标画笔
     * @param element      文字元素，必须已设置文本和字体
     * @param canvasWidth  画布宽度，单位为像素
     * @param canvasHeight 画布高度，单位为像素
     */
    @Override
    public void drawRepeat(Graphics2D g, TextEmbedded element, int canvasWidth, int canvasHeight) {
        FontMetrics metrics = g.getFontMetrics(element.getFont());
        int width = element.getWidth() == null ? metrics.stringWidth(element.getText()) : element.getWidth();
        int height = element.getHeight() == null
                ? (element.getLineHeight() == null ? metrics.getHeight() : element.getLineHeight()) : element.getHeight();
        Painter.super.drawRepeat(g, element, canvasWidth, canvasHeight, width, height);
    }

    /**
     * 将元素 y 坐标转换成 Java2D drawString 所需的基线坐标。
     *
     * @param line    当前行文字，baseLine 决定顶部、居中、底部或直接基线定位
     * @param metrics 当前字体的测量结果
     * @return 换算后的纵向基线位置，单位为像素
     */
    private int getBaseline(TextEmbedded line, FontMetrics metrics) {
        switch (line.getBaseLine()) {
            case Base:
                return line.getY();
            case Bottom:
                return line.getY() - metrics.getDescent();
            case Middle:
                return line.getY() + (metrics.getAscent() - metrics.getDescent()) / 2;
            default:
                return line.getY() + metrics.getAscent();
        }
    }
}
