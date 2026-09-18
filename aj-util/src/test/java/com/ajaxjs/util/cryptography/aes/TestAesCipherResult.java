package com.ajaxjs.util.cryptography.aes;

import com.ajaxjs.util.cryptography.CipherResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestAesCipherResult {
    @Test
    void retainsCiphertextRepresentationsAndOptionalNonce() {
        byte[] ciphertext = {0x01, 0x02, 0x03};
        CipherResult nonce = new CipherResult(new byte[]{0x04, 0x05});
        AesCipherResult result = new AesCipherResult(ciphertext);

        assertArrayEquals(ciphertext, result.getResult());
        assertEquals("AQID", result.toBase64());
        assertNull(result.getNonce());

        result.setNonce(nonce);

        assertSame(nonce, result.getNonce());
    }
}
