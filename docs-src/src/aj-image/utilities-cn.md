---
title: 图片工具与输出
layout: layouts/aj-util-cn.njk
alternate: /utilities/
---

# 图片工具与输出

## 工具一览

`com.ajaxjs.image.utils` 不只是合成器的辅助代码，也提供可以独立使用的图片处理工具。

| 类 | 能做什么 | 使用时注意 |
| --- | --- | --- |
| `ImageConvert` | 在 JPG、PNG、GIF、BMP 等编码格式间转换；返回文件字节、写入输出流或保存文件；可设置编码质量 | 先调用 `setImageFormat()`；文件扩展名不会自动决定格式。JPG、BMP 输出不保留透明度 |
| `ImageEffect` | 按倍数缩放、指定尺寸缩放、缩略图、圆角、扩展画布和补成正方形 | 通过 `getOutput()` 取结果；多个效果不会自动连续叠加。扩展画布新增区域为黑色，补正方形时原图放在左上角 |
| `ImageMatrix` | 提取 RGB、BGR、RGBA、灰度原始像素字节；裁剪三通道矩阵；从矩阵创建图片 | 原始像素不是 PNG/JPG 文件字节；`cutMatrix()` 仅适用于 RGB/BGR；NV21 未实现，`createRGBAImage()` 当前会丢弃透明度 |
| `ImageTypeHandler` | 读取图片宽高、格式信息，解码整图或指定区域，可指定读取器支持的目标像素类型 | 输入完整图片字节和格式名；只处理第一帧，一次解码后不能复用。用 try-with-resources 管理资源 |
| `ImageEncoder` | 直接把 `BufferedImage` 编码成文件字节、写入流，或保存已编码字节 | 是转换器与合成器共用的编码工具；`writeFile()` 接收编码后的字节，不会自行转换图片 |
| `ImageBase` | 提供图片读取、复制和处理结果输出的公共能力 | 抽象基类，不直接实例化；`ImageConvert`、`ImageEffect`、`ImageMatrix` 可从内存图片、完整图片字节、本地文件或 HTTP/HTTPS 地址创建 |

### 示例：转换格式

下面返回的是完整 PNG 文件内容，可以保存为文件或用于上传，并不是原始像素数组。

```java
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageConvert;

byte[] pngBytes = new ImageConvert("input.jpg")
        .setImageFormat(ImageFormat.PNG)
        .convert();
```

### 示例：缩略图再加圆角

每个效果方法都读取构造时的输入图片。要把上一步结果交给下一步处理，需要用 `getOutput()` 创建新的处理器，而不是直接串联两个效果方法。

```java
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageEffect;
import java.awt.image.BufferedImage;

BufferedImage thumbnail = new ImageEffect("input.jpg")
        .thumbnail(320, 240, 2.0)
        .getOutput();

new ImageEffect(thumbnail)
        .roundCorner(24)
        .outputAsFile(ImageFormat.PNG, 1.0f, "thumbnail.png");
```

缩略图不会放大原图，也不保证正好填满指定宽高。保存到文件时，父目录需要预先存在。若改为读取网络图片，请自行限制来源和图片大小；当前网络读取入口不提供可配置的超时或身份认证。

## 使用时需要了解的行为

- `resize()` 保留索引图片的调色板和透明度；等比例缩放细长图片时，宽高至少保留一个像素。无法识别的图片字节或网络图片会在读取时直接报错。
- `getMatrix(MatType.RGBA)` 返回普通 RGBA 颜色值，即使原图内部使用预乘透明度，也不会把 RGB 再压暗一次。
- `ImageConvert.setQuality(null)` 使用编码器默认质量；手动指定的质量必须在 `0` 到 `1` 之间，`0` 也是有效的明确质量值。
- 缩略图不会超过指定尺寸，也不会放大原图；长宽比较极端时使用居中裁剪。原图带透明通道时，`scale()` 会保留透明度。
- 区域解码会按选中区域返回图片，指定输出像素类型时也一样。
- `getMatrix(MatType.BGR)` 按蓝、绿、红的顺序返回字节。NV21 转换尚未实现，调用时会抛出 `UnsupportedOperationException`。

## 输出与失败处理

格式转换、效果输出和合成器使用相同的图片编码逻辑，支持 JPG、PNG、GIF 和 BMP；透明图片输出为 JPG 或 BMP 时，主动转成不透明 RGB，避免不同 Java 版本的编码器产生不同结果。

保存文件时先完成编码，再写入目标同目录的临时文件，成功后替换目标。编码或临时写入失败不会提前清空旧文件。替换优先使用原子移动，不支持时使用普通移动，其故障保证取决于文件系统。

`combine()` 只有全部绘制成功后才更新结果。后续合成失败时，仍可输出上次成功的图片；如果第一次合成就失败，则不能输出半成品。编码错误会保留原始异常原因，方便排查。

模糊在独立图片上计算，不会改写输入图片；同一图片重复绘制或平铺时，模糊不会逐次累积。

`ImageTypeHandler` 解码完成或失败后都会释放读取器和输入流。只读取宽高等元数据时，请使用 try-with-resources，确保没有执行解码也能释放资源。`ImageConvert.convert(OutputStream)` 不关闭调用方传入的流。

模块测试提供了这些行为的小型可运行示例。

[源代码](https://github.com/lightweight-component/aj-util/tree/master/aj-image)

相关：[图片合成](/aj-image/composition-cn/) · [返回首页](/aj-image/cn/)
