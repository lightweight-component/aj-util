package com.ajaxjs.util.cryptography;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestDoCipher {
    private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NONCE = "123456789012".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] AAD = "request-metadata".getBytes(StandardCharsets.UTF_8);

    private DoCipher aesGcm() {
        return new DoCipher("AES/GCM/NoPadding", new SecretKeySpec(KEY, SecretKeyMgr.AES));
    }

    @Test
    void encryptsAndDecryptsBinaryDataWithAssociatedData() {
        byte[] plaintext = "encrypted content 测试".getBytes(StandardCharsets.UTF_8);
        CipherResult encrypted = aesGcm().doCipher(Cipher.ENCRYPT_MODE, plaintext, new GCMParameterSpec(128, NONCE), AAD);

        byte[] decrypted = aesGcm().doCipher(Cipher.DECRYPT_MODE, encrypted.getResult(), new GCMParameterSpec(128, NONCE), AAD).getResult();

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void supportsStringAndBase64ConvenienceOperations() {
        CipherResult encrypted = aesGcm().doCipher(Cipher.ENCRYPT_MODE, "plain text", new GCMParameterSpec(128, NONCE), null);

        assertEquals("plain text", aesGcm().doCipherFromBase64(Cipher.DECRYPT_MODE, encrypted.toBase64(), new GCMParameterSpec(128, NONCE), null).toUtf8());
    }

    @Test
    void rejectsTamperedAssociatedDataAndInvalidOperationState() {
        CipherResult encrypted = aesGcm().doCipher(Cipher.ENCRYPT_MODE, "plain text", new GCMParameterSpec(128, NONCE), AAD);

        IllegalArgumentException authenticationFailure = assertThrows(IllegalArgumentException.class,
                () -> aesGcm().doCipher(Cipher.DECRYPT_MODE, encrypted.getResult(), new GCMParameterSpec(128, NONCE), "other".getBytes(StandardCharsets.UTF_8)));

        assertEquals("Authentication failed: the key, parameters, associated data, or ciphertext is invalid.", authenticationFailure.getMessage());
        assertThrows(IllegalStateException.class, () -> aesGcm().doCipher(0, new byte[0], null, null));
        assertThrows(NullPointerException.class, () -> aesGcm().doCipher(Cipher.ENCRYPT_MODE, (String) null, null, null));
        assertThrows(IllegalArgumentException.class, () -> DoCipher.randomBytes(0));
    }
}
