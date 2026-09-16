package com.ajaxjs.util.cryptography;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.ajaxjs.util.cryptography.Aes.pbeDecrypt;
import static com.ajaxjs.util.cryptography.Aes.pbeEncrypt;
import static org.junit.jupiter.api.Assertions.*;

class TestAes {
    @Test
    void testAesEncryptLegacy() {
        String result = Aes.aesEncryptLegacy("content", "psw");
        String decrypt = Aes.aesDecryptLegacy(result, "psw");

        assertEquals("content", decrypt);
    }

    @Test
    void certificateAesGcmDecryptsValidPayload() {
        byte[] aad = "certificate".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        byte[] nonce = "123456789012".getBytes(StandardCharsets.US_ASCII);

        String ciphertext = Aes.aesEncrypt("certificate payload", key, nonce, aad);

        assertEquals("certificate payload", Aes.aesDecrypt(ciphertext, key, nonce, aad));
    }

    @Test
    void testPbeRoundTripAndRandomNonce() {
        byte[] salt = Aes.randomBytes(Aes.PBE_SALT_LENGTH);
        byte[] first = pbeEncrypt("secret", "strong password", salt, Aes.MIN_PBE_ITERATIONS);
        byte[] second = pbeEncrypt("secret", "strong password", salt, Aes.MIN_PBE_ITERATIONS);

        assertEquals(Aes.PBE_SALT_LENGTH, salt.length);
        assertEquals("secret", pbeDecrypt(first, "strong password", salt, Aes.MIN_PBE_ITERATIONS));
        assertFalse(Arrays.equals(first, second), "Each encryption must use a fresh GCM nonce");
    }

    @Test
    void testPbeRejectsWeakParameters() {
        assertThrows(IllegalArgumentException.class, () -> pbeEncrypt("secret", "password", new byte[8], Aes.MIN_PBE_ITERATIONS));
        assertThrows(IllegalArgumentException.class, () -> pbeEncrypt("secret", "password", new byte[Aes.PBE_SALT_LENGTH], 1));
    }

    @Test
    void testAeadAuthenticationFailureIsClassifiedCorrectly() {
        DoCipher cryptography = new DoCipher(Aes.AES_GCM, Cipher.DECRYPT_MODE, new SecretKeySpec(new byte[16], SecretKeyMgr.AES));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cryptography.doCipher(new byte[16],
                new GCMParameterSpec(128, new byte[12]), null));
        assertTrue(exception.getMessage().startsWith("Authentication failed"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testPbeRejectsTamperedCiphertextAndWrongPassword() {
        byte[] salt = Aes.randomBytes(Aes.PBE_SALT_LENGTH);
        byte[] encrypted = pbeEncrypt("authenticated content", "correct password",
                salt, Aes.MIN_PBE_ITERATIONS
        );
        byte[] tampered = encrypted.clone();
        tampered[tampered.length - 1] ^= 1;

        IllegalArgumentException tamperError = assertThrows(IllegalArgumentException.class, () -> pbeDecrypt(tampered, "correct password", salt, Aes.MIN_PBE_ITERATIONS));
        IllegalArgumentException passwordError = assertThrows(IllegalArgumentException.class, () -> pbeDecrypt(encrypted, "wrong password", salt, Aes.MIN_PBE_ITERATIONS));

        assertTrue(tamperError.getMessage().startsWith("Authentication failed"));
        assertTrue(passwordError.getMessage().startsWith("Authentication failed"));
    }

    @Test
    void testTransformationAcceptsRawAesKeyData() {
        byte[] plaintext = "GCM content".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[16];
        byte[] nonce = new byte[12];

        Cryptography encrypt = new Cryptography(Aes.AES_GCM, Cipher.ENCRYPT_MODE);
        encrypt.setKeyData(key);
        encrypt.setSpec(new GCMParameterSpec(128, nonce));
        encrypt.setData(plaintext);
        byte[] encrypted = encrypt.doCipher();

        Aes.aesEncrypt("GCM content", key, nonce);

        Cryptography decrypt = new Cryptography(Aes.AES_GCM, Cipher.DECRYPT_MODE);
        decrypt.setKeyData(key);
        decrypt.setSpec(new GCMParameterSpec(128, nonce));
        decrypt.setData(encrypted);

        assertArrayEquals(plaintext, decrypt.doCipher());
    }

    @Test
    void testAesDecryptPhone2() {
        String sessionKey = "IOv62NY75gNbTYVEe1ogWQ==";
        String iv = "b/+OsOf6+y4Hl6RXJW+CjQ==";
        String ciphertext = "6fxmM2gjyAk5v9mzSnPXw3xv4WTYywHH/JK9A78Zb2K8i9kehzGLd3xalzx8qNgkZ/SG4/kfL8DgpvQBEoygi7K7YNguUW7HNYHkESUiGXId+DGpziBjmxmoPquFZ8N2XF71kn6MYfXVUiwxCHRu5YYlTbKr4IjA2xqKMgAhaK6YsyD1NE9iOH4eYnT9Ky7B54BW0yWVH3NFgkTmEBQTNg==";
        String decryptedText = Aes.aesDecryptCBC(ciphertext, sessionKey, iv);
        // Add assertions to validate the decrypted text
        System.out.println(decryptedText);
    }
}
