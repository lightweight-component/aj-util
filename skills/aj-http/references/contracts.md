# Current HTTP contracts and limitations

Inspect bodies before proposing code; this is not a claim that all edge cases are fixed.

## Request and method helpers

- `Request(HttpMethod, String)` and method subclasses' `(HttpMethod, String)` constructors do not send. Configure content type/data/timeouts, init(callback), initData() for body, connect(), then disconnect when managing the connection yourself.
- Get(String, Consumer), Delete(String, Consumer), and data-taking Post/Put constructors send immediately. Head(String) does not send; call init/connect. Requests are mutable and should be per operation.
- Defaults: connect 10,000 ms, read 15,000 ms. Configure before init or on HttpURLConnection in the callback. Only HTTP/HTTPS accepted. Callback must not start I/O. HttpMethod enum membership does not mean HttpURLConnection supports every verb (e.g. PATCH).
- Byte data sends unchanged. Other setData overloads require content type first. JSON String path checks only initial {/[ then sends JSON bytes, or parses for form conversion. setDataStr handles query-like text. Map/bean serialize to JSON or form; only form values are encoded, not keys. Multipart Map path throws UnsupportedOperationException.
- BasePost constructor String dispatch only recognizes untrimmed JSON-looking strings or strings containing '='; others can silently yield no body. Use Map or explicit UTF-8 bytes.
- Get.text returns text; Get.api/Post.api/Post.form/Delete.api return Maps, Put.api returns Response. Bean variants return null on non-success. Shortcuts do not all enforce status or expose cleanup. Delete.api requires token or Consumer; typed null may be needed to resolve overloaded calls.
- Head init disables redirects (callback can override). get302redirect reads Location regardless of code. is404 can throw. getFileSize returns long but calls int getContentLength, so large/unknown sizes may be -1.

## Response, errors, resources and logs

- Success is 200–299; non-2xx uses error stream if present. 3xx without automatic redirects may have no text. Response.isOk(Map) is unrelated business logic checking status.toString() == "1".
- connect catches checked IOException into isOk=false/ex; status may be null. Initialization, body writes, callbacks, JSON conversion and unchecked read failures may throw. Do not assume all errors appear in Response.ex.
- Local DataReader uses UTF-8, normalizes line endings and appends final platform newline. Request does not trim stored text. TestMultipartPost expects "response-body" without newline, a source/test mismatch; the POM resolves ajaxjs-util:1.3.8, whose contents must be checked separately.
- responseAsJson: nonempty text -> Map, no status/content-type check. responseAsJsonList uses a case-sensitive header-map lookup for Content-Type; can reject valid responses. responseAsBean gates success; responseAsXML does not. Empty text returns null except JSON-list type check happens first. Parse failures propagate.
- Input consumer bypasses text conversion and consumes synchronously; Request closes input after callback, including error stream. Response.in is not populated. initData closes/flushes output, callback writer wins over data. Request itself does not disconnect.
- Head.gzip only decodes a single case-insensitive gzip token after trim; chained encodings unchanged. IOException during GZIPInputStream construction is wrapped in UncheckedIOException; later stream reads can throw checked IOException, including corrupt/truncated payload failures. No automatic gzip decode.
- printLog logs URL, UTF-8 request body and response prefix (460 chars), at info and in MDC. Truncation is not redaction. Review secrets/PII exposure.

## Multipart

MultipartPost.upload(url, Map<String,?>, callback) supports fields/File values; uploadFile overloads accept File or filename plus byte[]/InputStream. It generates boundary, reapplies matching Content-Type after callback, forces 8192 chunked streaming and disables redirects. Output/input response streams close and connection disconnects in finally. Caller-supplied upload InputStream stays open; caller must close it. Validation/write/callback failures throw; checked read failures can be Response.ex.

MultipartWriter only encodes, does not send. One write call emits the entire body and final boundary; aggregate fields in one Map, never append with repeated writes. Caller output is not closed/flushed; caller input is not closed. Internally opened file streams close. Raw streams/bytes require dedicated overloads, not raw Map keys. UTF-8 headers/text; null values become empty text; CR/LF rejected in header names/filenames, quotes/backslashes escaped. Boundary syntax [A-Za-z0-9_-]{1,70}; empty Map rejected. I/O -> UncheckedIOException.

## Downloads

HttpFileDownload constructors accept single String URL with String/Path target, or String[] with directory. download/downloadAsync for single; downloadAll sequential; downloadAllAsync returns CompletableFuture<Void>. Async uses shared four daemon threads with unbounded queue; wait/handle failure, do not assume daemon tasks keep process alive. No result filenames or executor API. Supplied URL array is retained, not copied.

Explicit file target wins; directory name priority Content-Disposition (UTF-8 filename* first), URL path, fallback. String trailing slash/backslash marks directory, existing directory recognized; nonexisting Path is file. Batch always directory. Basename sanitization/control removal is not a filesystem sandbox. 2xx only, fixed 10/30-second timeouts, redirects enabled. Stream to temporary sibling, move REPLACE_EXISTING (not ATOMIC_MOVE), close streams/disconnect and attempt temp cleanup. No byte limit, SSRF protection or collision policy; enforce application URL/destination rules.

## Proxies

CallHandler.create(interface) implements @GET/@POST/@PUT/@DELETE; returns String, Map, otherwise responseAsBean(returnType). POST/PUT body is first Map argument. @POST defaults FORM, @PUT JSON_BODY. Class then method init Consumers; cached instances require public no-arg construction and safe reuse.

Path substitution uses reflection parameter names (-parameters), raw toString without encoding; URL joining is simple concatenation. @HEAD/@Header/@RawBody/@FormData/@Url.config have no dispatch support. FILE_UPLOAD routes to unsupported Map multipart path, not MultipartPost. create2 invokes BaseCall.init but lifecycle/default methods are not handled; not a usable initialization flow. Object methods, generic collections and raw Response handling are not specially supported. No explicit disconnection/status-aware return contract; use direct Request for critical control.

## TLS

SkipSSL.init changes JVM-wide socket factory AND hostname verifier to trust-all. setSSL_Ignore disables both per connection. getSocketFactory also trusts all servers even with client key managers, and can return null on errors. loadCert is package-private. Do not market these as production trust-store/mTLS security configuration. Preserve real trust/hostname checks; validate untrusted URLs and redirect destinations.
