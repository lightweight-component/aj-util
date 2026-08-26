package com.ajaxjs.image.embedd;

import com.ajaxjs.image.constant.enums.ZoomMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.image.BufferedImage;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageEmbedded extends EmbeddedElement {
    /**
     * 图片对象
     */
    private BufferedImage image;

    /**
     * 图片地址
     */
    private String imgUrl;

    /**
     * 绘制宽度
     */
    private Integer width;

    /**
     * 绘制高度
     */
    private Integer height;

    /**
     * 圆角大小
     */
    private Integer roundCorner;

    /**
     * 缩放模式
     */
    private ZoomMode zoomMode;

    /**
     * 旋转角度
     */
    private Integer rotate;

    /**
     * 高斯模糊（毛玻璃）
     */
    private Integer blur;
}
