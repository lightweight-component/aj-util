package com.ajaxjs.s3client;

import com.ajaxjs.s3client.factory.AliyunOSS;
import com.ajaxjs.s3client.factory.Backblaze;
import com.ajaxjs.s3client.factory.NeteaseOSS;
import com.ajaxjs.s3client.signer_v4.*;
import com.ajaxjs.s3client.util.S3SigV4Utils;
import com.ajaxjs.s3client.util.URLEncoding;
import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.httpremote.Response;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline unit tests for signing, canonicalization, encoding, and response handling.
 */
class TestS3Core {
    @Test
    void responseStatusUsesTheWholeSuccessRange() {
        Response response = new Response();
        response.setHttpCode(200);
        assertTrue(BaseS3Client.check(response));
        response.setHttpCode(299);
        assertTrue(BaseS3Client.check(response));
        response.setHttpCode(300);
        assertFalse(BaseS3Client.check(response));
        assertFalse(BaseS3Client.check(null));
    }

    @Test
    void sigV2ProvidersKeepKeyAndPayloadOrder() {
        Config config = config("OSS");
        AliyunOSS aliyun = new AliyunOSS();
        aliyun.setConfig(config);
        String sha1 = new HashHelper(HashHelper.HMAC_SHA1, "payload").setKey("secret").hashAsBase64();
        assertEquals("OSS access:" + sha1, aliyun.getAuthSignature("payload"));

        config.setRemark("NOS");
        NeteaseOSS netease = new NeteaseOSS();
        netease.setConfig(config);
        assertEquals("NOS access:" + HashHelper.getHmacSHA256("payload", "secret", false),
                netease.getAuthSignature("payload"));
    }

    @Test
    void urlEncodingPreservesPathSeparatorsAndEncodesComponents() {
        assertEquals("folder/%E4%B8%AD%20%E6%96%87%2B%23.txt",
                URLEncoding.encodePath("folder/中 文+#.txt"));
        assertEquals("a%2Fb%20c", URLEncoding.encodeQueryComponent("a/b c"));
        assertEquals("~", URLEncoding.encodeQueryComponent("~"));
    }

    @Test
    void canonicalQueryIsSortedAndMissingValuesBecomeEmpty() throws Exception {
        CanonicalRequest request = new CanonicalRequest("GET", new URI("https://example.test/a?z=2&a=hello%20world&empty"));
        assertEquals("a=hello%20world&empty=&z=2", request.getNormalizeQuery());
        assertEquals("/a", request.getNormalizePath());
    }

    @Test
    void encodedObjectPathIsCanonicalizedExactlyOnce() throws Exception {
        CanonicalRequest request = new CanonicalRequest("GET",
                new URI("https://example.test/bucket/%E4%B8%AD%20%E6%96%87%23.txt"));
        assertEquals("/bucket/%E4%B8%AD%20%E6%96%87%23.txt", request.getNormalizePath());
    }

    @Test
    void canonicalHeadersAreLowercaseSortedAndWhitespaceNormalized() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Amz-Date", "  20260812T000000Z  ");
        headers.put("Host", " example.test ");
        CanonicalHeaders canonical = CanonicalHeaders.build(headers);
        assertEquals("host;x-amz-date", canonical.getNames());
        assertEquals("host:example.test\nx-amz-date:20260812T000000Z\n", canonical.getCanonicalizedHeaders());
        assertEquals(Optional.of("example.test"), canonical.getFirstValue("HOST"));
    }

    @Test
    void canonicalHeadersRejectInjectionNormalizeTabsAndIgnoreDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("X-ID", "  one\t\t two  ");
            CanonicalHeaders canonical = CanonicalHeaders.build(headers);
            assertEquals("x-id", canonical.getNames());
            assertEquals("x-id:one two\n", canonical.getCanonicalizedHeaders());
        } finally {
            Locale.setDefault(original);
        }
        assertThrows(IllegalArgumentException.class,
                () -> CanonicalHeaders.build(java.util.Collections.singletonMap("Host", "ok\r\nInjected: yes")));
        assertThrows(IllegalArgumentException.class,
                () -> CanonicalHeaders.build(java.util.Collections.singletonMap("Bad Header", "value")));
    }

    @Test
    void canonicalQueryPreservesDuplicatesEmptyValuesAndEncodesOnce() throws Exception {
        CanonicalRequest request = new CanonicalRequest("GET",
                new URI("https://example.test/?foo&bar=qux&a=2&a=1&slash=%2F&plus=+&empty="));
        assertEquals("a=1&a=2&bar=qux&empty=&foo=&plus=%2B&slash=%2F", request.getNormalizeQuery());
        CanonicalRequest malformed = new CanonicalRequest("GET", "/?bad=%2");
        assertThrows(IllegalArgumentException.class, malformed::getNormalizeQuery);
    }

    @Test
    void signingModelsAreImmutableAndSigningDoesNotMutateRequest() throws Exception {
        SignBuilder original = new SignBuilder(new AwsCredentials("access", "secret"), "us-east-1");
        SignBuilder dated = original.header("X-Amz-Date", "20260812T000000Z").header("Host", "example.test");
        assertTrue(original.getMap().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> dated.getMap().put("x", "y"));
        CanonicalRequest request = new CanonicalRequest("GET", new URI("https://example.test/a"));
        String before = request.getNormalizePath();
        dated.getS3Signature(request, BaseS3ClientSigV4.EMPTY_SHA256);
        assertEquals(before, request.getNormalizePath());
    }

    @Test
    void digestHmacHexAndCredentialScopeMatchKnownValues() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                S3SigV4Utils.calcFileSHA256(new byte[0]));
        assertEquals("4869", S3SigV4Utils.toHex("Hi".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals("f09f9880", S3SigV4Utils.toHex("😀".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals(32, S3SigV4Utils.hmacSha256("key".getBytes(java.nio.charset.StandardCharsets.UTF_8), "value").length);
        assertEquals("20260812/auto/s3/aws4_request",
                new CredentialScope("20260812T000000Z", "s3", "auto").get());
    }

    @Test
    void endpointsAndBackblazeDefaultsAreBuiltAsExpected() {
        AliyunOSS client = new AliyunOSS();
        Config config = config("OSS");
        config.setEndPoint("oss.example.test");
        client.setConfig(config);
        assertEquals("https://oss.example.test", client.getEndPoint());
        assertEquals("https://bucket.oss.example.test", client.getFullEndPoint("bucket"));
        assertTrue(new Backblaze().isSetHost());
    }

    @Test
    void endpointAndObjectValidationAreCentralized() {
        TestClient client = new TestClient();
        Config config = config("OSS");
        config.setEndPoint("s3.example.test/");
        client.setConfig(config);
        assertEquals("https://s3.example.test", client.endpoint());
        assertEquals("https://s3.example.test/bucket/folder/%E4%B8%AD%20%E6%96%87%23.txt",
                client.object("bucket", "folder/中 文#.txt"));

        config.setEndPoint("http://s3.example.test");
        assertThrows(IllegalArgumentException.class, client::endpoint);
        config.setEndPoint("https://user:pass@s3.example.test");
        assertThrows(IllegalArgumentException.class, client::endpoint);
        config.setEndPoint("https://s3.example.test/path");
        assertThrows(IllegalArgumentException.class, client::endpoint);
        config.setEndPoint("https://s3.example.test");
        assertThrows(IllegalArgumentException.class, () -> client.object("bad/name", "key"));
        assertThrows(IllegalArgumentException.class, () -> client.object("bucket", "bad\nkey"));
    }

    @Test
    void etagCheckAcceptsQuotedOrUnquotedValuesAndAll2xxStatuses() throws Exception {
        Response response = new Response();
        response.setHttpCode(201);
        response.setConnection(new HeaderConnection("\"abc\""));
        assertTrue(TestClient.etag(response, "abc"));
        response.setConnection(new HeaderConnection("abc"));
        assertTrue(TestClient.etag(response, "ABC"));
        response.setConnection(new HeaderConnection("abc-2"));
        assertFalse(TestClient.etag(response, "abc-2"));
        response.setHttpCode(500);
        assertFalse(TestClient.etag(response, "abc"));
    }

    @Test
    void successfulUploadDoesNotRequireEtagWhenMd5VerificationIsNotApplicable() throws Exception {
        Response response = new Response();
        response.setHttpCode(204);
        response.setConnection(new HeaderConnection(null));
        assertTrue(TestClient.etag(response, null));
    }

    @Test
    void missingConfigurationAndNullPayloadAreRejected() {
        TestClient client = new TestClient();
        assertThrows(IllegalStateException.class, client::endpoint);
        assertThrows(IllegalArgumentException.class, () -> S3SigV4Utils.calcFileSHA256(null));
    }

    @Test
    void defaultBucketOperationsDelegateToExplicitBucketMethods() {
        TestClient client = new TestClient();
        Config config = config("OSS");
        config.setEndPoint("s3.example.test");
        config.setBucketName("default-bucket");
        client.setConfig(config);

        assertTrue(client.putObject("a.txt", new byte[]{1}));
        assertEquals("default-bucket", client.lastBucket);
        assertEquals("a.txt", client.lastObject);
        assertTrue(client.getObject("b.txt"));
        assertEquals("b.txt", client.lastObject);
        assertTrue(client.deleteObject("c.txt"));
        assertEquals("c.txt", client.lastObject);
    }

    @Test
    void packageVisibleValidationHelpersHandleNormalAndInvalidValues() {
        TestClient client = new TestClient();
        Config config = config("OSS");
        config.setEndPoint("s3.example.test");
        client.setConfig(config);

        assertEquals("s3.example.test", client.endpointUri().getHost());
        assertFalse(BaseS3Client.containsControl("folder/file.txt"));
        assertTrue(BaseS3Client.containsControl("bad\nname"));
        assertDoesNotThrow(() -> BaseS3Client.requireText("value", "field"));
        assertThrows(IllegalArgumentException.class, () -> BaseS3Client.requireText(" ", "field"));
    }

    @Test
    void sigV2CanonicalResourceAndHeadersAreBuiltConsistently() throws Exception {
        String now = "Sun, 16 Aug 2026 00:00:00 GMT";
        assertEquals("\n\n" + now + "\n/bucket/folder/a%20b.txt",
                BaseS3ClientSigV2.getCanonicalResource(now, "bucket", "folder/a b.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> BaseS3ClientSigV2.getCanonicalResource(now, "bucket", null));

        AliyunOSS client = new AliyunOSS();
        Config config = config("OSS");
        config.setEndPoint("oss.example.test");
        client.setConfig(config);
        HeaderConnection connection = new HeaderConnection(null);
        client.setRequestHead(now, "payload").accept(connection);
        assertEquals(now, connection.getRequestProperty(BaseS3Client.DATE));
        assertTrue(connection.getRequestProperty(BaseS3Client.AUTHORIZATION).startsWith("OSS access:"));
    }

    @Test
    void sigV4BuilderCanonicalRequestAndHeadersAreInitialized() throws Exception {
        com.ajaxjs.s3client.factory.CloudflareR2 client = new com.ajaxjs.s3client.factory.CloudflareR2();
        Config config = config("unused");
        config.setEndPoint("s3.example.test");
        client.setConfig(config);
        client.setSetHost(true);

        Map<String, String> extra = java.util.Collections.singletonMap("x-amz-acl", "private");
        SignBuilder builder = client.initSignatureBuilder("20260816T000000Z", BaseS3ClientSigV4.EMPTY_SHA256, extra);
        assertEquals("s3.example.test", builder.getMap().get("host"));
        assertEquals("private", builder.getMap().get("x-amz-acl"));
        assertEquals("/bucket", BaseS3ClientSigV4.getCanonicalRequest("GET", "https://s3.example.test/bucket").getPath());
        assertThrows(RuntimeException.class, () -> BaseS3ClientSigV4.getCanonicalRequest("GET", "https://bad host"));

        HeaderConnection connection = new HeaderConnection(null);
        client.setRequestHead("date", "signature", "hash", extra).accept(connection);
        assertEquals("date", connection.getRequestProperty("x-amz-date"));
        assertEquals("signature", connection.getRequestProperty(BaseS3Client.AUTHORIZATION));
        assertEquals("private", connection.getRequestProperty("x-amz-acl"));
    }

    private static Config config(String remark) {
        Config config = new Config();
        config.setRemark(remark);
        config.setAccessKey("access");
        config.setSecretKey("secret");
        config.setRegion("auto");
        return config;
    }

    private static final class TestClient extends BaseS3Client {
        String lastBucket;
        String lastObject;

        String endpoint() {
            return normalizedEndpoint();
        }

        String object(String bucket, String key) {
            return objectUrl(bucket, key);
        }

        static boolean etag(Response response, String hash) {
            return eTagCheck(response, hash);
        }

        public String listBucket() {
            return null;
        }

        public java.util.Map<String, String> listBucketXml() {
            return null;
        }

        public boolean createBucket(String bucketName) {
            return false;
        }

        public boolean deleteBucket(String bucketName) {
            return false;
        }

        public boolean putObject(String bucketName, String objectName, byte[] fileBytes) {
            lastBucket = bucketName;
            lastObject = objectName;
            return true;
        }

        public boolean getObject(String bucketName, String objectName) {
            lastBucket = bucketName;
            lastObject = objectName;
            return true;
        }

        public boolean deleteObject(String bucketName, String objectName) {
            lastBucket = bucketName;
            lastObject = objectName;
            return true;
        }
    }

    private static final class HeaderConnection extends HttpURLConnection {
        private final String etag;

        HeaderConnection(String etag) throws Exception {
            super(new URL("https://example.test"));
            this.etag = etag;
        }

        public String getHeaderField(String name) {
            return "ETag".equalsIgnoreCase(name) ? etag : null;
        }

        public void disconnect() {
        }

        public boolean usingProxy() {
            return false;
        }

        public void connect() {
        }
    }
}
