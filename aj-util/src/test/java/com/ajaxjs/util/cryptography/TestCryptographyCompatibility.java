package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class TestCryptographyCompatibility {
    @Test
    void legacySymmetricConvenienceMethodsRoundTrip() {
        String text = "Legacy encryption 测试";
        String password = "compatibility-password";
        byte[] tripleDesKey = "123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

        assertEquals(text, Cryptography.AES_decode(Cryptography.AES_encode(text, password), password));
        assertEquals(text, Cryptography.DES_decode(Cryptography.DES_encode(text, password), password));
        assertEquals(text, Cryptography.tripleDES_decode(
                Cryptography.tripleDES_encode(text, tripleDesKey), tripleDesKey));
    }

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
    void legacyPbeDecoderReadsHistoricalCiphertext() throws Exception {
        String password = "legacy-password";
        byte[] salt = "12345678".getBytes(StandardCharsets.US_ASCII);
        int iterations = 100;
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKey key = SecretKeyFactory.getInstance(Constant.PBE_LEGACY).generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(Constant.PBE_LEGACY);
        cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, iterations));
        byte[] encrypted = cipher.doFinal("legacy content".getBytes(StandardCharsets.UTF_8));
        keySpec.clearPassword();

        assertEquals("legacy content", Cryptography.PBE_legacy_decode(encrypted, password, salt, iterations));
        assertThrows(IllegalArgumentException.class,
                () -> Cryptography.PBE_legacy_decode(encrypted, password, new byte[7], iterations));
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
        assertThrows(RuntimeException.class,
                () -> SecretKeyMgr.getSecretKey("missing-algorithm", 128, random));
    }

    @Test
    void certificateAesGcmDecryptsValidPayload() {
        byte[] key = new byte[32];
        byte[] nonce = "123456789012".getBytes(StandardCharsets.US_ASCII);
        byte[] aad = "certificate".getBytes(StandardCharsets.UTF_8);
        Cryptography encrypt = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.ENCRYPT_MODE);
        encrypt.setKeyData(key);
        encrypt.setSpec(new javax.crypto.spec.GCMParameterSpec(128, nonce));
        encrypt.setAssociatedData(aad);
        encrypt.setDataStr("certificate payload");
        String ciphertext = encrypt.doCipherAsBase64Str();

        assertEquals("certificate payload",
                CertificateUtils.aesDecryptToString(key, aad, nonce, ciphertext));
        assertEquals("certificate payload",
                CertificateUtils.aesDecryptToString(key, "certificate", "123456789012", ciphertext));
    }
}
