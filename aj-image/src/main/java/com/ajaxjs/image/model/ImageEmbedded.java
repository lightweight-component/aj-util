package com.ajaxjs.image.model;

import com.ajaxjs.image.model.constant.ZoomMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.image.BufferedImage;

/**
 * 图片元素的绘制配置。
 * <p>需要提供已读取的图片和缩放模式；Origin 使用原始尺寸，其他模式按需要设置宽或高。
 * 本类只保存配置，不负责从地址下载图片。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageEmbedded extends EmbeddedElement {
    /**
     * 要绘制的内存图片，绘制前必须提供。
     * <p>保存原对象引用；内置画笔在副本上处理模糊，不会改写输入像素。</p>
     */
    private BufferedImage image;

    /**
     * 图片地址的预留配置；当前 ImagePainter 不读取此字段，也不会自动加载地址。
     */
    private String imgUrl;

    /**
     * 目标绘制宽度，单位为像素；Width、WidthHeight 模式需要设置，Origin 模式不使用。
     */
    private Integer width;

    /**
     * 目标绘制高度，单位为像素；Height、WidthHeight 模式需要设置，Origin 模式不使用。
     */
    private Integer height;

    /**
     * 图片圆角的弧宽和弧高，单位为像素；{@code null} 表示不处理圆角。
     */
    private Integer roundCorner;

    /**
     * 图片缩放模式；没有默认值，绘制前必须设置。
     */
    private ZoomMode zoomMode;

    /**
     * 绕实际绘制区域中心旋转的角度，单位为度；{@code null} 表示不旋转。
     * <p>在屏幕坐标中，正角度呈顺时针旋转。</p>
     */
    private Integer rotate;

    /**
     * 模糊处理半径；{@code null} 表示不启用，小于 1 时不处理，数值越大模糊范围越大。
     */
    private Integer blur;
}
