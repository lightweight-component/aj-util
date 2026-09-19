---
title: HTTP 高级用法索引
layout: layouts/aj-util-cn.njk
alternate: /http_request/advanced-usage/
---

# HTTP 高级用法索引

本页为已有书签保留。原先将多个主题混在一起的高级用法已拆分，便于按任务查找，并缩短每页的阅读路径。

| 需求 | 文档 |
| --- | --- |
| 上传 multipart 表单或下载文件 | [上传与下载](/aj-http/http_request/Transfer-cn/) |
| 使用注解接口、流式处理二进制响应、处理 gzip，或了解 TLS/日志限制 | [注解代理、流与 TLS](/aj-http/http_request/ProxySecurity-cn/) |
| 控制状态、请求体、错误和连接生命周期 | [请求生命周期与响应](/aj-http/http_request/Base-cn/) |
| 使用同步 GET/POST/PUT/DELETE/HEAD 快捷方法 | [HTTP 方法辅助类](/aj-http/http_request/Get-cn/) |

新的集成建议从 [AJ HTTP 简介](/aj-http/cn/) 开始，再选择与操作相对应的页面。当集成需要明确资源所有权或安全策略时，仍建议使用直接的 `Request` 生命周期。
