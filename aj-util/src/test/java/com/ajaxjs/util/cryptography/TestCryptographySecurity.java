package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TestCryptographySecurity {
    @Test
    void testPbeRoundTripAndRandomNonce() {
        byte[] salt = Cryptography.initSalt();
        byte[] first = Cryptography.PBE_encode("secret", "strong password", salt, Cryptography.MIN_PBE_ITERATIONS);
        byte[] second = Cryptography.PBE_encode("secret", "strong password", salt, Cryptography.MIN_PBE_ITERATIONS);

        assertEquals("secret", Cryptography.PBE_decode(first, "strong password", salt, Cryptography.MIN_PBE_ITERATIONS));
        assertFalse(Arrays.equals(first, second), "Each encryption must use a fresh GCM nonce");
        assertEquals(Cryptography.PBE_SALT_LENGTH, salt.length);
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
        assertNotNull(exception.getCause());
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
        assertNotNull(exception.getCause());
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

    @Test
    void testPbeRejectsTamperedCiphertextAndWrongPassword() {
        byte[] salt = Cryptography.initSalt();
        byte[] encrypted = Cryptography.PBE_encode(
                "authenticated content",
                "correct password",
                salt,
                Cryptography.MIN_PBE_ITERATIONS
        );
        byte[] tampered = encrypted.clone();
        tampered[tampered.length - 1] ^= 1;

        IllegalArgumentException tamperError = assertThrows(
                IllegalArgumentException.class,
                () -> Cryptography.PBE_decode(tampered, "correct password", salt, Cryptography.MIN_PBE_ITERATIONS)
        );
        IllegalArgumentException passwordError = assertThrows(
                IllegalArgumentException.class,
                () -> Cryptography.PBE_decode(encrypted, "wrong password", salt, Cryptography.MIN_PBE_ITERATIONS)
        );

        assertTrue(tamperError.getMessage().startsWith("Authentication failed"));
        assertTrue(passwordError.getMessage().startsWith("Authentication failed"));
    }

    @Test
    void testRsaPemRoundTripEncryptionAndSignature() {
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        KeyPair pair = keyMgr.generateKeyPair();
        String publicPem = keyMgr.getPublicToPem();
        String privatePem = keyMgr.getPrivateToPem();
        byte[] plaintext = "RSA round trip".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(pair.getPublic().getEncoded(), KeyMgr.restoreKey(true, publicPem).getEncoded());
        assertArrayEquals(pair.getPrivate().getEncoded(), KeyMgr.restorePrivateKey(privatePem).getEncoded());

        byte[] encrypted = KeyMgr.publicKeyEncrypt(plaintext, publicPem);
        assertArrayEquals(plaintext, KeyMgr.privateKeyDecrypt(encrypted, privatePem));

        byte[] signature = new DoSignature(Constant.SHA256_RSA)
                .setData(plaintext)
                .setPrivateKey(pair.getPrivate())
                .sign();
        assertTrue(new DoVerify(Constant.SHA256_RSA)
                .setData(plaintext)
                .setSignatureData(signature)
                .setPublicKey(pair.getPublic())
                .verify());

        byte[] changed = "RSA round trip!".getBytes(StandardCharsets.UTF_8);
        assertFalse(new DoVerify(Constant.SHA256_RSA)
                .setData(changed)
                .setSignatureData(signature)
                .setPublicKey(pair.getPublic())
                .verify());
    }

    @Test
    void testTransformationAcceptsRawAesKeyData() {
        byte[] key = new byte[16];
        byte[] nonce = new byte[12];
        byte[] plaintext = "GCM content".getBytes(StandardCharsets.UTF_8);

        Cryptography encrypt = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.ENCRYPT_MODE);
        encrypt.setKeyData(key);
        encrypt.setSpec(new GCMParameterSpec(128, nonce));
        encrypt.setData(plaintext);
        byte[] encrypted = encrypt.doCipher();

        Cryptography decrypt = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        decrypt.setKeyData(key);
        decrypt.setSpec(new GCMParameterSpec(128, nonce));
        decrypt.setData(encrypted);

        assertArrayEquals(plaintext, decrypt.doCipher());
    }

    @Test
    void testCipherRejectsMissingKeyClearly() {
        Cryptography missingKey = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        missingKey.setData(new byte[0]);
        assertEquals(
                "Cipher key is required.",
                assertThrows(IllegalStateException.class, missingKey::doCipher).getMessage()
        );
    }

    @Test
    void testCipherRejectsMissingDataClearly() {
        Cryptography missingData = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        missingData.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        assertEquals(
                "Cipher data is required.",
                assertThrows(IllegalStateException.class, missingData::doCipher).getMessage()
        );
    }

    @Test
    void testCipherRejectsMissingAlgorithmClearly() {
        Cryptography missingAlgorithm = new Cryptography(null, Cipher.ENCRYPT_MODE);
        missingAlgorithm.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        missingAlgorithm.setData(new byte[0]);

        assertEquals(
                "Cipher algorithm is required.",
                assertThrows(IllegalStateException.class, missingAlgorithm::doCipher).getMessage()
        );
    }
}
