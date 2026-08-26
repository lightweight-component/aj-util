package com.ajaxjs.image.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

public class ImageEffect extends ImageBase {
    public ImageEffect(BufferedImage source) {
        super(source);
    }

    public ImageEffect(byte[] imageBytes) {
        super(imageBytes);
    }

    public ImageEffect(File file) {
        super(file);
    }

    public ImageEffect(String file) {
        super(file);
    }

    /**
     * 对原图缩放，返回缩放后的新对象
     *
     * @param scale 缩放比例
     */
    public ImageEffect scale(double scale) {
        if (scale < 0)
            throw new IllegalArgumentException("scale must > 0");

        int width = source.getWidth();
        int height = source.getHeight();
        Image image = source.getScaledInstance((int) Math.round(width * scale), (int) Math.round(height * scale), Image.SCALE_SMOOTH);

        output = copy(image, BufferedImage.TYPE_3BYTE_BGR);

        return this;
    }

    /**
     * 对图像进行缩放
     *
     * @param targetWidth  缩放后图像宽度
     * @param targetHeight 缩放后图像高度
     * @param constrain    为true时等比例缩放，targetWidth,targetHeight为缩放图像的限制尺寸
     */
    public ImageEffect resize(int targetWidth, int targetHeight, boolean constrain) {
        if (constrain) {
            double aspectRatio = (double) source.getWidth() / source.getHeight();
            double sx = (double) targetWidth / source.getWidth();
            double sy = (double) targetHeight / source.getHeight();

            if (sx > sy)
                targetWidth = (int) Math.round(targetHeight * aspectRatio);
            else
                targetHeight = (int) Math.round(targetWidth / aspectRatio);
        }

        int type = source.getType();

        if (type == BufferedImage.TYPE_CUSTOM) {
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetWidth, targetHeight);
            output = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
        } else
            output = new BufferedImage(targetWidth, targetHeight, type);

        Graphics2D g = output.createGraphics();

        try {
            g.drawImage(source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        } finally {
            g.dispose();
        }

        return this;
    }

    /**
     * 对原图创建缩略图对象<br>
     * 如果原图尺寸小于指定的缩略图尺寸则直接返回原图对象的副本
     *
     * @param thumbnailWidth  缩略图宽度
     * @param thumbnailHeight 缩略图高度
     * @param ratioThreshold  最大宽高比阀值(宽高中较大的值/较小的值)，<此值时对原图等比例缩放，>=此值时从原图切出中间部分图像再等比例缩放
     */
    public ImageEffect thumbnail(int thumbnailWidth, int thumbnailHeight, double ratioThreshold) {
        int w = source.getWidth();
        int h = source.getHeight();

        if (w < thumbnailWidth && h < thumbnailHeight) { // 返回原图的副本
            output = source.getSubimage(0, 0, w, h);

            return this;
        }

        double thumbAspectRatio = (double) thumbnailWidth / thumbnailHeight;
        double wh_sca = w > h ? (double) w / h : (double) h / w;

        if (wh_sca >= ratioThreshold) {
            if (w > h) {
                int fw = (int) (thumbAspectRatio * h);

                if (h <= thumbnailHeight) {
                    output = source.getSubimage((w - fw) / 2, 0, fw, h);
                    return this;
                } else {
                    source = source.getSubimage((w - fw) / 2, 0, fw, h);
                    return resize(thumbnailWidth, thumbnailHeight, true);
                }
            } else {
                int fh = (int) (thumbAspectRatio * w);

                if (w <= thumbnailWidth) {
                    output = source.getSubimage(0, (h - fh) / 2, w, fh);
                    return this;
                } else {
                    source = source.getSubimage(0, (h - fh) / 2, w, fh);
                    return resize(thumbnailWidth, thumbnailHeight, true);
                }
            }
        } else
            return resize(thumbnailWidth, thumbnailHeight, true);
    }

    /**
     * Create a round corner image
     *
     * @param width  the width
     * @param height the height
     * @param radius the radius
     * @return the round corner image
     */
    public ImageEffect roundCorner(int width, int height, int radius) {
        if (width == 0 && height == 0) {
            width = source.getWidth();
            height = source.getHeight();
        }

        output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();

        return this;
    }

    public ImageEffect roundCorner(int radius) {
        return roundCorner(0, 0, radius);
    }

    /**
     * 将图像上下左右扩充指定的尺寸
     *
     * @param imageType 图片类型
     * @return 返回扩展尺寸后的新对象
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
     * 将图像上下左右扩充指定的尺寸
     *
     * @return 返回扩展尺寸后的新对象
     */
    public ImageEffect growCanvas(int left, int top, int right, int bottom) {
        return growCanvas(BufferedImage.TYPE_3BYTE_BGR, left, top, right, bottom);
    }

    /**
     * 将{@link Image}图像(向右下)扩充为正文形(尺寸长宽最大边)
     *
     * @return 扩充后的图片
     */
    public ImageEffect growSquareCanvas() {
        int width = source.getWidth(), height = source.getHeight();
        int size = Math.max(width, height);

        return growCanvas(0, 0, size - width, size - height);
    }
}
