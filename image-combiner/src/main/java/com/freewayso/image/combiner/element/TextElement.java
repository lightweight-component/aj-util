package com.freewayso.image.combiner.element;

import com.ajaxjs.image.constant.enums.BaseLine;
import com.ajaxjs.image.constant.enums.LineAlign;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * 文本元素
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TextElement extends CombineElement<TextElement> {
    private static Map<String, Font> fontRegisterMap = new HashMap<>();

    //内部度量对象
    private FontMetrics metrics;

    //基础属性
    private String text;                    //文本
    private Font font;                      //字体
    private Float space;                    //字间距
    private boolean strikeThrough;          //删除线
    private Color color = new Color(0, 0, 0);   //颜色，默认黑色
    private Integer rotate;                 //旋转
    private Integer lineHeight;             //行高（根据设计稿设置具体的值，默认等于metrics.getHeight()）
    private Integer width;                  //宽度（只读，计算值）
    private Integer height;                 //高度（只读，计算值，单行时等于lineHeight，多行时等于lineHeight*行数）
    private Integer drawY;                  //实际绘制用的y（sketch的y与graph2d有所区别，需要换算）
    private Integer autoFitMaxWidth;        //自适应宽度最大宽度
    private Integer autoFitMinFontSize = 0;   //自适应宽度最小字号（超出截断并加省略号）
    private BaseLine baseLine = BaseLine.Top;       //Y轴坐标参考基线（默认文字顶部）

    // 换行计算相关属性
    private boolean autoBreakLine = false;          //是否自动换行
    private int maxLineWidth = 999;                 //最大行宽，超出则换行
    private int maxLineCount = 999;                 //最大行数，超出则丢弃
    private LineAlign lineAlign = LineAlign.Left;   //行对齐方式，默认左对齐
    private List<TextElement> breakLineElements;    //换过行的元素们
    private String breakLineSplitter;               //手动指定换行符（支持正则，同string.split方法参数）

    /**
     * @param text 文本内容
     * @param font Font对象
     * @param x    x坐标
     * @param y    y坐标
     */
    public TextElement(String text, Font font, int x, int y) {
        this.text = text;
        this.font = font;
        super.setX(x);
        super.setY(y);
    }

    /**
     * @param text     文本内容
     * @param fontSize 字号
     * @param x        x坐标
     * @param y        y坐标
     */
    public TextElement(String text, int fontSize, int x, int y) {
        this(text, new Font("阿里巴巴普惠体", Font.PLAIN, fontSize), x, y);
    }

    /**
     * @param text      文本内容
     * @param fontStyle 字体样式
     * @param fontSize  字号
     * @param x         x坐标
     * @param y         y坐标
     */
    public TextElement(String text, int fontStyle, int fontSize, int x, int y) {
        this(text, new Font("阿里巴巴普惠体", fontStyle, fontSize), x, y);
    }

    /**
     * @param text           文本内容
     * @param fontNameOrPath 字体名称
     * @param fontSize       字号
     * @param x              x坐标
     * @param y              y坐标
     */
    public TextElement(String text, String fontNameOrPath, int fontSize, int x, int y) {
        this(text, fontNameOrPath, Font.PLAIN, fontSize, x, y);
    }

    /**
     * @param text           文本内容
     * @param fontNameOrPath 字体名称
     * @param fontStyle      字体样式
     * @param fontSize       字号
     * @param x              x坐标
     * @param y              y坐标
     */
    public TextElement(String text, String fontNameOrPath, int fontStyle, int fontSize, int x, int y) {
        Font font = isFontFile(fontNameOrPath) ? loadFont(fontNameOrPath, fontStyle, fontSize) : new Font(fontNameOrPath, fontStyle, fontSize);
        this.text = text;
        this.font = font;
        super.setX(x);
        super.setY(y);
    }

    /************* 计算属性 *************/
    public Integer getWidth() {
        if (width == null) {
            if (autoBreakLine) {
                Integer maxElementWidth = 0;

                for (TextElement textElement : getBreakLineElements()) {
                    Integer elementWidth = textElement.getWidth();
                    maxElementWidth = elementWidth > maxElementWidth ? elementWidth : maxElementWidth;
                }

                width = maxElementWidth;
            } else
                width = getMetrics().stringWidth(text);
        }

        return width;
    }

    public Integer getHeight() {
        if (height == null) {
            if (autoBreakLine)
                height = getLineHeight() * getBreakLineElements().size();     //如果自动换行，则计算多行高度
            else
                height = getLineHeight();
        }

        return height;
    }

    public Integer getDrawY() {
        int middleY = getY() - getMetrics().getDescent() + getMetrics().getHeight() / 2;
        if (baseLine == BaseLine.Top) {
            if (drawY == null)
                //drawY = getY() + (getLineHeight() - getMetrics().getHeight()) / 2 + getMetrics().getAscent();
                drawY = middleY + getLineHeight() / 2;

        } else if (baseLine == BaseLine.Middle)
            drawY = middleY;
        else if (baseLine == BaseLine.Bottom) {
            //drawY = getY() - (getLineHeight() - getMetrics().getHeight()) / 2 - getMetrics().getDescent();
            drawY = middleY - getLineHeight() / 2;
        } else
            drawY = getY();

        return drawY;
    }

    public List<TextElement> getBreakLineElements() {
        if (breakLineElements == null)
            breakLineElements = computeBreakLineElements();

        return breakLineElements;
    }

    /**
     * 设置Y坐标
     */
    @Override
    public TextElement setY(int y) {
        //textElement的setY会影响drawY的计算，所以要复写基类方法，重置一下计算属性
        this.resetProperties();
        return super.setY(y);
    }

    /**
     * 设置文本
     */
    public TextElement setText(String text) {
        this.resetProperties();
        this.text = text;
        return this;
    }

    /**
     * 设置字体
     */
    public TextElement setFont(Font font) {
        this.resetProperties();
        this.font = font;
        return this;
    }

    /**
     * 设置字间距
     */
    public TextElement setSpace(Float space) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, space);
        Font font2 = font.deriveFont(attributes);
        setFont(font2);

        return this;
    }

    /**
     * 设置旋转角度（0~360）
     *
     * @param rotate
     * @return
     */
    public TextElement setRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }

    /**
     * 设置文字颜色
     *
     * @param color
     * @return
     */
    public TextElement setColor(Color color) {
        this.color = color;
        return this;
    }

    public TextElement setColor(int r, int g, int b) {
        return setColor(new Color(r, g, b));
    }

    public Integer getLineHeight() {
        if (lineHeight == null)
            lineHeight = getMetrics().getHeight();      //未设置lineHeight则默认取文本高度

        return lineHeight;
    }

    /**
     * 设置行高
     *
     * @param lineHeight
     * @return
     */
    public TextElement setLineHeight(Integer lineHeight) {
        this.resetProperties();
        this.lineHeight = lineHeight;
        return this;
    }

    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    /**
     * 设置删除线
     *
     * @param strikeThrough
     * @return
     */
    public TextElement setStrikeThrough(boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        return this;
    }

    /**
     * 设置自动换行
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param lineAlign    行对齐方式
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.lineAlign = lineAlign;
        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;

        return this;
    }

    /**
     * 设置自动换行
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @param lineAlign    行对齐方式
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineAlign = lineAlign;

        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @param lineHeight   行高
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, int lineHeight) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;

        return this;
    }

    /**
     * 设置自动换行
     *
     * @param maxLineWidth 最大宽度（超出则换行）
     * @param maxLineCount 最大行数（超出则丢弃）
     * @param lineHeight   行高
     * @param lineAlign    行对齐方式
     * @return
     */
    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, int lineHeight, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;
        this.lineAlign = lineAlign;

        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param splitter 指定换行符
     * @return
     */
    public TextElement setAutoBreakLine(String splitter) {
        this.autoBreakLine = true;
        this.breakLineSplitter = splitter;
        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param splitter   指定换行符
     * @param lineHeight 行高
     * @return
     */
    public TextElement setAutoBreakLine(String splitter, int lineHeight) {
        this.autoBreakLine = true;
        this.breakLineSplitter = splitter;
        this.lineHeight = lineHeight;
        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param splitter  指定换行符
     * @param lineAlign 行对齐方式
     * @return
     */
    public TextElement setAutoBreakLine(String splitter, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.breakLineSplitter = splitter;
        this.lineAlign = lineAlign;
        return this;
    }

    /**
     * 设置自动换行（默认左对齐）
     *
     * @param splitter   指定换行符
     * @param lineHeight 行高
     * @param lineAlign  行对齐方式
     * @return
     */
    public TextElement setAutoBreakLine(String splitter, int lineHeight, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.breakLineSplitter = splitter;
        this.lineHeight = lineHeight;
        this.lineAlign = lineAlign;
        return this;
    }

    /**
     * 设置自适应宽度（超出宽度则缩小字体以适应之）
     *
     * @param autoFitMaxWidth
     * @return
     */
    public TextElement setAutoFitWidth(Integer autoFitMaxWidth) {
        this.autoFitMaxWidth = autoFitMaxWidth;
        return this;
    }

    /**
     * 设置自适应宽度（超出宽度则缩小字体以适应之，达到最小字号则截断）
     *
     * @param autoFitMaxWidth
     * @return
     */
    public TextElement setAutoFitWidth(Integer autoFitMaxWidth, Integer autoFixMinFontSize) {
        this.autoFitMaxWidth = autoFitMaxWidth;
        this.autoFitMinFontSize = autoFixMinFontSize;
        return this;
    }

    /**
     * 设置坐标参考基线（默认左上角为00点）
     *
     * @param baseLine
     * @return
     */
    public TextElement setBaseLine(BaseLine baseLine) {
        this.baseLine = baseLine;
        return this;
    }

    private void resetProperties() {
        // 如果设置了影响布局的字段，需要重置这几个计算值（主要为提高性能，计算字段如未受影响，则只计算一次，相当于缓存，有变动再重置重算）
        this.width = null;
        this.height = null;
        this.drawY = null;
        this.breakLineElements = null;
        this.metrics = null;
    }

    private FontMetrics getMetrics() {
        if (metrics == null) {
            metrics = new Canvas().getFontMetrics(font);
        }
        return metrics;
    }

    private List<TextElement> computeBreakLineElements() {
        java.util.List<TextElement> breakLineElements = new ArrayList<>();
        List<String> breakLineTexts = computeLines(text);
        int currentY = getY();

        for (int i = 0; i < breakLineTexts.size(); i++) {
            if (i < maxLineCount) {
                String text = breakLineTexts.get(i);
                // 如果计该行是要取的最后一行，但不是整体最后一行，则加...
                if (i == maxLineCount - 1 && i < breakLineTexts.size() - 1)
                    text = text.substring(0, text.length() - 1) + "...";

                TextElement textLineElement = new TextElement(text, font, getX(), currentY);
                textLineElement.setColor(color);
                textLineElement.setStrikeThrough(strikeThrough);
                textLineElement.setCenter(isCenter());
                textLineElement.setAlpha(getAlpha());
                textLineElement.setRotate(rotate);
                textLineElement.setLineHeight(getLineHeight());
                textLineElement.setDirection(getDirection());
                textLineElement.setBaseLine(getBaseLine());
                breakLineElements.add(textLineElement);

                currentY += getLineHeight();
            } else
                break;
        }

        return breakLineElements;
    }

    private List<String> computeLines(String text) {
        List<String> computedLines = new ArrayList<>();     // 最终要返回的多行文本（不超限定宽度）

        if (breakLineSplitter != null && !breakLineSplitter.equals(""))// 手动指定换行符时，直接按换行符分隔返回
            return Arrays.asList(text.split(breakLineSplitter));

        String strToComputer = "";
        String word = "";             // 一个完整单词
        boolean hasWord = false;      // 是否获得一个完整单词
        char[] chars = text.toCharArray();
        int count = 0;

        // 遍历每个字符，拆解单词（一个中文算一个单词，其他字符直到碰到空格算一个单词）
        for (int i = 0; i < chars.length; i++) {
            if (count++ > 2000)
                break;      // 防止意外情况进入死循环

            char c = chars[i];          // 当前字符

            if (isChineseChar(c) || c == ' ' || i == (chars.length - 1)) {
                word += c;             //如果是中文或空格或最后一个字符，一个中文算一个单词, 其他字符遇到空格认为单词结束
                hasWord = true;
            } else
                word += c;             //英文或其他字符，加入word，待组成单词

            if (hasWord) { // 获得了一个完整单词，加入当前行，并计算限宽
                int originWidth = getMetrics().stringWidth(strToComputer);  //计算现有文字宽度
                int wordWidth = getMetrics().stringWidth(word); //计算单个单词宽度（防止一个单词就超限宽的情况）
                strToComputer += word;//单词加入待计算字符串
                int newWidth = originWidth + wordWidth;    //加入了新单词之后的宽度

                if (wordWidth > maxLineWidth) { // 一个单词就超限，要暴力换行
                    //按比例计算要取几个字符（不是特别精准）
                    int fetch = (int) ((float) (maxLineWidth - originWidth) / (float) wordWidth * word.length());   //本行剩余宽度所占word宽度比例，乘以字符长度（字符不等宽的时候不太准）
                    fetch = fetch == 0 ? 1 : fetch;                                                                 //至少取一个
                    strToComputer = strToComputer.substring(0, strToComputer.length() - word.length() + fetch);     //去除最后的word的后半截
                    computedLines.add(strToComputer);                      // 加入计算结果列表
                    strToComputer = "";
                    i -= (word.length() - fetch);                          // 遍历计数器回退word.length()-fetch个
                }
                //行宽度超出限宽，则去除最后word，加入计算结果列表
                else if (newWidth > maxLineWidth) {
                    strToComputer = strToComputer.substring(0, strToComputer.length() - word.length());      //去除最后word
                    computedLines.add(strToComputer);                       // 加入计算结果列表
                    strToComputer = "";
                    i -= word.length();                                     // 遍历计数器回退word.length()个
                }

                word = "";
                hasWord = false;        // 重置标记
            }
        }

        if (!strToComputer.equals(""))
            computedLines.add(strToComputer);   // 加入计算结果列表

        return computedLines;
    }

    private boolean isChineseChar(char c) {
        boolean isChar = String.valueOf(c).matches("[\u4e00-\u9fa5]");
        return isChar || c == '，' || c == '。' || c == '、' || c == '；' || c == '！' || c == '？' || c == '：' || c == '“' || c == '”' || c == '【' || c == '】' || c == '《' || c == '》' || c == '（' || c == '）';
    }

    private Font loadFont(String path, int fontStyle, int fontSize) {
        Font font = fontRegisterMap.get(path);

        if (font == null) {
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream(path)));
                // 如果不需要通过字体名调用字体，可以不用注册，注册字体还是有点慢的
                //GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                //ge.registerFont(font);
                fontRegisterMap.put(path, font);
            } catch (FontFormatException | IOException e) {
                e.printStackTrace();
            }
        }

        assert font != null;
        return font.deriveFont(fontStyle, fontSize);
    }

    private boolean isFontFile(String fontNameOrPath) {
        return fontNameOrPath.endsWith(".ttf")
                || fontNameOrPath.endsWith(".otf")
                || fontNameOrPath.endsWith(".ttc")
                || fontNameOrPath.endsWith(".woff")
                || fontNameOrPath.endsWith(".woff2")
                || fontNameOrPath.endsWith(".eot");
    }
}
