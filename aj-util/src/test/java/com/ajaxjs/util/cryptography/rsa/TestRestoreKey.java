package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.io.ResourceHelper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

class TestRestoreKey {
    @Test
    void testRestore() {
        InputStream stream2 = new ResourceHelper("\\1623777099_20251021_cert\\apiclient_key.pem").getStream();
        Key key = RestoreKey.loadPrivateKey(stream2);

        System.out.println(key.getAlgorithm());
    }

    @Test
    void testInvalidPrivateKeyIsNotIncludedInException() {
        String invalidKey = "c2VjcmV0LXByaXZhdGUta2V5";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RestoreKey.restorePrivateKey(invalidKey));

        assertFalse(exception.getMessage().contains(invalidKey));
        assertNotNull(exception.getCause());
    }
}
