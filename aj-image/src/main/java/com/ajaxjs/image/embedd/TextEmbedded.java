package com.ajaxjs.image.embedd;

import com.ajaxjs.image.constant.enums.BaseLine;
import com.ajaxjs.image.constant.enums.LineAlign;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TextEmbedded extends EmbeddedElement {
    /**
     * 内部度量对象
     */
    private FontMetrics metrics;

    /**
     * 文本
     */
    private String text;

    /**
     * 字体
     */
    private Font font;

    /**
     * 字间距
     */
    private Float space;

    /**
     * 删除线
     */
    private boolean strikeThrough;

    /**
     * 颜色，默认黑色
     */
    private Color color = new Color(0, 0, 0);

    /**
     * 旋转
     */
    private Integer rotate;

    /**
     * 行高（根据设计稿设置具体的值，默认等于metrics.getHeight()）
     */
    private Integer lineHeight;

    /**
     * 宽度（只读，计算值）
     */
    private Integer width;

    /**
     * 高度（只读，计算值，单行时等于lineHeight，多行时等于lineHeight*行数）
     */
    private Integer height;

    /**
     * 实际绘制用的y（sketch的y与graph2d有所区别，需要换算）
     */
    private Integer drawY;

    /**
     * 自适应宽度最大宽度
     */
    private Integer autoFitMaxWidth;

    /**
     * 自适应宽度最小字号（超出截断并加省略号）
     */
    private Integer autoFitMinFontSize = 0;

    /**
     * Y轴坐标参考基线（默认文字顶部）
     */
    private BaseLine baseLine = BaseLine.Top;

    /* ---------- 换行计算相关属性 ----------- */

    /**
     * 是否自动换行
     */
    private boolean autoBreakLine = false;

    /**
     * 最大行宽，超出则换行
     */
    private int maxLineWidth = 999;

    /**
     * 最大行数，超出则丢弃
     */
    private int maxLineCount = 999;

    /**
     * 行对齐方式，默认左对齐
     */
    private LineAlign lineAlign = LineAlign.Left;

    /**
     * 换过行的元素们
     */
    private List<TextEmbedded> breakLineElements;

    /**
     * 手动指定换行符（支持正则，同string.split方法参数）
     */
    private String breakLineSplitter;
}
