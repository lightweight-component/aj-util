package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class TestSecretKeyMgr {
    @Test
    void secretKeyManagerGeneratesDerivesSeedsAndEncodesKeys() {
        SecureRandom random = SecretKeyMgr.getRandom("seed");
        SecretKey generated = SecretKeyMgr.getSecretKey(random);
        String encoded = SecretKeyMgr.getSecretKeyAsStr(SecretKeyMgr.AES, 128, random);
        PBEKeySpec spec = new PBEKeySpec("password".toCharArray(), new byte[16], 100_000, 128);

        assertEquals(16, generated.getEncoded().length);
        assertEquals(16, new Base64Utils(encoded).decode().length);
        assertNotNull(SecretKeyMgr.getSecretKey(spec));
        spec.clearPassword();
        assertThrows(RuntimeException.class, () -> SecretKeyMgr.getSecretKey("missing-algorithm", 128, random));
    }
}
