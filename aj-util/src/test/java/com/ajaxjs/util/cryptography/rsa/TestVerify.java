package com.ajaxjs.util.cryptography.rsa;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class TestVerify {
    @Test
    void verifiesRawAndBase64SignaturesAndRejectsAlteredInputs() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        byte[] data = "signed payload".getBytes(StandardCharsets.UTF_8);
        byte[] signature = new DoSignature(pair.getPrivate()).sign(data);
        DoVerify verifier = new DoVerify(pair.getPublic());

        assertTrue(verifier.verify(data, signature));
        assertTrue(verifier.verify(data, new com.ajaxjs.util.Base64Utils(signature).encodeAsString()));
        assertFalse(verifier.verify("altered payload".getBytes(StandardCharsets.UTF_8), signature));
    }

    @Test
    void rejectsMissingVerificationInput() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        DoVerify verifier = new DoVerify(pair.getPublic());

        assertThrows(IllegalArgumentException.class, () -> verifier.verify((byte[]) null, new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> verifier.verify(new byte[0], (byte[]) null));
        assertThrows(NullPointerException.class, () -> verifier.verify("data", (String) null));
    }
}
