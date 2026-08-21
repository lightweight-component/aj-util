package com.ajaxjs.s3client.signer_v4;

import com.ajaxjs.s3client.BaseS3ClientSigV4;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class TestSignerInternals {
    @Test
    void headerValidationAndNormalizationSupportCanonicalRules() {
        assertDoesNotThrow(() -> CanonicalHeaders.validateHeader("X-Amz-Date", " value "));
        assertEquals("one two", CanonicalHeaders.normalizeHeaderValue("  one\t  two "));
        assertThrows(IllegalArgumentException.class,
                () -> CanonicalHeaders.validateHeader("Bad Header", "value"));
        assertThrows(IllegalArgumentException.class,
                () -> CanonicalHeaders.validateHeader("Host", "value\r\nInjected"));
    }

    @Test
    void percentDecoderDecodesUtf8AndRejectsMalformedEscapes() {
        assertEquals("中 文+", CanonicalRequest.decodePercent("%E4%B8%AD%20%E6%96%87+"));
        assertThrows(IllegalArgumentException.class, () -> CanonicalRequest.decodePercent("%2"));
        assertThrows(IllegalArgumentException.class, () -> CanonicalRequest.decodePercent("%GG"));
    }

    @Test
    void nonS3PathsAreNormalizedWhileS3PathsPreserveSegments() {
        CanonicalRequest request = new CanonicalRequest("GET", "/a/../b//c");
        assertEquals("/a/../b//c", request.getNormalizePath("s3"));
        assertEquals("/b/c", request.getNormalizePath("glacier"));
    }

    @Test
    void signerBuildSupportsS3AndGlacierAndRejectsMissingInputs() throws Exception {
        SignBuilder signer = new SignBuilder(new AwsCredentials("access", "secret"), "auto")
                .header("X-Amz-Date", "20260816T000000Z")
                .header("Host", "example.test");
        CanonicalRequest request = new CanonicalRequest("GET", new URI("https://example.test/object"));

        assertTrue(signer.getS3Signature(request, BaseS3ClientSigV4.EMPTY_SHA256)
                .contains("/auto/s3/aws4_request"));
        assertTrue(signer.getGlacierSignature(request, BaseS3ClientSigV4.EMPTY_SHA256)
                .contains("/auto/glacier/aws4_request"));
        assertThrows(IllegalArgumentException.class,
                () -> signer.build(null, BaseS3ClientSigV4.EMPTY_SHA256, "s3"));
        assertThrows(IllegalArgumentException.class, () -> new SignBuilder(null, "auto"));
        assertThrows(IllegalArgumentException.class,
                () -> new SignBuilder(new AwsCredentials("access", "secret"), " "));
    }
}
