package com.ajaxjs.image.model;

import com.ajaxjs.image.model.constant.BaseLine;
import com.ajaxjs.image.model.constant.LineAlign;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.util.List;

/**
 * 文字元素的内容、样式和布局配置。
 * <p>绘制前必须提供文本和字体。普通绘制会根据当前画笔重新测量文字，
 * 不修改原始文本、字体和坐标；超宽时可缩小字号或添加省略号。</p>
 * <p>当前多行绘制使用调用方事先生成的 breakLineElements，
 * 不会自动根据最大行宽或分隔符拆分文本。</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TextEmbedded extends EmbeddedElement {
    /**
     * 调用方可保存的字体测量结果；当前 TextPainter 会自行测量，不使用此缓存字段。
     */
    private FontMetrics metrics;

    /**
     * 要绘制的文字，不能为空引用；允许空字符串，空字符串不产生可见文字。
     */
    private String text;

    /**
     * 文字字体和字号，绘制前必须设置；字号以 Java2D 字体单位为准。
     */
    private Font font;

    /**
     * 字符间距预留配置；当前 TextPainter 未应用此字段。
     */
    private Float space;

    /**
     * 是否绘制删除线，默认 false；启用时在线条中部绘制横线。
     */
    private boolean strikeThrough;

    /**
     * 文字颜色，默认黑色；还会叠加公共 alpha 属性控制的透明度。
     */
    private Color color = new Color(0, 0, 0);

    /**
     * 绕文字水平中心和基线位置旋转的角度，单位为度；null 表示不旋转。
     */
    private Integer rotate;

    /**
     * 平铺时使用的行高，单位为像素。
     * <p>当 height 未设置时使用此值；两者都未设置时使用字体测量高度。普通绘制不据此自动排版。</p>
     */
    private Integer lineHeight;

    /**
     * 可选的平铺单元宽度，单位为像素；null 时按当前文本测量。
     * <p>普通文字对齐和缩小判断使用实时测量宽度，不使用此字段。</p>
     */
    private Integer width;

    /**
     * 可选的平铺单元高度，单位为像素；null 时依次使用 lineHeight 或字体测量高度。
     */
    private Integer height;

    /**
     * 明确指定的文字基线纵坐标，单位为像素；优先于 y 和 baseLine。
     * <p>null 时由 y、baseLine 和字体测量结果计算，平铺时基线跟随画笔一起移动。</p>
     */
    private Integer drawY;

    /**
     * 单行文字允许的最大宽度，单位为像素；null 表示关闭自动适应。
     * <p>超宽时先缩小字体，再添加省略号；非负值有效，多行绘制时忽略此设置。</p>
     */
    private Integer autoFitMaxWidth;

    /**
     * 自动缩小的最小字号，默认 0；绘制时将小于 1 的值按 1 处理。
     * <p>不会为了满足此下限主动放大原字体；缩到下限仍超宽时进行省略。</p>
     */
    private Integer autoFitMinFontSize = 0;

    /**
     * y 坐标对应的文字定位方式，默认顶部 Top；明确指定 drawY 时忽略此项。
     */
    private BaseLine baseLine = BaseLine.Top;

    /* ---------- 换行计算相关属性 ----------- */

    /**
     * 是否使用预先拆好的多行元素，默认 false。
     * <p>设置为 true 时必须提供 breakLineElements；开关本身不会执行自动拆行。</p>
     */
    private boolean autoBreakLine = false;

    /**
     * 最大行宽的预留配置，默认 999 像素；当前 Painter 不自动使用它拆行。
     */
    private int maxLineWidth = 999;

    /**
     * 最大行数的预留配置，默认 999；当前 Painter 不自动限制行数。
     */
    private int maxLineCount = 999;

    /**
     * 多行相对第一行的水平对齐方式，默认左对齐。
     */
    private LineAlign lineAlign = LineAlign.Left;

    /**
     * 按绘制顺序排列的多行文字元素；多行模式下必须提供。
     * <p>每行需有自己的文本、字体和纵向位置；列表为空时不绘制文字。</p>
     */
    private List<TextEmbedded> breakLineElements;

    /**
     * 拆行分隔符的预留配置，设计上采用 String.split 的正则语义；当前 Painter 不读取此字段。
     */
    private String breakLineSplitter;
}
