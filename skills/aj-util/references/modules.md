# Module map

Paths below are relative to the repository root (the directory containing both `aj-util/` and `skills/`).
The module POM currently declares `com.ajaxjs:ajaxjs-util:1.3.8` and a Java 8 baseline.
Jackson databind and datatype-jsr310 are declared with provided scope; applications using the default JSON engine must supply them at runtime.

## Current packages

| Package under com.ajaxjs.util | Classes and responsibilities |
| --- | --- |
| root | StrUtil, StringBytes (including hex), Base64Utils, UrlCodec, ConvertBasicValue, ObjectHelper, MapTool, RegExpUtils, RegExpHelper, RandomTools, HashHelper, JsonUtil, XmlHelper, CommonConstant |
| date | DateTools, DateTypeConvert, Formatter |
| io | DataReader, DataWriter, FileHelper, ResourceHelper, ZipHelper, UnzipHelper |
| httpremote | Get, Post, Put, Delete, Head, BasePost, BatchDownload, FileUpload, SkipSSL |
| httpremote.model | Request, Response, HttpConstant, HttpMethod, PayloadType |
| reflect | Clazz, Fields, Methods, Types, NewInstance |
| cryptography | Cryptography, SecretKeyMgr, CertificateUtils, Constant; rsa contains KeyMgr, DoSignature, DoVerify |
| json | JSON engine abstraction and Jackson 2 implementation |
| log | Trace, TextBox, EnableOperationLog |

## Refactoring migration

| Old name or location | Current API |
| --- | --- |
| UrlEncode / UrlHelper | UrlCodec: explicit form or query-value encoding; concatUrl and simpleGET |
| BytesHelper hex conversion | StringBytes.bytesToHex / hexToBytes |
| Resources | ResourceHelper |
| ZipHelper extraction | new UnzipHelper(...).extract() |
| ZIP compression | new ZipHelper(...).zip() |
| httpremote.Request / Response / related models | httpremote.model package |
| MapTool XML conversion | XmlHelper.xmlToMap / mapToXml |
| RandomTools.uuid / uuidStr | uuidV7() or uuidV7(boolean withHyphen) |
| ReflectMethod | Methods |

Do not generate examples using removed README names such as CollUtils, DateHelper, EncodeTools or MessageDigestHelper.
Hashing is handled by HashHelper, not an encryption API.

## Navigation

- Implementation: `aj-util/src/main/java/com/ajaxjs/util/`.
- Tests and fixtures: `aj-util/src/test/java/`, `aj-util/src/test/resources/`.
- Overview: `aj-util/README.md` (English), `aj-util/README.zh-CN.md` (Chinese).
- Known issues and test baseline: `aj-util/to_fix.md`.
- Detailed site documentation: `docs-src/src/`; locate relevant pages by content, since filenames may retain pre-refactor class names.
- Local style conventions: `code-style.md`.

Read the actual constructors and overloads before recommending a usage pattern; static helpers and instance APIs coexist.
