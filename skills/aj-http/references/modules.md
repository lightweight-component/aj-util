# AJ HTTP source map

Paths are relative to the repository root (contains both aj-http/ and aj-util/).

Current `aj-http/pom.xml`: inherited group com.ajaxjs, artifact **aj-net**, version **2.1**, name **aj-http**, parent ajaxjs-parent:1.36, dependency ajaxjs-util:1.3.8. Do not replace the artifact name with aj-http in examples or claim it is the latest release. APIs use Java 8. Default JSON engine needs Jackson databind and datatype-jsr310 at runtime; AJ Util declares both 2.22.1 as provided.

| Source package under com.ajaxjs.util.httpremote | Classes |
| --- | --- |
| root | Request, Get, Post, Put, Delete, Head, BasePost, MultipartPost, MultipartWriter, HttpFileDownload, SkipSSL |
| model | Response, HttpMethod, HttpConstant, PayloadType |
| call | CallHandler, BaseCall, HttpApiConfig, NoConfig, NoOp |
| call.annotation | Url, GET, POST, PUT, DELETE, HEAD, Header, RawBody, FormData (not all implemented by dispatcher) |

## Migration

- HTTP ownership moved from aj-util to aj-http; package namespace retained.
- Request is in **httpremote**, not httpremote.model. Response/enums/constants are in model.
- Use Get.text, not Get.simpleGET. UrlCodec.simpleGET remains a separate AJ Util helper with different behavior.
- Use HttpFileDownload, not removed Get.download/BatchDownload helpers.
- Use MultipartPost to upload and MultipartWriter to encode; do not generate FileUpload/Post.multiPOST examples.
- Use model.Response, not ResponseEntity or ResponseHandler; connection setup is via Consumers, Head helpers and HttpConstant, not SetConnection.
- Get(String) does not exist; Delete.api(String) does not exist; Head's status/size/location methods are instance methods.

## Discoverability

- Implementation: `aj-http/src/main/java/com/ajaxjs/util/httpremote/`.
- Tests: `aj-http/src/test/java/com/ajaxjs/util/httpremote/`, with proxy tests in call/.
- Documentation homes: `docs-src/src/aj-http/index.md` and cn.md.
- Guides: `docs-src/src/aj-http/http_request/{Base,Get,advanced-usage}[-cn].md`.
- Shared menus: `docs-src/src/_includes/layouts/aj-util[-cn].njk`, gated by project == aj-http.
- Redirects: `docs-src/src/_data/legacyRoutes.js` scans the remaining AJ Util pages and new HTTP guides; both historical HTTP prefixes map directly to aj-http.
- Dependency implementation: `aj-util/src/main/java/com/ajaxjs/util/{JsonUtil,MapTool,UrlCodec}.java` and io/DataReader/DataWriter. Maven resolves a versioned dependency, not automatically this sibling source.
