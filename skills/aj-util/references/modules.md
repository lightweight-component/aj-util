# Module map

## Build and source layout

- Maven module: `aj-util`
- Coordinates: `com.ajaxjs:ajaxjs-util`
- Runtime baseline: Java 8+
- Production source: `aj-util/src/main/java`
- Tests: `aj-util/src/test/java`
- Documentation: `docs-src/src`

The Jackson dependencies are declared with `provided` scope. Code that uses `JsonUtil` must ensure compatible Jackson databind and JSR-310 modules are available at runtime.

## Package routing

| Area | Source | Documentation |
|---|---|---|
| Core string, bytes, map, XML, URL, conversion, JSON | `com/ajaxjs/util/*.java` | `docs-src/src/common` |
| Date and time | `com/ajaxjs/util/date` | `docs-src/src/date` |
| Reflection | `com/ajaxjs/util/reflect` | `docs-src/src/reflect` |
| Streams, files, resources, ZIP, commands | `com/ajaxjs/util/io` | `docs-src/src/io` |
| Symmetric crypto, certificates, RSA | `com/ajaxjs/util/cryptography` | `docs-src/src/cryptography` |
| HTTP client helpers | `com/ajaxjs/util/httpremote` | `docs-src/src/http_request` |
| Logging helpers | `com/ajaxjs/util/log` | related source and tests |

## Primary classes

- `StrUtil`: counting, padding, templates, and joining.
- `ConvertBasicValue`: primitive, wrapper, enum, and string conversion.
- `BytesHelper`, `StringBytes`, `Base64Utils`, `HashHelper`: byte and encoding helpers.
- `UrlEncode`, `UrlHelper`: URL and query-string handling.
- `MapTool`: map transforms plus XML conversion.
- `XmlHelper`: hardened DOM and XPath helpers.
- `JsonUtil`: facade over a pluggable `JsonEngine`.
- `DateTypeConvert`, `DateTools`, `Formatter`: legacy and `java.time` conversion/formatting.
- `Clazz`, `Fields`, `Methods`, `Types`, `NewInstance`: reflection utilities.
- `DataReader`, `DataWriter`, `FileHelper`, `Resources`, `ZipHelper`: I/O utilities.
- `Cryptography`, `SecretKeyMgr`, `CertificateUtils`, RSA `KeyMgr`, `DoSignature`, `DoVerify`: cryptographic utilities.

Use `rg` to locate symbols and callers rather than relying on this summary:

```bash
rg -n "methodName|ClassName" aj-util/src/main aj-util/src/test docs-src/src
```
