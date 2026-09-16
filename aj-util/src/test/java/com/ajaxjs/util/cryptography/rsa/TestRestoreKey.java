package com.ajaxjs.util.cryptography.rsa;

import org.junit.jupiter.api.Test;

import java.security.Key;

class TestRestoreKey {
    @Test
    void testRestore() {
        Key key = RestoreKey.loadPrivateKey("C:\\Users\\Z\\Downloads\\WXCertUtil\\cert\\1623777099_20251021_cert\\apiclient_key.pem");

        System.out.println(key.getAlgorithm());
    }
}
