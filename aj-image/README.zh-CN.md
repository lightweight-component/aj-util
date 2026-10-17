<h1 align="center">aj-image</h1>
<h3 align="center">小型的 Java 图片处理包</h3>

<div style="text-align: center;">

![Java Version](https://img.shields.io/badge/Java-8-blue)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-image)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-image)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-image)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)
</div>

<hr />
aj-image 是一个用于 Java 服务端图片处理与合成的轻量级工具，提供图片、文字、矩形三种素材，可以用于海报拼合、图片水印等场景。合成入口是 `ImageContainer`，此外还提供图片格式转换、缩略图、圆角和像素矩阵等工具。

## Maven 引入

支持 Java 8 及以上版本。当前发布版本为 `1.0`，已发布到 Maven 中央仓库，在项目的 `pom.xml` 中加入：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-image</artifactId>
    <version>1.0</version>
</dependency>
```

通常无需额外配置仓库地址。

## 能够做什么？

- 图片：定位、缩放、旋转、圆角、透明度和模糊。
- 文字：字体、字号、颜色、透明度、旋转、删除线、基线定位，以及超宽时缩小字号或添加省略号。
- 矩形：尺寸、纯色、渐变、透明度、圆角和边框；设置边框后只绘制轮廓，不同时填充内部。
- 三种素材都支持水平居中、水平定位方向和重复平铺。绘制方向用于决定坐标如何定位，不是图片镜像或文字倒序。

## 图片工具包：不合成图片也能单独使用

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

## 当前限制

- 可以绘制预先拆分好的多行文字，但还不能根据指定宽度自动换行。启用 `setAutoBreakLine(true)`
  时，仍需提供 `breakLineElements`，并设置每行的位置和样式；`setSpace()` 暂不生效。
- 图片元素需要先通过 `setImage()` 提供 `BufferedImage`；仅设置 `setImgUrl()`
  不会自动加载图片。背景图入口 `ImageContainer.setBackgroundImage(String, ...)` 则支持本地路径和 HTTP/HTTPS
  地址，但网络读取没有可配置的超时或身份认证，请勿直接传入不可信的地址。
- 图片模糊采用加权平滑，并非严格的高斯模糊；只平滑颜色，保留原有透明度。

## 参考项目

基于 https://gitee.com/dromara/image-combiner 源码重构修改而来，鸣谢！
