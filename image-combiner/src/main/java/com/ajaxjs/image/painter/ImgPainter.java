package com.ajaxjs.image.painter;

import com.ajaxjs.image.embedd.ImageEmbedded;
import com.ajaxjs.image.constant.enums.Direction;
import com.freewayso.image.combiner.ImageCombiner;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImgPainter implements Painter<ImageEmbedded> {
    @Override
    public void draw(Graphics2D g, ImageEmbedded element, int canvasWidth) {
        BufferedImage image = element.getImage();
        int width = 0, height = 0; // 计算缩放后的宽高

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
                height = element.getHeight();
                width = element.getWidth();
                break;
        }

        if (element.getRoundCorner() != null)// 设置圆角
            image = ImageCombiner.makeRoundCorner(image, width, height, element.getRoundCorner());

        if (element.getBlur() != null) // 高斯模糊
            image = makeBlur(image, element.getBlur());

        if (element.isCenter()) { // 是否居中（优先）和绘制方向
            int centerX = (canvasWidth - width) / 2;
            element.setX(centerX);
        } else if (element.getDirection() == Direction.RightLeft)
            element.setX(element.getX() - width);
        else if (element.getDirection() == Direction.CenterLeftRight)
            element.setX(element.getX() - width / 2);

        if (element.getRotate() != null) // 旋转
            g.rotate(Math.toRadians(element.getRotate()), element.getX() + (double) element.getWidth() / 2, element.getY() + (double) element.getHeight() / 2);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, element.getAlpha())); // 设置透明度
        g.drawImage(image, element.getX(), element.getY(), width, height, null); // 将元素图绘制到画布

        if (element.getRotate() != null) // 绘制完后反向旋转，以免影响后续元素
            g.rotate(-Math.toRadians(element.getRotate()), element.getX() + (double) element.getWidth() / 2, element.getY() + (double) element.getHeight() / 2);
    }

    @Override
    public void drawRepeat(Graphics2D g, ImageEmbedded element, int canvasWidth, int canvasHeight) {
        BufferedImage image = element.getImage();// 读取元素图
        int width = 0, height = 0; // 计算缩放后的宽高

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
                height = element.getHeight();
                width = element.getWidth();
                break;
        }

        int currentX = element.getX(), currentY = element.getY();

        while (currentX > 0)  // 起始坐标归位
            currentX = currentX - element.getRepeatPaddingHorizontal() - width;

        while (currentY > 0)
            currentY = currentY - element.getRepeatPaddingVertical() - height;

        int startY = currentY;


        while (currentX < canvasWidth) {// 从左往右绘制
            int i = 0;
            element.setX(currentX);
            currentX = currentX + element.getRepeatPaddingHorizontal() + width;

            while (currentY < canvasHeight) {// 从上往下绘制
                if (i % 2 != 0)  // 偶数行错位效果
                    element.setX(element.getX() - element.getRepeatRowOffset());

                element.setY(currentY);
                currentY = currentY + element.getRepeatPaddingVertical() + height;
                draw(g, element, canvasWidth);

                if (i % 2 != 0)  // 还原偏移的 x 坐标
                    element.setX(element.getX() + element.getRepeatRowOffset());

                i++;
            }

            currentY = startY; // 重置 y 坐标
        }
    }

    /**
     * 高斯模糊（毛玻璃效果）
     *
     * @param srcImage
     * @param radius
     * @return
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

        srcImage.setRGB(0, 0, w, h, pix, 0, w);

        return srcImage;
    }
}
