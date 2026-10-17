---
title: aj-image
layout: layouts/aj-util-cn.njk
---

# aj-image

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

## 阅读导航

- [图片合成](/aj-image/composition-cn/)：背景、图片、文字和矩形的绘制，以及现有功能限制。
- [合成教程与完整示例](/aj-image/tutorial-cn/)：从简单例子开始，按步骤完成海报合成。
- [图片工具与输出](/aj-image/utilities-cn/)：格式转换、缩略图、圆角、像素矩阵、区域解码及文件保存。

不需要合成图片时，可以直接使用 `utils` 包中的工具。

[源代码](https://github.com/lightweight-component/aj-util/tree/master/aj-image)
