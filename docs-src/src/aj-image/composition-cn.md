---
title: 图片合成
layout: layouts/aj-util-cn.njk
alternate: /composition/
---

# 图片合成

使用 `ImageContainer` 先设置背景以确定画布尺寸，再添加元素，调用 `combine()` 合成，最后输出。后添加的元素覆盖先添加的元素。元素 setter 不支持链式调用。

## 最小示例

需要带图片、文字、水印和二维码的完整示例？请阅读[合成教程](/aj-image/tutorial-cn/)。

```java
import com.ajaxjs.image.ImageContainer;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.ZoomMode;
import java.awt.Font;

ImageContainer container = new ImageContainer();
container.setOutputFormat(ImageFormat.PNG);
container.setBackgroundImage("background.jpg", ZoomMode.Origin, 0, 0);

TextEmbedded title = new TextEmbedded();
title.setText("Hello aj-image");
title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
title.setX(30);
title.setY(40);
container.addElement(title);

container.combine();
container.save("poster.png");
```

示例读取已有的 `background.jpg`，并保存为 `poster.png`。实际项目可替换素材和文字；输出父目录需要预先存在。透明背景和整图圆角请使用 PNG。

## 能够做什么？

- 图片：定位、缩放、旋转、圆角、透明度和模糊。
- 文字：字体、字号、颜色、透明度、旋转、删除线、基线定位，以及超宽时缩小字号或添加省略号。
- 矩形：尺寸、纯色、渐变、透明度、圆角和边框；设置边框后只绘制轮廓，不同时填充内部。
- 三种素材都支持水平居中、水平定位方向和重复平铺。绘制方向用于决定坐标如何定位，不是图片镜像或文字倒序。

## 当前限制

- 可以绘制预先拆分好的多行文字，但还不能根据指定宽度自动换行。启用 `setAutoBreakLine(true)` 时，仍需提供 `breakLineElements`，并设置每行的位置和样式；`setSpace()` 暂不生效。
- 图片元素需要先通过 `setImage()` 提供 `BufferedImage`；仅设置 `setImgUrl()` 不会自动加载图片。背景图入口 `ImageContainer.setBackgroundImage(String, ...)` 则支持本地路径和 HTTP/HTTPS 地址，但网络读取没有可配置的超时或身份认证，请勿直接传入不可信的地址。
- 图片模糊采用加权平滑，并非严格的高斯模糊；只平滑颜色，保留原有透明度。

## 使用时需要了解的行为

- `setBackgroundImage(image, zoomMode, width, height)` 按指定模式计算画布大小，并将背景图绘制到对应尺寸。
- 背景图也可以传入本地路径或 HTTP/HTTPS 地址；读取失败会立即报错，不添加背景。网络读取没有可配置的超时或身份认证，请勿直接传入不可信的地址。
- 绘制会保留元素坐标和调用方的画笔状态。重复使用同一个元素时，位置不会越画越偏；文字平铺时，文字基线也会跟随每一格移动。
- 文字自动适应宽度时，每次调整局部字体大小后都会重新测量。连 `...` 都放不下时，绘制为空文字。原始文本、字体和坐标不会被改写，方便再次绘制。

`combine()` 只有全部绘制成功后才更新结果；再次合成失败时，上一次成功结果仍可输出。模糊在副本上计算，不会修改原图，也不会因平铺而反复累积。

下一步：[图片工具与输出](/aj-image/utilities-cn/) · [返回首页](/aj-image/cn/)
