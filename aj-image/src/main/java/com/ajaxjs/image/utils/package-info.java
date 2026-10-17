/**
 * 图片读取、编码、基础效果以及像素矩阵转换工具。
 * <p>ImageConvert 从源图直接输出编码数据；ImageEffect 与 ImageMatrix 通常先产生处理结果，
 * 再通过 getOutput 或输出方法读取。处理器保存可变状态，连续效果调用是否叠加应查看方法说明。</p>
 * <p>输入文件字节、内存图片和像素矩阵是不同的数据形式，调用时应选择对应入口。</p>
 */
package com.ajaxjs.image.utils;
