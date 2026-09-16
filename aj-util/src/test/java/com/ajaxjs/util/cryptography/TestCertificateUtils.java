package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.io.ResourceHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestCertificateUtils {
    @Test
    void testGetCert() throws IOException {
        try (InputStream stream = new ResourceHelper("\\1623777099_20251021_cert\\apiclient_cert.pem").getStream()) {
            X509Certificate cert = CertificateUtils.getCert(stream);
            System.out.println(cert);
        }
    }

    @Test
    void testCertificateFieldUnquotingOnlyRemovesSurroundingQuotes() {
        assertEquals("a\"b", CertificateUtils.remove("\"a\"b\""));
        assertEquals("a\"b", CertificateUtils.remove("a\"b"));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.remove(null));
    }
}
