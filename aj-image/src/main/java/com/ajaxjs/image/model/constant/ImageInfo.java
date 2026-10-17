package com.ajaxjs.image.model.constant;

import lombok.Data;

/**
 * 图片头部读取到的格式和尺寸信息。
 * <p>由 ImageTypeHandler 在解码像素前填充；MIME 类型根据后缀推测，并非内容安全校验。</p>
 */
@Data
public class ImageInfo {
    /**
     * 图片原始宽度，单位为像素，不是区域解码后的宽度。
     */
    Integer width;

    /**
     * 图片原始高度，单位为像素，不是区域解码后的高度。
     */
    Integer height;

    /**
     * 读取器报告的图片格式名，已转换为小写；不一定与传入的别名完全相同。
     */
    String suffix;

    /**
     * 根据传入后缀推测的 MIME 类型，例如 image/png；无法识别时可能为 null。
     */
    String mime;
}
