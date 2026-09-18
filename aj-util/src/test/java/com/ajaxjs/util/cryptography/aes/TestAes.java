package com.ajaxjs.util.cryptography.aes;

import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TestAes {
    @Test
    void testAesEncryptLegacy() {
        String result = new AesLegacy("psw").encrypt("content");
        String decrypt = new AesLegacy("psw").decrypt(result);

        assertEquals("content", decrypt);
    }

    @Test
    void aesCbcEncryptsWithGeneratedOrSuppliedIv() {
        AesCbc aes = new AesCbc(new byte[16]);
        AesCipherResult generatedIv = aes.encrypt("CBC content");
        byte[] iv = "1234567890123456".getBytes(StandardCharsets.US_ASCII);
        AesCipherResult suppliedIv = aes.encrypt("CBC content", iv);

        assertEquals(16, generatedIv.getNonce().getResult().length);
        assertEquals("CBC content", aes.decrypt(generatedIv.toBase64(), generatedIv.getNonce().toBase64()));
        assertEquals("CBC content", aes.decrypt(suppliedIv.toBase64(), suppliedIv.getNonce().toBase64()));
        assertThrows(IllegalArgumentException.class, () -> aes.encrypt("CBC content", new byte[15]));
    }

    @Test
    void certificateAesGcmDecryptsValidPayload() {
        byte[] aad = "certificate".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        byte[] nonce = "123456789012".getBytes(StandardCharsets.US_ASCII);

        String ciphertext = new AesGcm(key).encrypt("certificate payload", nonce, aad).toBase64();

        assertEquals("certificate payload", new AesGcm(key).decrypt(ciphertext, nonce, aad));
    }

    @Test
    void testPbeRoundTripAndRandomNonce() {
        byte[] salt = DoCipher.randomBytes(AesPbe.PBE_SALT_LENGTH);
        AesPbe aesPbe = new AesPbe("strong password", salt, AesPbe.MIN_PBE_ITERATIONS);
        byte[] first = aesPbe.encrypt("secret");
        byte[] second = aesPbe.encrypt("secret");

        assertEquals(AesPbe.PBE_SALT_LENGTH, salt.length);
        assertEquals("secret", aesPbe.decrypt(first));
        assertFalse(Arrays.equals(first, second), "Each encryption must use a fresh GCM nonce");
    }

    @Test
    void testPbeRejectsWeakParameters() {
        assertThrows(IllegalArgumentException.class, () -> new AesPbe("password", new byte[8], AesPbe.MIN_PBE_ITERATIONS));
        assertThrows(IllegalArgumentException.class, () -> new AesPbe("password", new byte[AesPbe.PBE_SALT_LENGTH], 1));
        assertThrows(IllegalArgumentException.class, () -> new AesPbe("", new byte[AesPbe.PBE_SALT_LENGTH], AesPbe.MIN_PBE_ITERATIONS));
    }

    @Test
    void testPbeRejectsTamperedCiphertextAndWrongPassword() {
        byte[] salt = DoCipher.randomBytes(AesPbe.PBE_SALT_LENGTH);
        AesPbe aesPbe = new AesPbe("correct password", salt, AesPbe.MIN_PBE_ITERATIONS);
        byte[] encrypted = aesPbe.encrypt("authenticated content");
        byte[] tampered = encrypted.clone();
        tampered[tampered.length - 1] ^= 1;

        IllegalArgumentException tamperError = assertThrows(IllegalArgumentException.class, () -> aesPbe.decrypt(tampered));

        AesPbe wrongPassword = new AesPbe("wrong password", salt, AesPbe.MIN_PBE_ITERATIONS);
        IllegalArgumentException passwordError = assertThrows(IllegalArgumentException.class, () -> wrongPassword.decrypt(encrypted));

        assertTrue(tamperError.getMessage().startsWith("Authentication failed"));
        assertTrue(passwordError.getMessage().startsWith("Authentication failed"));

        assertThrows(IllegalArgumentException.class, () -> aesPbe.decrypt(null));
        assertThrows(IllegalArgumentException.class, () -> aesPbe.decrypt(new byte[27]));
    }

    @Test
    void testTransformationAcceptsRawAesKeyData() {
        byte[] plaintext = "GCM content".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[16];
        byte[] nonce = new byte[12];

        DoCipher encrypt = new DoCipher(AesGcm.AES_GCM, new SecretKeySpec(key, SecretKeyMgr.AES));
        byte[] encrypted = encrypt.doCipher(Cipher.ENCRYPT_MODE, plaintext, new GCMParameterSpec(128, nonce), null).getResult();

        DoCipher decrypt = new DoCipher(AesGcm.AES_GCM, new SecretKeySpec(key, SecretKeyMgr.AES));

        assertArrayEquals(plaintext, decrypt.doCipher(Cipher.DECRYPT_MODE, encrypted, new GCMParameterSpec(128, nonce), null).getResult());
    }

    @Test
    void testAesDecryptPhone2() {
        String sessionKey = "IOv62NY75gNbTYVEe1ogWQ==";
        String iv = "b/+OsOf6+y4Hl6RXJW+CjQ==";
        String ciphertext = "6fxmM2gjyAk5v9mzSnPXw3xv4WTYywHH/JK9A78Zb2K8i9kehzGLd3xalzx8qNgkZ/SG4/kfL8DgpvQBEoygi7K7YNguUW7HNYHkESUiGXId+DGpziBjmxmoPquFZ8N2XF71kn6MYfXVUiwxCHRu5YYlTbKr4IjA2xqKMgAhaK6YsyD1NE9iOH4eYnT9Ky7B54BW0yWVH3NFgkTmEBQTNg==";
        String decryptedText = new AesCbc(sessionKey).decrypt(ciphertext, iv);
        // Add assertions to validate the decrypted text
        System.out.println(decryptedText);
    }
}
