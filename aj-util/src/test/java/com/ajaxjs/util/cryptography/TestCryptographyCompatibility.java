package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static com.ajaxjs.util.cryptography.TestCryptographyLegacy.*;
import static org.junit.jupiter.api.Assertions.*;

class TestCryptographyCompatibility {
    @Test
    void configuredCipherSupportsStringBase64AndHexResults() {
        SecretKey key = new SecretKeySpec(new byte[16], Constant.AES);
        Cryptography encrypt = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        encrypt.setSecretKey(key);
        encrypt.setDataStr("content");
        String base64 = encrypt.doCipherAsBase64Str();

        Cryptography decrypt = new Cryptography(Constant.AES, Cipher.DECRYPT_MODE);
        decrypt.setSecretKey(key);
        decrypt.setDataStrBase64(base64);
        assertEquals("content", decrypt.doCipherAsStr());

        encrypt.setDataStr("content");
        assertFalse(encrypt.doCipherAsHexStr().isEmpty());
        assertEquals(Constant.AES, encrypt.getKeyAlgorithm());
    }

    @Test
    void secretKeyManagerGeneratesDerivesSeedsAndEncodesKeys() {
        SecureRandom random = SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, "seed");
        SecretKey generated = SecretKeyMgr.getSecretKey(Constant.AES, 128, random);
        String encoded = SecretKeyMgr.getSecretKeyAsStr(Constant.AES, 128, random);
        PBEKeySpec spec = new PBEKeySpec("password".toCharArray(), new byte[16], 100_000, 128);

        assertEquals(16, generated.getEncoded().length);
        assertEquals(16, new Base64Utils(encoded).decode().length);
        assertNotNull(SecretKeyMgr.getSecretKey(Constant.PBE, spec));
        spec.clearPassword();
        assertThrows(RuntimeException.class, () -> SecretKeyMgr.getSecretKey("missing-algorithm", 128, random));
    }
}
