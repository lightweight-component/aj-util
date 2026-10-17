package com.ajaxjs.image;

import com.ajaxjs.image.model.*;
import com.ajaxjs.image.model.constant.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 原版素材演示：保留旧测试中的图片地址、坐标、文本和输出文件名。
 * 需要外网、原 D 盘素材及 /yahei.ttc，不替换为 test.jpg。
 * 手动运行：mvn -f aj-image/pom.xml test -Dtest=TestMigratedApp -Daj.image.legacyDemo=true
 * 输出默认放在 aj-image/target/legacy-demo，也可用 aj.image.demoOutput 指定目录。
 * 注意：拆行和文字测量由下方测试辅助方法实现，不是库的内置自动排版。
 * setSpace 仍是预留配置，不生效；普通多行 lineHeight 需要在拆行时传入。
 * 系统字体名称仍取决于运行环境；字体文件测试需要原 /yahei.ttc。
 */
@EnabledIfSystemProperty(named = "aj.image.legacyDemo", matches = "true")
public class TestMigratedApp {

    /**
     * 简单测试
     *
     * @throws Exception
     */
    @Test
    public void simpleTest() throws Exception {

        //合成器和背景图（整个图片的宽高和相关计算依赖于背景图，所以背景图的大小是个基准）
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);

        //加图片元素（居中绘制，圆角，半透明）
        configure(addImage(combiner, "https://img.thebeastshop.com/image/20201130115835493501.png?x-oss-process=image/resize,m_pad,w_750,h_783/auto-orient,1/quality,q_90/format,jpg", 0, 300), e -> { e.setCenter(true); });

        //加文本元素
        configure(addText(combiner, "周末大放送", 60, 100, 960), e -> { e.setColor(Color.red); });

        //合成图片
        combiner.combine();

        //保存文件（或getCombinedImageStream()并上传图片服务器）
        save(combiner, "simpleTest.jpg");

        //或者获取流（并上传oss等）
        //InputStream is = combiner.getCombinedImageStream();
        //String url = ossUtil.upload(is);
    }

    /**
     * 完整功能测试
     *
     * @throws Exception
     */
    @Test
    public void FullTest() throws Exception {
        String bgImageUrl = "https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png";                       //背景图（测试url形式）
        String qrCodeUrl = "http://imgtest.thebeastshop.com/file/combine_image/qrcodef3d132b46b474fe7a9cc6e76a511dfd5.jpg";     //二维码
        String productImageUrl = "https://img.thebeastshop.com/combine_image/funny_topic/resource/product_3x4.png";             //商品图
        BufferedImage waterMark = loadImage("https://img.thebeastshop.com/combine_image/funny_topic/resource/water_mark.png");  //水印图（测试BufferedImage形式）
        BufferedImage avatar = loadImage("https://img.thebeastshop.com/member/privilege/level-icon/level-three.jpg");           //头像
        String title = "# 最爱的家居";                                       //标题文本
        String content = "苏格拉底说：“如果没有那个桌子，可能就没有那个水壶”";  //内容文本

        //合成器和背景图（整个图片的宽高和相关计算依赖于背景图，所以背景图的大小是个基准）
        ImageContainer combiner = background(bgImageUrl, ImageFormat.PNG);
        combiner.setBackgroundBlur(30);     //设置背景加权模糊（毛玻璃效果）
        combiner.setCanvasRoundCorner(100); //设置整图圆角（输出格式必须为PNG）

        //商品图（设置坐标、宽高和缩放模式，若按宽度缩放，则高度按比例自动计算）
        configure(addImage(combiner, productImageUrl, 0, 160, 837, 0, ZoomMode.Width), e -> { e.setRoundCorner(46); e.setCenter(true); });       //居中绘制，会忽略x坐标参数，改为自动计算

        //标题（默认字体为“阿里巴巴普惠体”，也可以自己指定字体名称或Font对象）
        addText(combiner, title, 55, 150, 1400);

        //内容（设置文本自动换行，需要指定最大宽度（超出则换行）、最大行数（超出则丢弃）、行高）
        configure(addText(combiner, content, "微软雅黑", 40, 150, 1480), e -> { prepareLines(e, 837, 2, 60); });

        //头像（圆角设置一定的大小，可以把头像变成圆的）
        configure(addImage(combiner, avatar, 200, 1200, 130, 130, ZoomMode.WidthHeight), e -> { e.setRoundCorner(200); e.setBlur(5); });       //加权模糊，毛玻璃效果

        //水印（设置透明度，0.0~1.0）
        configure(addImage(combiner, waterMark, 630, 1200), e -> { e.setAlpha(.8f); e.setRotate(15); });     //旋转，0~360，按中心点旋转

        //二维码（强制按指定宽度、高度缩放）
        addImage(combiner, qrCodeUrl, 138, 1707, 186, 186, ZoomMode.WidthHeight);

        //元素对象也可以直接new，然后手动加入待绘制列表
        TextEmbedded textPrice = textElement("￥1290", 40, 600, 1400);
        textPrice.setStrikeThrough(true);       //删除线
        combiner.addElement(textPrice);         //加入待绘制集合

        //动态计算位置
        int offsetPrice = textPrice.getX() + textPrice.getWidth() + 10;
        configure(addText(combiner, "￥999", 60, offsetPrice, 1400), e -> { e.setColor(Color.red); });

        //执行图片合并
        combiner.combine();

        //保存文件
        save(combiner, "fullTest.png");

        //或者获取流（并上传oss等）
        //InputStream is = combiner.getCombinedImageStream();
        //String url = ossUtil.upload(is);
    }

    /**
     * 旋转测试
     *
     * @throws Exception
     */
    @Test
    public void rotateTest() throws Exception {
        String bg = "https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png";
        ImageContainer combiner = background(bg, ImageFormat.JPG);

        configure(addText(combiner, "我觉得应该可以正常显示", 80, 300, 300), e -> { e.setCenter(true); });
        configure(addText(combiner, "我觉得应该可以正常显示", 80, 300, 300), e -> { e.setColor(Color.red); e.setCenter(true); e.setRotate(40); });


        configure(addText(combiner, "测试一下多行文本换行加旋转的动作，不知道是否能正常显示", 80, 300, 600), e -> { e.setStrikeThrough(true); prepareLines(e, 600, 2, 80); });
        configure(addText(combiner, "测试一下多行文本换行加旋转的动作，不知道是否能正常显示", 80, 300, 600), e -> { e.setColor(Color.red); e.setStrikeThrough(true); prepareLines(e, 600, 2, 80); e.setRotate(40); });

        configure(addImage(combiner, "http://img.thebeastshop.com/images/index/imgs/8wzZ7St7KH.jpg", 300, 1000), e -> { e.setCenter(true); e.setRotate(45); });

        combiner.combine();
        save(combiner, "rotateTest.jpg");
    }

    /**
     * 动态计算宽度测试
     *
     * @throws Exception
     */
    @Test
    public void dynamicWidthDemoTest() throws Exception {
        String bg = "https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png";
        ImageContainer combiner = background(bg, ImageFormat.JPG);

        String str1 = "您出征";
        String str2 = "某城市";     //内容不定，宽度也不定
        String str3 = "，共在前线战斗了";
        String str4 = "365";     //内容不定，宽度也不定
        String str5 = "天！";
        int fontSize = 60;
        int xxxFontSize = 80;

        int offsetX = 20;
        int y = 300;

        TextEmbedded element1 = configure(addText(combiner, str1, fontSize, offsetX, y), e -> { e.setBaseLine(BaseLine.Bottom); });
        offsetX += element1.getWidth();

        TextEmbedded element2 = configure(addText(combiner, str2, xxxFontSize, offsetX, y), e -> { e.setBaseLine(BaseLine.Bottom); e.setColor(Color.red); });
        offsetX += element2.getWidth();

        TextEmbedded element3 = configure(addText(combiner, str3, fontSize, offsetX, y), e -> { e.setBaseLine(BaseLine.Bottom); });
        offsetX += element3.getWidth();

        TextEmbedded element4 = configure(addText(combiner, str4, xxxFontSize, offsetX, y), e -> { e.setBaseLine(BaseLine.Bottom); e.setColor(Color.red); });
        offsetX += element4.getWidth();

        configure(addText(combiner, str5, fontSize, offsetX, y), e -> { e.setBaseLine(BaseLine.Bottom); });

        BufferedImage img = loadImage("http://img.thebeastshop.com/images/index/imgs/8wzZ7St7KH.jpg");
        addImage(combiner, img, 20, 500);

        combiner.combine();
        save(combiner, "demo.jpg");
    }

    /**
     * Png透明背景图测试
     *
     * @throws IOException
     */
    @Test
    public void pngTest() throws Exception {

        BufferedImage bgImage = loadImage("d://memberCard.png");   //背景是圆角透明图
        String content = "2021-12-12 到期";

        //如背景包含透明部分，一定要用ImageFormat.PNG格式，否则合成后透明部分会变黑
        ImageContainer combiner = background(bgImage, 1000, 0, ZoomMode.Width, ImageFormat.PNG);

        //内容文本
        configure(addText(combiner, content, 38, 72, 260), e -> { e.setColor(Color.white); });

        //合成图片
        combiner.combine();

        //上传oss
        save(combiner, "pngTest.png");
    }

    /**
     * 矩形（圆形）绘制测试
     *
     * @throws Exception
     */
    @Test
    public void rectangleTest() throws Exception {
        //合成器和背景图（整个图片的宽高和相关计算依赖于背景图，所以背景图的大小是个基准）
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);

        //加图片元素（居中绘制，圆角，半透明）
        configure(addImage(combiner, "https://img.thebeastshop.com/image/20201130115835493501.png?x-oss-process=image/resize,m_pad,w_750,h_783/auto-orient,1/quality,q_90/format,jpg", 0, 300), e -> { e.setCenter(true); });

        //加文本元素
        configure(addText(combiner, "周末大放送", 60, 200, 960), e -> { e.setColor(Color.red); e.setAlpha(0.2f); });

        //加文本元素
        configure(addText(combiner, "周末大放送", 60, 200, 560), e -> { e.setColor(Color.red); });

        //加入矩形元素
        configure(addRectangle(combiner, 200, 500, 300, 300), e -> { e.setColor(Color.BLUE); });

        //加入矩形元素（圆角）
        configure(addRectangle(combiner, 300, 700, 300, 300), e -> { e.setColor(Color.YELLOW); e.setRoundCorner(100); e.setBorderSize(10); e.setAlpha(.8f); gradient(e, Color.yellow, Color.blue, GradientDirection.LeftRight); });

        //加入矩形元素（圆）
        configure(addRectangle(combiner, 200, 500, 300, 300), e -> { e.setColor(Color.RED); e.setRoundCorner(300); e.setAlpha(.8f); e.setCenter(true); });

        //合成图片
        combiner.combine();

        save(combiner, "rectangleTest.jpg");
    }

    /**
     * 文本自动换行测试
     *
     * @throws Exception
     */
    @Test
    public void breakLineTest() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        Font font = new Font("阿里巴巴普惠体", Font.PLAIN, 62);

        //添加文字，并设置换行参数
        configure(addText(combiner, "大江东去，浪淘尽，千古风流人物。故垒西边，人道是：三国周郎赤壁。乱石穿空，惊涛拍岸，卷起千堆雪。江山如画，一时多少豪杰。", font, 100, 0), e -> { e.setColor(Color.red); e.setCenter(true); e.setStrikeThrough(true); prepareLines(e, 630, 8, 100, LineAlign.Right); });    //不给LineAlign参数的话，默认左对齐
        //合成图片
        combiner.combine();

        save(combiner, "breakLineTest.jpg");
    }

    /**
     * 手动指定换行符测试
     *
     * @throws Exception
     */
    @Test
    public void breakLineSplitterTest() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        Font font = new Font("阿里巴巴普惠体", Font.PLAIN, 60);

        //添加文字，并设置换行参数
        configure(addText(combiner, "大江东去，浪淘尽，\r\n千古风流人物。\r\n故垒西边，\r\n人道是：三国周郎赤壁。\r\n乱石穿空，惊涛拍岸，卷起千堆雪。\r\n江山如画，\r\n一时多少豪杰。", font, 0, 150), e -> { e.setColor(Color.red); prepareLines(e, "\r\n", 100, LineAlign.Right); });     //不给LineAlign参数的话，默认左对齐
        //合成图片
        combiner.combine();

        save(combiner, "breakLineSplitterTest.jpg");
    }

    /**
     * 文字竖排测试
     */
    @Test
    public void verticalTextTest() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        //添加文字，并设置为自动换行，且行宽设为0
        configure(addText(combiner, "通过自动换行功能，实现文字竖排", 50, 200, 100), e -> { prepareLines(e, 0, 20, 60); e.setDirection(Direction.CenterLeftRight); });

        configure(addText(combiner, "将文本元素设为自动换行，且行宽设为0即可", 50, 300, 100), e -> { prepareLines(e, 0, 50, 60, LineAlign.Center); });
        //合成图片
        combiner.combine();

        save(combiner, "verticalTextTest.jpg");
    }

    /**
     * 文字加粗、斜体测试
     */
    @Test
    public void boldTextTest() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        //添加文字，并设置为自动换行，且行宽设为0
        configure(addText(combiner, "通过自动换行功能，实现文字竖排", Font.BOLD, 50, 200, 100), e -> { prepareLines(e, 0, 20, 60); });

        configure(addText(combiner, "将文本元素设为自动换行，且行宽设为0即可", Font.ITALIC, 50, 300, 100), e -> { prepareLines(e, 0, 50, 60, LineAlign.Center); });
        //合成图片
        combiner.combine();

        save(combiner, "boldTextTest.jpg");
    }

    /**
     * 测试矩形渐变色
     */
    @Test
    public void GradientTest() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);

        //四向渐变
        configure(addRectangle(combiner, 100, 100, 300, 300), e -> { e.setRoundCorner(50); gradient(e, Color.blue, Color.red, GradientDirection.TopBottom); });
        configure(addRectangle(combiner, 450, 100, 300, 300), e -> { gradient(e, Color.blue, Color.red, GradientDirection.LeftRight); });
        configure(addRectangle(combiner, 100, 450, 300, 300), e -> { gradient(e, Color.blue, Color.red, GradientDirection.LeftTopRightBottom); });
        configure(addRectangle(combiner, 450, 450, 300, 300), e -> { gradient(e, Color.blue, Color.red, GradientDirection.RightTopLeftBottom); });

        //渐变拉伸对比
        configure(addRectangle(combiner, 100, 800, 300, 300), e -> { gradient(e, Color.yellow, Color.magenta, GradientDirection.TopBottom); });
        configure(addRectangle(combiner, 450, 800, 300, 300), e -> { gradient(e, Color.yellow, Color.magenta, 0, 100, GradientDirection.TopBottom); });
        configure(addRectangle(combiner, 800, 800, 300, 300), e -> { gradient(e, Color.yellow, Color.magenta, 100, 0, GradientDirection.TopBottom); });

        //渐变+透明度
        addText(combiner, "渐变+透明度", 30, 120, 1300);
        configure(addRectangle(combiner, 100, 1150, 300, 300), e -> { gradient(e, Color.yellow, Color.magenta, GradientDirection.TopBottom); e.setAlpha(.6f); });

        //合成图片
        combiner.combine();

        save(combiner, "GradientTest.jpg");
    }

    /**
     * 不带背景图的空白画布，画布高度根据内容动态计算
     *
     * @throws Exception
     */
    @Test
    public void ymxkTest() throws Exception {
        BufferedImage imgTop = loadImage("d://ymxk/top.png");
        BufferedImage imgStar = loadImage("d://ymxk/star.png");
        BufferedImage imgGrayStar = loadImage("d://ymxk/star_gray.png");
        String content = "这是很长一串文字，用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度，这是很长一串文字，这是很长一串文字，用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度，这是很长一串文字，用于计算背景图的高度";

        //用于临时计算高度
        TextEmbedded tempElement = textElement(content, 18, 0, 340);
        tempElement.setCenter(true);
        prepareLines(tempElement, 320, 20, 25);
        int contentHeight = tempElement.getHeight();

        //来一个空的背景
        ImageContainer combiner = background(343, 400 + contentHeight, ImageFormat.PNG);
        combiner.setCanvasRoundCorner(60);

        //加入各种元素
        addImage(combiner, imgTop, 0, 0);
        configure(addRectangle(combiner, 0, imgTop.getHeight() - 85, imgTop.getWidth(), 85), e -> { gradient(e, Color.gray, Color.red, GradientDirection.TopBottom); e.setAlpha(.3f); });  //渐变遮罩
        configure(addText(combiner, "德军总部：新血脉", 18, 10, 205), e -> { e.setColor(Color.white); });
        configure(addText(combiner, "Wolfenstein Yound Blood", 10, 10, 220), e -> { e.setColor(Color.white); });
        configure(addText(combiner, "第一人称射击", 12, 10, 240), e -> { e.setColor(Color.white); });
        configure(addText(combiner, "9.0", new Font("微软雅黑", Font.BOLD, 22), 290, 210), e -> { e.setColor(Color.white); });
        for (int i = 0; i < 5; i++) {
            addImage(combiner, i < 4 ? imgStar : imgGrayStar, 280 + i * 12, 220);
        }
        configure(addImage(combiner, imgTop, 10, 270, 40, 40, ZoomMode.WidthHeight), e -> { e.setRoundCorner(300); });
        addText(combiner, "苏轼的早餐", "微软雅黑", 16, 60, 290);
        combiner.addElement(tempElement);

        combiner.combine();
        save(combiner, "fonttest.jpg");
    }

    /**
     * 计算文字高度
     */
    @Test
    public void testComputeFontHeight() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        configure(addText(combiner, "点击画布上方", 60, 0, 0), e -> { e.setLineHeight(200); });

        configure(addRectangle(combiner, 300, 300, 300, 300), e -> { e.setColor(Color.red); });
        combiner.combine();
        save(combiner, "computeHeight.jpg");
    }

    /**
     * 测试绘制方向
     */
    @Test
    public void testDirection() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        //单行文本
        configure(addText(combiner, "测试绘制方向1", 60, canvasWidths.get(combiner) / 2, 100), e -> { e.setDirection(Direction.CenterLeftRight); });
        configure(addText(combiner, "看看右对齐到效果", 60, 1000, 200), e -> { e.setDirection(Direction.RightLeft); });
        //多行文本
        configure(addText(combiner, "多行文本多行文本多行文本多行文本多行文本多行文本", 60, 1000, 300), e -> { prepareLines(e, 600, 5, LineAlign.Right); e.setDirection(Direction.RightLeft); });
        //图片
        configure(addImage(combiner, "http://img.thebeastshop.com/images/index/imgs/8wzZ7St7KH.jpg", 1000, 600), e -> { e.setDirection(Direction.RightLeft); });
        //矩形（右到左）
        configure(addRectangle(combiner, 1000, 900, 200, 200), e -> { e.setColor(new Color(100, 32, 200)); e.setDirection(Direction.RightLeft); });
        //矩形（中间到两边）
        configure(addRectangle(combiner, 1000, 1100, 200, 200), e -> { e.setColor(new Color(200, 132, 20)); e.setDirection(Direction.CenterLeftRight); });

        combiner.combine();
        save(combiner, "testDirection.jpg");
    }

    /**
     * 自动适应最大宽度（超出则自动缩小字号，以适应之）
     *
     * @throws Exception
     */
    @Test
    public void testAutoFixWidth() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        configure(addText(combiner, "测试自适应最大宽度，测试自适应最大宽度，测试自适应最大宽度", 60, 0, 200), e -> { e.setAutoFitMaxWidth(600); e.setAutoFitMinFontSize(40); });

        combiner.combine();
        save(combiner, "testAutoFixWidth.jpg");
    }

    /**
     * 绘制一个时间轴
     */
    @Test
    public void testDrawTimeLine() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        int startX = 200;
        int startY = 200;
        int stepY = 200;
        int radius = 20;
        int lineWidth = 6;

        for (int i = 0; i < 5; i++) {
            int currentY = startY + stepY * (i + 1);
            //竖线
            if (i < 4) {
                configure(addRectangle(combiner, startX, startY + stepY * (i + 1) + radius / 2, lineWidth, stepY), e -> { e.setColor(Color.orange); e.setDirection(Direction.CenterLeftRight); });   //用中间到两边，不用考虑限宽问题了（不用再精细的计算x坐标）
            }
            //圆圈
            configure(addRectangle(combiner, startX, currentY, radius * 2, radius * 2), e -> { e.setRoundCorner(radius * 2); e.setColor(Color.green); e.setDirection(Direction.CenterLeftRight); });
            //文本
            TextEmbedded text = addText(combiner, "公司于2030年成功在火星设立了办事处", 30, startX + radius + 50, currentY);
            configure(addText(combiner, "2022-02-16 18:55:26", 26, text.getX() + text.getWidth(), currentY + text.getHeight()), e -> { e.setDirection(Direction.RightLeft); e.setColor(Color.lightGray); });
        }

        combiner.combine();
        save(combiner, "testDrawTimeLine.jpg");
    }

    /**
     * 测试字间距
     */
    @Test
    public void testSpacing() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        configure(addText(combiner, "测试字间距，hello my world", 40, 100, 200), e -> { e.setSpace(.5f); e.setStrikeThrough(true); });

        combiner.combine();
        save(combiner, "testSpacing.jpg");
    }

    /**
     * 测试水印平铺
     */
    @Test
    public void testRepeat() throws Exception {
        ImageContainer combiner = background("file:///d:/pure.jpg", ImageFormat.JPG);        //采用file:///协议加载图片
        String productImageUrl = "https://img.thebeastshop.com/image/20200807160107681379.JPG?x-oss-process=image/resize,m_pad,w_374,h_390/auto-orient,1/quality,q_90/format,jpg";             //商品图

        configure(addRectangle(combiner, 200, 200, 300, 300), e -> { e.setRoundCorner(60); e.setColor(Color.lightGray); e.setAlpha(.6f); repeat(e, true, 50); });

        configure(addImage(combiner, productImageUrl, 380, 230, 100, 0, ZoomMode.Width), e -> { e.setAlpha(.4f); e.setRotate(-10); e.setRoundCorner(30); repeat(e, true, 100, 30); });

        configure(addText(combiner, "水印平铺效果", 40, 400, 200), e -> { e.setSpace(.5f); e.setRotate(30); repeat(e, true, 80, 150); });

        combiner.combine();
        save(combiner, "testRepeat.jpg");
    }

    @Test
    public void testFeature1() throws Exception {

        ImageContainer combiner = background("file:///d:/pure.jpg", ImageFormat.JPG);        //采用file:///协议加载图片
        String productImageUrl = "https://img.thebeastshop.com/image/20200807160107681379.JPG?x-oss-process=image/resize,m_pad,w_374,h_390/auto-orient,1/quality,q_90/format,jpg";             //商品图

        int fontSize = 38;

        int x1 = 50;
        int x2 = 400;
        int x3 = 750;

        int y1 = 80;
        int y2 = 300;
        int y3 = 600;
        int y4 = 900;
        int y5 = 1200;
        int y6 = 1450;
        int y7 = 1700;

        //字体、字号、颜色、删除线
        addText(combiner, "基本：", fontSize, x1, y1);
        configure(addText(combiner, "字体、字号、颜色、删除线", "微软雅黑", fontSize, x2 - 150, y1), e -> { e.setColor(Color.red); e.setStrikeThrough(true); });


        //旋转
        addText(combiner, "旋转：", fontSize, x1, y2);
        configure(addText(combiner, "文字旋转20度", fontSize, x2 - 150, y2), e -> { e.setRotate(20); });
        configure(addImage(combiner, productImageUrl, x3 - 180, y2, 200, 0, ZoomMode.Width), e -> { e.setRotate(20); });
        configure(addImage(combiner, productImageUrl, x3 + 80, y2, 200, 0, ZoomMode.Width), e -> { e.setRotate(-20); });

        //圆角
        addText(combiner, "圆角：", fontSize, x1, y3);
        configure(addImage(combiner, productImageUrl, x2 - 150, y3, 200, 0, ZoomMode.Width), e -> { e.setRoundCorner(50); });
        configure(addRectangle(combiner, x3 - 200, y3, 200, 200), e -> { e.setRoundCorner(50); e.setColor(Color.lightGray); });
        configure(addRectangle(combiner, x3 + 100, y3, 200, 200), e -> { e.setRoundCorner(200); e.setColor(Color.orange); });

        //自动换行
        configure(addText(combiner, "自动换行，超过指定宽度自动换到下一行，超过指定行数则丢弃", fontSize, x1, y4), e -> { prepareLines(e, 320, 4); });
        configure(addText(combiner, "自动换行，超过指定宽度自动换到下一行，超过指定行数则丢弃", fontSize, x2, y4), e -> { prepareLines(e, 320, 4, LineAlign.Center); });
        configure(addText(combiner, "自动换行，超过指定宽度自动换到下一行，超过指定行数则丢弃", fontSize, x3, y4), e -> { prepareLines(e, 320, 4, LineAlign.Right); });

        //绘制方向
        //addText(combiner, "绘制方向：", fontSize, x1, y5);
        addText(combiner, "从左到右", fontSize, x1 + 100, y5);
        addImage(combiner, productImageUrl, x2 + 100, y5, 200, 0, ZoomMode.Width);
        configure(addRectangle(combiner, x3 + 100, y5, 200, 200), e -> { e.setColor(Color.lightGray); });

        configure(addText(combiner, "从右到左", fontSize, x1 + 100, y6), e -> { e.setDirection(Direction.RightLeft); });
        configure(addImage(combiner, productImageUrl, x2 + 100, y6, 200, 0, ZoomMode.Width), e -> { e.setDirection(Direction.RightLeft); });
        configure(addRectangle(combiner, x3 + 100, y6, 200, 200), e -> { e.setColor(Color.lightGray); e.setDirection(Direction.RightLeft); });

        configure(addText(combiner, "中间到两边", fontSize, x1 + 100, y7), e -> { e.setDirection(Direction.CenterLeftRight); });
        configure(addImage(combiner, productImageUrl, x2 + 100, y7, 200, 0, ZoomMode.Width), e -> { e.setDirection(Direction.CenterLeftRight); });
        configure(addRectangle(combiner, x3 + 100, y7, 200, 200), e -> { e.setColor(Color.lightGray); e.setDirection(Direction.CenterLeftRight); });

        //参考线
        configure(addRectangle(combiner, x1 + 98, y5 - 50, 3, 800), e -> { e.setColor(Color.red); });
        configure(addRectangle(combiner, x2 + 98, y5 - 50, 3, 800), e -> { e.setColor(Color.red); });
        configure(addRectangle(combiner, x3 + 98, y5 - 50, 3, 800), e -> { e.setColor(Color.red); });

        combiner.combine();
        save(combiner, "testFeatures1.jpg");
    }

    @Test
    public void testFeature2() throws Exception {

        ImageContainer combiner = background("file:///d:/pure.jpg", ImageFormat.JPG);        //采用file:///协议加载图片
        String productImageUrl = "https://img.thebeastshop.com/image/20200807160107681379.JPG?x-oss-process=image/resize,m_pad,w_374,h_390/auto-orient,1/quality,q_90/format,jpg";             //商品图

        int fontSize = 38;

        int x1 = 50;
        int x2 = 400;
        int x3 = 750;

        int y1 = 80;
        int y2 = 200;
        int y3 = 500;
        int y4 = 800;
        int y5 = 1100;
        int y6 = 1400;
        int y7 = 1600;

        //居中绘制
        configure(addText(combiner, "居中绘制，忽略x坐标，改为自动计算", fontSize, x1, y1), e -> { e.setCenter(true); });
        configure(addImage(combiner, productImageUrl, x1, y2, 200, 0, ZoomMode.Width), e -> { e.setCenter(true); });
        configure(addRectangle(combiner, x1, y3, 200, 200), e -> { e.setCenter(true); gradient(e, Color.orange, Color.pink, GradientDirection.TopBottom); });

        //加权模糊
        addText(combiner, "加权模糊：", fontSize, x1, y4);
        configure(addImage(combiner, productImageUrl, x2, y4, 200, 0, ZoomMode.Width), e -> { e.setBlur(10); });

        //透明度
        addText(combiner, "透明度：", fontSize, x1, y5);
        configure(addRectangle(combiner, x2 + 50, y5 + 40, 400, 100), e -> { e.setColor(Color.orange); });
        configure(addText(combiner, "透明度0.5", 60, x2, y5), e -> { e.setAlpha(.5f); });
        configure(addImage(combiner, productImageUrl, x3, y5, 200, 0, ZoomMode.Width), e -> { e.setAlpha(.5f); });

        //字间距
        addText(combiner, "字间距：", fontSize, x1, y6);
        configure(addText(combiner, "加大文字间距看起来更舒适", fontSize, x2, y6), e -> { e.setSpace(.5f); });

        //行高
        addText(combiner, "行高：", fontSize, x1, y7);
        configure(addText(combiner, "行高主要用于设置文字上下补白，可以和Sketch设计稿保持一致，单行文本或自动换行文本都可以设置", fontSize, x2, y7), e -> { prepareLines(e, 600, 3); e.setLineHeight(80); });

        combiner.combine();
        save(combiner, "testFeature2.jpg");
    }

    /**
     * 测试保存文件压缩质量
     */
    @Test
    public void testQuality() throws Exception {
        ImageContainer combiner = background("file:///D:/testTuZi.jpg", ImageFormat.PNG);
        addImage(combiner, "file:///D:/testTuZi.jpg", 0, 0);
        combiner.setQuality(0.5f);
        combiner.combine();
        save(combiner, "testAutoFixWidth4.png");
    }

    @Test
    public void testTrans() throws Exception {
        ImageContainer combiner = background(300, 300, ImageFormat.PNG);
        addText(combiner, "这是一个透明背景图", 20, 100, 100);
        addImage(combiner, "file:///D:/tuzi.png", 30, 30, 50, 50, ZoomMode.WidthHeight);
        combiner.combine();
        save(combiner, "透明图.png");
    }

    /**
     * 测试文本参考基线
     */
    @Test
    public void testBaseLine() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        configure(addRectangle(combiner, 100, 200, 1000, 1), e -> { e.setColor(Color.red); });
        configure(addRectangle(combiner, 100, 500, 1000, 1), e -> { e.setColor(Color.red); });

        configure(addText(combiner, "Top", 60, 150, 200), e -> { e.setBaseLine(BaseLine.Top); });         //不设置默认Top
        configure(addText(combiner, "Middle", 60, 320, 200), e -> { e.setBaseLine(BaseLine.Middle); });
        configure(addText(combiner, "Bottom", 60, 590, 200), e -> { e.setBaseLine(BaseLine.Bottom); });
        configure(addText(combiner, "Base", 60, 860, 200), e -> { e.setBaseLine(BaseLine.Base); });


        combiner.combine();
        save(combiner, "testBaseLine.jpg");
    }

    /**
     * 测试从项目路径加载字体文件
     *
     * @throws Exception
     */
    @Test
    public void testLoadFont() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);

        //字体文件采用Launcher.class.getResourceAsStream加载，fontNameOrPath参数包含.认为是字体文件，否则认为是字体名
        addText(combiner, "微软雅黑", "/yahei.ttc", 60, 150, 300);
        addText(combiner, "微软雅黑", "/yahei.ttc", 60, 150, 400);

        combiner.combine();
        save(combiner, "testLoadFont.jpg");
    }

    /**
     * 测试水印错位效果
     *
     * @throws Exception
     */
    @Test
    public void testRepeatOffset() throws Exception {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        configure(addText(combiner, "水印错位效果", 30, 100, 300), e -> { repeat(e, true, 100, 200, -60); e.setRotate(-15); });
        combiner.combine();
        save(combiner, "testRepeatOffset.png");
    }

    /**
     * 测试绘制连续的方框，填充数组，自动调整文字宽度
     *
     */
    @Test
    public void testRectangles() {
        ImageContainer combiner = background("https://img.thebeastshop.com/combine_image/funny_topic/resource/bg_3x4.png", ImageFormat.JPG);
        int offsetX = 100;
        int y = 200;
        int rectangleSize = 50;
        int fontMaxWidth = 40;
        for (int i = 2; i < 12; i++) {
            configure(addRectangle(combiner, offsetX, y, rectangleSize, rectangleSize), e -> { e.setColor(new Color(252, 196, 4)); e.setAlpha(0.5f); });
            configure(addRectangle(combiner, offsetX, y, rectangleSize, rectangleSize), e -> { e.setBorderSize(1); e.setColor(Color.red); });
            configure(addText(combiner, i * i * i + "", 40, offsetX + rectangleSize / 2, y + rectangleSize / 2), e -> { e.setBaseLine(BaseLine.Middle); e.setDirection(Direction.CenterLeftRight); e.setAutoFitMaxWidth(fontMaxWidth); });
            offsetX += rectangleSize - 1;
        }
        combiner.combine();
        save(combiner, "testRectangles.jpg");
    }


    /**
     * 显示所有可用字体
     */
    @Test
    public void showFonts() throws InterruptedException {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontName = e.getAvailableFontFamilyNames();
        for (int i = 0; i < fontName.length; i++) {
            System.out.println(fontName[i]);
        }
    }

    /* 以下仅为演示侧的新 API 组装方法，不修改生产 API。 */
    private final Map<ImageContainer, Integer> canvasWidths = new IdentityHashMap<>();

    private BufferedImage loadImage(Object source) {
        if (source instanceof BufferedImage)
            return (BufferedImage) source;
        String location = (String) source;
        try {
            BufferedImage image;
            if (location.startsWith("http://") || location.startsWith("https://") || location.startsWith("file:")) {
                java.net.URLConnection connection = new URL(location).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(20000);
                try (InputStream input = connection.getInputStream()) {
                    image = ImageIO.read(input);
                }
            } else {
                image = ImageIO.read(new File(location));
            }
            if (image == null)
                throw new IllegalArgumentException("无法识别原版素材：" + location);
            return image;
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("无法读取原版素材，不会用其他图片替代：" + location, e);
        }
    }

    private ImageContainer background(Object source, ImageFormat format) {
        return background(source, 0, 0, ZoomMode.Origin, format);
    }

    private ImageContainer background(Object source, int width, int height, ZoomMode mode, ImageFormat format) {
        BufferedImage image = loadImage(source);
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(format);
        container.setBackgroundImage(image, mode, width, height);
        int canvasWidth = mode == ZoomMode.Origin ? image.getWidth()
                : mode == ZoomMode.Height ? image.getWidth() * height / image.getHeight() : width;
        canvasWidths.put(container, canvasWidth);
        return container;
    }

    private ImageContainer background(int width, int height, ImageFormat format) {
        return background(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), format);
    }

    private ImageEmbedded addImage(ImageContainer container, Object source, int x, int y) {
        return addImage(container, source, x, y, 0, 0, ZoomMode.Origin);
    }

    private ImageEmbedded addImage(ImageContainer container, Object source, int x, int y,
                                   int width, int height, ZoomMode mode) {
        ImageEmbedded element = new ImageEmbedded();
        element.setImage(loadImage(source));
        element.setX(x);
        element.setY(y);
        element.setWidth(width);
        element.setHeight(height);
        element.setZoomMode(mode);
        container.addElement(element);
        return element;
    }

    private TextEmbedded textElement(String value, int size, int x, int y) {
        TextEmbedded element = new TextEmbedded();
        element.setText(value);
        element.setFont(new Font("阿里巴巴普惠体", Font.PLAIN, size));
        element.setX(x);
        element.setY(y);
        measure(element);
        return element;
    }

    private TextEmbedded addText(ImageContainer container, String value, int size, int x, int y) {
        TextEmbedded element = textElement(value, size, x, y);
        container.addElement(element);
        return element;
    }

    private TextEmbedded addText(ImageContainer container, String value, String fontNameOrPath,
                                 int size, int x, int y) {
        TextEmbedded element = addText(container, value, size, x, y);
        if (fontNameOrPath.startsWith("/")) {
            // 当前库接收 Font，不自动读字体文件。缺少原字体时明确失败，不替换素材。
            try (InputStream input = getClass().getResourceAsStream(fontNameOrPath)) {
                if (input == null)
                    throw new IllegalArgumentException("缺少原字体资源：" + fontNameOrPath);
                element.setFont(Font.createFont(Font.TRUETYPE_FONT, input).deriveFont((float) size));
            } catch (IOException | FontFormatException e) {
                throw new IllegalArgumentException("无法加载原字体：" + fontNameOrPath, e);
            }
        } else {
            element.setFont(new Font(fontNameOrPath, Font.PLAIN, size));
        }
        measure(element);
        return element;
    }

    private TextEmbedded addText(ImageContainer container, String value, Font font, int x, int y) {
        TextEmbedded element = addText(container, value, font.getSize(), x, y);
        element.setFont(font);
        measure(element);
        return element;
    }

    private TextEmbedded addText(ImageContainer container, String value, int style, int size, int x, int y) {
        TextEmbedded element = addText(container, value, size, x, y);
        element.setFont(element.getFont().deriveFont(style));
        measure(element);
        return element;
    }

    private FontMetrics metrics(TextEmbedded element) {
        Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            return g.getFontMetrics(element.getFont());
        } finally {
            g.dispose();
        }
    }

    private void measure(TextEmbedded element) {
        FontMetrics metrics = metrics(element);
        element.setWidth(metrics.stringWidth(element.getText()));
        element.setHeight(metrics.getHeight());
    }

    private RectangleEmbedded addRectangle(ImageContainer container, int x, int y, int width, int height) {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(x);
        element.setY(y);
        element.setWidth(width);
        element.setHeight(height);
        container.addElement(element);
        return element;
    }

    private <T extends EmbeddedElement> T configure(T element, Consumer<T> configuration) {
        configuration.accept(element);
        if (element instanceof TextEmbedded) {
            TextEmbedded text = (TextEmbedded) element;
            if (text.isAutoBreakLine()) {
                // 新画笔读取每行自身的样式，因此将本演示中父元素样式明确传递给各行。
                for (TextEmbedded line : text.getBreakLineElements()) {
                    line.setColor(text.getColor());
                    line.setFont(text.getFont());
                    line.setAlpha(text.getAlpha());
                    line.setRotate(text.getRotate());
                    line.setStrikeThrough(text.isStrikeThrough());
                    line.setDirection(text.getDirection());
                    line.setCenter(text.isCenter());
                    line.setBaseLine(text.getBaseLine());
                }
            }
        }
        return element;
    }

    private void gradient(RectangleEmbedded element, Color from, Color to, GradientDirection direction) {
        gradient(element, from, to, 0, 0, direction);
    }

    private void gradient(RectangleEmbedded element, Color from, Color to, int fromExtend, int toExtend,
                          GradientDirection direction) {
        element.setFromColor(from);
        element.setToColor(to);
        element.setFromExtend(fromExtend);
        element.setToExtend(toExtend);
        element.setGradientDirection(direction);
    }

    private void repeat(EmbeddedElement element, boolean enabled, int padding) {
        repeat(element, enabled, padding, padding, 0);
    }

    private void repeat(EmbeddedElement element, boolean enabled, int horizontal, int vertical) {
        repeat(element, enabled, horizontal, vertical, 0);
    }

    private void repeat(EmbeddedElement element, boolean enabled, int horizontal, int vertical, int offset) {
        element.setRepeat(enabled);
        element.setRepeatPaddingHorizontal(horizontal);
        element.setRepeatPaddingVertical(vertical);
        element.setRepeatRowOffset(offset);
    }

    private void prepareLines(TextEmbedded element, int maxWidth, int maxLines) {
        prepareLines(element, maxWidth, maxLines, metrics(element).getHeight(), LineAlign.Left);
    }

    private void prepareLines(TextEmbedded element, int maxWidth, int maxLines, int lineHeight) {
        prepareLines(element, maxWidth, maxLines, lineHeight, LineAlign.Left);
    }

    private void prepareLines(TextEmbedded element, int maxWidth, int maxLines, LineAlign align) {
        prepareLines(element, maxWidth, maxLines, metrics(element).getHeight(), align);
    }

    private void prepareLines(TextEmbedded element, int maxWidth, int maxLines, int lineHeight, LineAlign align) {
        List<String> parts = new ArrayList<>();
        FontMetrics metrics = metrics(element);
        for (String paragraph : element.getText().split("\\r?\\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (int offset = 0; offset < paragraph.length();) {
                int codePoint = paragraph.codePointAt(offset);
                String character = new String(Character.toChars(codePoint));
                if (line.length() > 0 && (maxWidth <= 0
                        || metrics.stringWidth(line.toString() + character) > maxWidth)) {
                    parts.add(line.toString());
                    line.setLength(0);
                }
                line.append(character);
                offset += Character.charCount(codePoint);
            }
            parts.add(line.toString());
        }
        setLines(element, parts, maxLines, lineHeight, align);
    }

    private void prepareLines(TextEmbedded element, String splitter, int lineHeight, LineAlign align) {
        List<String> parts = java.util.Arrays.asList(element.getText().split(splitter, -1));
        setLines(element, parts, parts.size(), lineHeight, align);
    }

    private void setLines(TextEmbedded element, List<String> parts, int maxLines, int lineHeight, LineAlign align) {
        List<TextEmbedded> lines = new ArrayList<>();
        int width = 0;
        for (int i = 0; i < Math.min(parts.size(), maxLines); i++) {
            TextEmbedded line = textElement(parts.get(i), element.getFont().getSize(),
                    element.getX(), element.getY() + i * lineHeight);
            line.setFont(element.getFont());
            measure(line);
            width = Math.max(width, line.getWidth());
            lines.add(line);
        }
        element.setAutoBreakLine(true);
        element.setBreakLineElements(lines);
        element.setLineAlign(align);
        element.setLineHeight(lineHeight);
        element.setWidth(width);
        element.setHeight(lines.size() * lineHeight);
    }

    private void save(ImageContainer container, String originalName) {
        // 保留原文件名，集中输出到可配置目录，不覆盖原 D 盘文件。
        Path directory = Paths.get(System.getProperty("aj.image.demoOutput", "target/legacy-demo"));
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        Path output = directory.resolve(originalName);
        container.save(output.toString());
        org.junit.jupiter.api.Assertions.assertTrue(Files.isRegularFile(output));
    }
}
