package com.ajaxjs.util.cryptography.rsa;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPemUtils {

    @Test
    void convertsRsaKeysToRestorablePem() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
        String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());

        assertTrue(publicPem.startsWith("-----BEGIN PUBLIC KEY-----\n"));
        assertTrue(publicPem.endsWith("\n-----END PUBLIC KEY-----"));
        assertTrue(privatePem.startsWith("-----BEGIN PRIVATE KEY-----\n"));
        assertTrue(privatePem.endsWith("\n-----END PRIVATE KEY-----"));
        assertArrayEquals(pair.getPublic().getEncoded(), RestoreKey.restorePublicKey(publicPem).getEncoded());
        assertArrayEquals(pair.getPrivate().getEncoded(), RestoreKey.restorePrivateKey(privatePem).getEncoded());
    }
}
