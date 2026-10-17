package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.constant.Direction;
import com.ajaxjs.image.utils.ImageEffect;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片元素画笔，支持缩放、圆角、模糊、透明度、旋转和平铺。
 * <p>实际绘制尺寸由缩放模式确定。旋转绕该尺寸的中心进行，
 * 不会修改元素坐标或输入图片像素；模糊在独立副本上计算。</p>
 */
public class ImagePainter implements Painter<ImageEmbedded> {
    /**
     * 将图片按配置绘制一次，依次处理圆角、模糊和定位。
     * <p>在独立画笔上下文中设置旋转与透明度，不改变调用方的画笔状态。</p>
     *
     * @param g           目标画笔
     * @param element     图片元素，必须设置 image、zoomMode 及该模式所需的宽高
     * @param canvasWidth 画布宽度，单位为像素，用于居中定位
     */
    @Override
    public void draw(Graphics2D g, ImageEmbedded element, int canvasWidth) {
        int x = element.getX();
        // 独立的绘图上下文负责保存颜色、透明度和旋转状态，结束时统一释放。
        Graphics2D originalGraphics = g;
        g = (Graphics2D) originalGraphics.create();
        try {
            BufferedImage image = element.getImage();
            Dimension size = getDrawSize(element);
            int width = size.width, height = size.height;

            if (element.getRoundCorner() != null) // 设置圆角
                image = new ImageEffect(image).roundCorner(width, height, element.getRoundCorner()).getOutput();

            if (element.getBlur() != null) // 高斯模糊
                image = makeBlur(image, element.getBlur());

            if (element.isCenter()) { // 是否居中（优先）和绘制方向
                int centerX = (canvasWidth - width) / 2;
                x = centerX;
            } else if (element.getDirection() == Direction.RightLeft)
                x = x - width;
            else if (element.getDirection() == Direction.CenterLeftRight)
                x = x - width / 2;

            if (element.getRotate() != null) // 旋转
                g.rotate(Math.toRadians(element.getRotate()), x + (double) width / 2, element.getY() + (double) height / 2);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, element.getAlpha())); // 设置透明度
            g.drawImage(image, x, element.getY(), width, height, null); // 将元素图绘制到画布

            if (element.getRotate() != null) // 绘制完后反向旋转，以免影响后续元素
                g.rotate(-Math.toRadians(element.getRotate()), x + (double) width / 2, element.getY() + (double) height / 2);
        } finally {
            g.dispose();
        }
    }

    /**
     * 按缩放后的实际尺寸平铺图片，复用公共平铺循环。
     *
     * @param g            目标画笔
     * @param element      需要平铺的图片元素
     * @param canvasWidth  画布宽度，单位为像素
     * @param canvasHeight 画布高度，单位为像素
     */
    @Override
    public void drawRepeat(Graphics2D g, ImageEmbedded element, int canvasWidth, int canvasHeight) {
        Dimension size = getDrawSize(element);
        Painter.super.drawRepeat(g, element, canvasWidth, canvasHeight, size.width, size.height);
    }

    /**
     * 根据缩放模式计算绘制尺寸，不修改元素或原图。
     *
     * @param element 已配置图片、缩放模式及目标尺寸的元素
     * @return 实际绘制的宽高，单位为像素
     */
    private Dimension getDrawSize(ImageEmbedded element) {
        BufferedImage image = element.getImage();
        int width = 0, height = 0;

        switch (element.getZoomMode()) {
            case Origin:
                width = image.getWidth();
                height = image.getHeight();
                break;
            case Width:
                width = element.getWidth();
                height = image.getHeight() * width / image.getWidth();
                break;
            case Height:
                height = element.getHeight();
                width = image.getWidth() * height / image.getHeight();
                break;
            case WidthHeight:
                width = element.getWidth();
                height = element.getHeight();
                break;
        }

        return new Dimension(width, height);
    }

    /**
     * 以分离的水平、垂直加权平滑处理生成模糊效果。
     * <p>结果写入独立图片，不改变源图；半径越大，影响范围与计算成本越大。
     * 保留源图各像素的透明度，仅平滑颜色分量。</p>
     *
     * @param srcImage 待模糊的图片，不会被改写
     * @param radius   模糊半径；小于 1 时直接返回源图
     * @return 模糊后的新图片；半径小于 1 时直接返回未改动的源图
     */
    private static BufferedImage makeBlur(BufferedImage srcImage, int radius) {
        if (radius < 1)
            return srcImage;

        int w = srcImage.getWidth(), h = srcImage.getHeight();

        int[] pix = new int[w * h];
        srcImage.getRGB(0, 0, w, h, pix, 0, w);

        int wm = w - 1, hm = h - 1, wh = w * h;
        int div = radius + radius + 1;

        int[] r = new int[wh], g = new int[wh], b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];

        for (i = 0; i < 256 * divsum; i++)
            dv[i] = (i / divsum);

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer, stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum, rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;

            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }

            stackpointer = radius;

            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0)
                    vmin[x] = Math.min(x + radius + 1, wm);

                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }

            yw += w;
        }

        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;

            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm)
                    yp += w;
            }

            yi = x;
            stackpointer = radius;

            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0)
                    vmin[y] = Math.min(y + r1, hm) * w;

                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        BufferedImage result = new BufferedImage(w, h, srcImage.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, pix, 0, w);
        return result;
    }
}
