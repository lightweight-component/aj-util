/**
 * 将元素配置绘制到 Java2D 画布的画笔实现。
 * <p>Painter 的泛型参数保证画笔与元素类型匹配，默认方法共享平铺循环。
 * 内置画笔使用独立 Graphics2D 子上下文绘制，保留调用方画笔状态和元素坐标；
 * 图片模糊使用独立副本，不改写输入图片像素。</p>
 */
package com.ajaxjs.image.painter;
