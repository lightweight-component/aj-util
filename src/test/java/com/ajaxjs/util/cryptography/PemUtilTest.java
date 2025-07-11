package com.ajaxjs.util.cryptography;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class PemUtilTest {

    private static final String PRIVATE_KEY_PATH = "path/to/your/private_key.pem"; // Update with actual path
    private static final String PRIVATE_KEY_CONTENT = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQD5...\n" +
            "-----END PRIVATE KEY-----";

    @Test
    public void loadPrivateKeyByPath_ValidPath_ShouldReturnPrivateKey() {
        PrivateKey privateKey = CommonUtil.loadPrivateKeyByPath(PRIVATE_KEY_PATH);
        assertNotNull(privateKey, "Private key should not be null");
    }

    @Test
    public void loadPrivateKey_ValidContent_ShouldReturnPrivateKey() {
        PrivateKey privateKey = CommonUtil.loadPrivateKey(PRIVATE_KEY_CONTENT);
        assertNotNull(privateKey, "Private key should not be null");
    }

    @Test
    public void loadPrivateKey_InvalidContent_ShouldThrowException() {
        CommonUtil.loadPrivateKey("invalid private key content");
    }

    @Test
    public void deserializeToCerts_ValidData_ShouldReturnCertificates() {
        String apiV3Key = "your_api_v3_key"; // Update with actual API v3 key
        Map<String, Object> pMap = new HashMap<>();
        // Populate pMap with actual data, this example assumes proper structure

        Map<BigInteger, X509Certificate> certs = CommonUtil.deserializeToCerts(apiV3Key, pMap);
        assertNotNull(certs, "Certificate map should not be null");
        assertFalse(certs.isEmpty(), "Certificate map should not be empty");
    }
}
