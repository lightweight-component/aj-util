---
title: Advanced HTTP Guide
layout: layouts/aj-util.njk
alternate: /http_request/advanced-usage-cn/
---

# Advanced HTTP guide

This page is retained for existing bookmarks. The former all-in-one advanced guide has been split so each topic has a clearer ownership and a shorter reading path.

| Need | Guide |
| --- | --- |
| Upload a multipart form or download a file | [Uploads and downloads](/aj-http/http_request/Transfer/) |
| Use annotation interfaces, stream binary responses, handle gzip, or understand TLS/logging limits | [Annotation proxy, streams and TLS](/aj-http/http_request/ProxySecurity/) |
| Control status, request bodies, errors, and connection lifetime | [Request lifecycle and responses](/aj-http/http_request/Base/) |
| Use synchronous GET/POST/PUT/DELETE/HEAD shortcuts | [HTTP method helpers](/aj-http/http_request/Get/) |

For new integrations, start with the [AJ HTTP overview](/aj-http/) and choose the guide matching the operation. The direct `Request` lifecycle remains the recommended path when an integration needs explicit resource ownership or security policy.
