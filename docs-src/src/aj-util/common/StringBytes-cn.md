---
title: StringBytes
description: StringBytes
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /common/StringBytes/
---

# StringBytes

`com.ajaxjs.util.StringBytes` 转换文本与字节。按方向选择构造器：`String` 对应 `getBytes(Charset)` / `getUTF8_Bytes()`；`byte[]` 对应 `getString(Charset)` / `getUTF8_String()`。

无参数的 `getBytes()` / `getString()` 及 null 字符集使用平台默认值，可通过 `showPlatformDefaultCharset()` 查询。协议和文件应明确字符集。

## 十六进制

`bytesToHex(byte[])` 输出大写。`hexToBytes(String)` 接受大小写，空字符串返回空数组；奇数长度或非法字符抛出 `IllegalArgumentException`。十六进制相关方法不接受 null。

```java
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.ObjectHelper;

byte[] utf8 = new StringBytes("你好").getUTF8_Bytes();
String text = new StringBytes(utf8).getUTF8_String();
String hex = StringBytes.bytesToHex(new byte[]{0x1A, 0x2B}); // 1A2B
byte[] restored = StringBytes.hexToBytes(hex);
byte[] combined = ObjectHelper.concat(restored, new byte[]{0x3C});
```

字节拼接位于 `ObjectHelper.concat(byte[], byte[])`。这些转换不会识别字符集或验证业务数据。
