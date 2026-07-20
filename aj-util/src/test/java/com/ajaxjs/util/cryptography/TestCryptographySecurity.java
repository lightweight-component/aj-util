package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TestCryptographySecurity {
    @Test
    void testPbeRoundTripAndRandomNonce() {
        byte[] salt = Cryptography.initSalt();
        byte[] first = Cryptography.PBE_encode("secret", "strong password", salt, Cryptography.MIN_PBE_ITERATIONS);
        byte[] second = Cryptography.PBE_encode("secret", "strong password", salt, Cryptography.MIN_PBE_ITERATIONS);

        assertEquals("secret", Cryptography.PBE_decode(first, "strong password", salt, Cryptography.MIN_PBE_ITERATIONS));
        assertFalse(java.util.Arrays.equals(first, second));
    }

    @Test
    void testPbeRejectsWeakParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> Cryptography.PBE_encode("secret", "password", new byte[8], Cryptography.MIN_PBE_ITERATIONS));
        assertThrows(IllegalArgumentException.class,
                () -> Cryptography.PBE_encode("secret", "password", new byte[Cryptography.PBE_SALT_LENGTH], 1));
    }

    @Test
    void testAeadAuthenticationFailureIsClassifiedCorrectly() {
        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        cryptography.setSpec(new GCMParameterSpec(128, new byte[12]));
        cryptography.setData(new byte[16]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, cryptography::doCipher);
        assertTrue(exception.getMessage().startsWith("Authentication failed"));
    }

    @Test
    void testRejectsWeakRsaKeySize() {
        assertThrows(IllegalArgumentException.class, () -> new KeyMgr(Constant.RSA, 1024).generateKeyPair());
    }

    @Test
    void testInvalidPrivateKeyIsNotIncludedInException() {
        String invalidKey = "c2VjcmV0LXByaXZhdGUta2V5";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> KeyMgr.restorePrivateKey(invalidKey));

        assertFalse(exception.getMessage().contains(invalidKey));
    }

    @Test
    void testSignatureStateValidation() {
        DoSignature signature = new DoSignature(Constant.SHA256_RSA);
        assertEquals("Data to sign is required.", assertThrows(IllegalStateException.class, signature::sign).getMessage());

        signature.setData("data".getBytes(StandardCharsets.UTF_8));
        assertEquals("Private key is required.", assertThrows(IllegalStateException.class, signature::sign).getMessage());
    }

    @Test
    void testVerifyStateValidation() {
        DoVerify verify = new DoVerify(Constant.SHA256_RSA);
        assertEquals("Data to verify is required.", assertThrows(IllegalStateException.class, verify::verify).getMessage());

        verify.setData("data".getBytes(StandardCharsets.UTF_8));
        assertEquals("Signature data is required.", assertThrows(IllegalStateException.class, verify::verify).getMessage());
    }
}
