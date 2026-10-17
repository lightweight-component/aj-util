package com.ajaxjs.image.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

/**
 * 图片的基础效果处理器，包括缩放、缩略图、圆角和扩展画布。
 * <p>方法将结果保存到 output 并返回当前实例，可通过 getOutput 或输出方法取得结果。
 * 各处理方法读取 source，连续调用不等于把上一步结果作为下一步输入；
 * 需要叠加效果时，可用上一步 getOutput 创建新的处理器。</p>
 */
public class ImageEffect extends ImageBase {
    /**
     * 直接使用内存图片创建处理器。
     *
     * @param source 已读取的图片，不应为 null；不会自动复制像素
     */
    public ImageEffect(BufferedImage source) {
        super(source);
    }

    /**
     * 从图片文件字节创建处理器，ImageIO 根据文件头识别编码格式。
     *
     * @param imageBytes PNG、JPEG 等完整编码数据，不是原始像素矩阵
     * @throws java.io.UncheckedIOException 解码发生 I/O 错误时抛出
     */
    public ImageEffect(byte[] imageBytes) {
        super(imageBytes);
    }

    /**
     * 从本地图片文件创建处理器。
     *
     * @param file 图片文件，须存在且具有受支持的编码格式
     * @throws IllegalArgumentException     无法识别图片格式时抛出
     * @throws java.io.UncheckedIOException 文件读取失败时抛出
     */
    public ImageEffect(File file) {
        super(file);
    }

    /**
     * 从本地路径或以 http 开头的网络地址创建处理器。
     * <p>网络读取使用 ImageIO 的默认连接行为；本构造方法不提供超时或身份认证参数。</p>
     *
     * @param file 本地文件路径或 HTTP/HTTPS 图片地址
     * @throws RuntimeException 图片读取失败时抛出
     */
    public ImageEffect(String file) {
        super(file);
    }

    /**
     * 按指定倍数缩放源图，保留源图是否具有透明通道的特性。
     * <p>宽高四舍五入到整数像素；比例过小导致某一边变为零时，底层图片 API 会拒绝该尺寸。</p>
     *
     * @param scale 缩放倍数，例如 0.5 为缩小一半，2 为放大两倍；必须为正的有限数
     * @return 当前处理器，output 为缩放后的新图片
     * @throws IllegalArgumentException 比例非法或结果尺寸无效时抛出
     */
    public ImageEffect scale(double scale) {
        if (!Double.isFinite(scale) || scale <= 0)
            throw new IllegalArgumentException("scale must > 0");

        int width = source.getWidth();
        int height = source.getHeight();
        Image image = source.getScaledInstance((int) Math.round(width * scale), (int) Math.round(height * scale), Image.SCALE_SMOOTH);

        output = copy(image, source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_3BYTE_BGR);

        return this;
    }

    /**
     * 按目标宽高调整源图尺寸，可选择保持原始宽高比。
     * <p>constrain 为 true 时，结果放入指定矩形内并尽量占满；不裁剪。
     * 为 false 时直接使用目标宽高，可能拉伸图片。结果保持源像素类型或其兼容颜色模型，
     * 包括索引图片的调色板。等比例缩放后每一边至少保留一个像素。</p>
     *
     * @param targetWidth  目标宽度或宽度上限，单位为像素，应大于零
     * @param targetHeight 目标高度或高度上限，单位为像素，应大于零
     * @param constrain    是否保持宽高比
     * @return 当前处理器，output 为调整后的新图片
     * @throws IllegalArgumentException 目标或计算结果尺寸不合法时抛出
     */
    public ImageEffect resize(int targetWidth, int targetHeight, boolean constrain) {
        if (targetWidth <= 0 || targetHeight <= 0)
            throw new IllegalArgumentException("Target dimensions must be positive");

        if (constrain) {
            double aspectRatio = (double) source.getWidth() / source.getHeight();
            double sx = (double) targetWidth / source.getWidth();
            double sy = (double) targetHeight / source.getHeight();

            if (sx > sy)
                targetWidth = Math.max(1, (int) Math.round(targetHeight * aspectRatio));
            else
                targetHeight = Math.max(1, (int) Math.round(targetWidth / aspectRatio));
        }

        int type = source.getType();

        // 索引图片的颜色由调色板决定，只复制类型会换成默认调色板，导致颜色改变。
        if (type == BufferedImage.TYPE_CUSTOM || source.getColorModel() instanceof java.awt.image.IndexColorModel) {
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetWidth, targetHeight);
            output = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
        } else
            output = new BufferedImage(targetWidth, targetHeight, type);

        Graphics2D g = output.createGraphics();

        try {
            // 直接覆盖目标像素，透明像素也要写入，不能与调色板默认颜色混合。
            g.setComposite(AlphaComposite.Src);
            g.drawImage(source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        } finally {
            g.dispose();
        }

        return this;
    }

    /**
     * 生成不超过目标大小的缩略图，不放大原图。
     * <p>原图两边都小于目标时返回像素副本。其他情况先判断原图长边与短边之比：
     * 达到 ratioThreshold 时按目标比例居中裁剪，否则直接等比例缩小。
     * 结果可能小于请求尺寸，不会修改 source 指向的图片。</p>
     *
     * @param thumbnailWidth  缩略图宽度上限，单位为像素，必须大于零
     * @param thumbnailHeight 缩略图高度上限，单位为像素，必须大于零
     * @param ratioThreshold  启用居中裁剪的长短边比阈值，必须为正的有限数
     * @return 当前处理器，output 为缩略图
     * @throws IllegalArgumentException 尺寸或比例阈值非法时抛出
     */
    public ImageEffect thumbnail(int thumbnailWidth, int thumbnailHeight, double ratioThreshold) {
        if (thumbnailWidth <= 0 || thumbnailHeight <= 0 || !Double.isFinite(ratioThreshold) || ratioThreshold <= 0)
            throw new IllegalArgumentException("thumbnail dimensions and ratioThreshold must be positive");

        int w = source.getWidth(), h = source.getHeight();

        if (w < thumbnailWidth && h < thumbnailHeight) {
            output = copy(source, source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

            return this;
        }

        BufferedImage cropped = source;
        double sourceRatio = (double) w / h;
        double targetRatio = (double) thumbnailWidth / thumbnailHeight;

        if (Math.max(sourceRatio, 1 / sourceRatio) >= ratioThreshold) {
            // 裁剪哪条边由目标宽高比决定，不能只看原图是横图还是竖图。
            int cropWidth = w, cropHeight = h;

            if (sourceRatio > targetRatio)
                cropWidth = Math.max(1, Math.min(w, (int) Math.round(h * targetRatio)));
            else
                cropHeight = Math.max(1, Math.min(h, (int) Math.round(w / targetRatio)));

            cropped = source.getSubimage((w - cropWidth) / 2, (h - cropHeight) / 2, cropWidth, cropHeight);
        }

        // 缩略图只缩小，不放大；尺寸始终限制在请求的矩形之内。
        double factor = Math.min(1, Math.min((double) thumbnailWidth / cropped.getWidth(),
                (double) thumbnailHeight / cropped.getHeight()));
        output = new ImageEffect(cropped).resize(
                Math.max(1, (int) Math.round(cropped.getWidth() * factor)),
                Math.max(1, (int) Math.round(cropped.getHeight() * factor)), false).getOutput();

        return this;
    }

    /**
     * 将源图绘制到透明圆角遮罩内，结果使用 ARGB 像素格式。
     * <p>宽高同时为 0 时沿用源图尺寸；指定宽高时会将源图缩放到该尺寸。</p>
     *
     * @param width  结果宽度，单位为像素；与 height 同时为 0 时使用原宽度
     * @param height 结果高度，单位为像素；与 width 同时为 0 时使用原高度
     * @param radius 圆角矩形的弧宽和弧高，单位为像素，并非几何半径
     * @return 当前处理器，output 为圆角外部透明的新图片
     */
    public ImageEffect roundCorner(int width, int height, int radius) {
        if (width == 0 && height == 0) {
            width = source.getWidth();
            height = source.getHeight();
        }

        output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fillRoundRect(0, 0, width, height, radius, radius);
            g.setComposite(AlphaComposite.SrcIn);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }

        return this;
    }

    /**
     * 按源图尺寸生成圆角图片，保留透明的圆角外部区域。
     *
     * @param radius 圆角弧宽和弧高，单位为像素
     * @return 当前处理器，output 为圆角图片
     */
    public ImageEffect roundCorner(int radius) {
        return roundCorner(0, 0, radius);
    }

    /**
     * 向图片四周扩展画布，在指定左、上偏移处绘制源图。
     * <p>扩展区域填充黑色；即使目标像素类型支持透明度，也不会自动将扩展区域设为透明。</p>
     *
     * @param imageType 新画布的 BufferedImage 像素类型常量
     * @param left      左侧增加的像素数，不能为负
     * @param top       顶部增加的像素数，不能为负
     * @param right     右侧增加的像素数，不能为负
     * @param bottom    底部增加的像素数，不能为负
     * @return 当前处理器，output 为扩展画布后的图片
     * @throws IllegalArgumentException 任一扩展值为负数时抛出
     */
    public ImageEffect growCanvas(int imageType, int left, int top, int right, int bottom) {
        if (left < 0 || top < 0 || right < 0 || bottom < 0)
            throw new IllegalArgumentException("left,top,right,bottom must >=0");

        output = new BufferedImage(source.getWidth() + left + right, source.getHeight() + top + bottom, imageType);
        Graphics g = output.getGraphics();

        try {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, output.getWidth(), output.getHeight());
            g.drawImage(source, left, top, null);

            return this;
        } finally {
            g.dispose();
        }
    }

    /**
     * 使用不透明三字节 BGR 画布扩展源图，新增区域为黑色。
     *
     * @param left   左侧增加的像素数，不能为负
     * @param top    顶部增加的像素数，不能为负
     * @param right  右侧增加的像素数，不能为负
     * @param bottom 底部增加的像素数，不能为负
     * @return 当前处理器，output 为扩展后的图片
     */
    public ImageEffect growCanvas(int left, int top, int right, int bottom) {
        return growCanvas(BufferedImage.TYPE_3BYTE_BGR, left, top, right, bottom);
    }

    /**
     * 仅向右侧或底部补黑边，使画布成为正方形。
     * <p>正方形边长等于原图较长的一边，原图放在左上角；结果不保留透明通道。</p>
     *
     * @return 当前处理器，output 为正方形画布图片
     */
    public ImageEffect growSquareCanvas() {
        int width = source.getWidth(), height = source.getHeight();
        int size = Math.max(width, height);

        return growCanvas(0, 0, size - width, size - height);
    }
}
