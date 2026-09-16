package com.ajaxjs.util.cryptography.rsa;

import org.junit.jupiter.api.Test;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

class TestRestoreKey {
    @Test
    void testRestore() {
        Key key = RestoreKey.loadPrivateKey("C:\\Users\\Z\\Downloads\\WXCertUtil\\cert\\1623777099_20251021_cert\\apiclient_key.pem");

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
