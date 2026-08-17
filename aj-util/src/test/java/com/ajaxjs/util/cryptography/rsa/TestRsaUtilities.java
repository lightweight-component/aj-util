package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.cryptography.Constant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

class TestRsaUtilities {
    @TempDir
    Path tempDir;

    @Test
    void keyRepresentationsAndLoadersRoundTrip() throws Exception {
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        KeyPair pair = keyMgr.generateKeyPair();
        String publicBase64 = keyMgr.getPublicKeyStr();
        String privateBase64 = keyMgr.getPrivateKeyStr();
        String privatePem = KeyMgr.privateKeyToPem(pair.getPrivate());

        assertArrayEquals(pair.getPublic().getEncoded(), KeyMgr.restoreKey(true, publicBase64).getEncoded());
        assertTrue(KeyMgr.publicKeyToPem(pair.getPublic()).contains("BEGIN PUBLIC KEY"));
        assertTrue(KeyMgr.publicKeyToPem(publicBase64).contains("BEGIN PUBLIC KEY"));
        assertTrue(KeyMgr.privateKeyToPem(privateBase64).contains("BEGIN PRIVATE KEY"));
        assertArrayEquals(pair.getPrivate().getEncoded(), KeyMgr.loadPrivateKey(
                new ByteArrayInputStream(privatePem.getBytes(StandardCharsets.UTF_8))).getEncoded());
        assertArrayEquals(pair.getPrivate().getEncoded(), KeyMgr.loadPrivateKey(
                new ByteArrayInputStream(privatePem.getBytes(StandardCharsets.UTF_8)), "UTF-8").getEncoded());

        Path keyFile = tempDir.resolve("private.pem");
        Files.write(keyFile, privatePem.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(pair.getPrivate().getEncoded(), KeyMgr.loadPrivateKey(keyFile.toString()).getEncoded());
    }

    @Test
    void rsaConvenienceMethodsCoverBothOperationDirections() {
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        keyMgr.generateKeyPair();
        byte[] data = "rsa content".getBytes(StandardCharsets.UTF_8);

        byte[] publicEncrypted = KeyMgr.publicKeyEncrypt(data, keyMgr.getPublicToPem());
        assertFalse(KeyMgr.publicKeyEncryptAsBase64Str(data, keyMgr.getPublicToPem()).isEmpty());
        assertEquals("rsa content", KeyMgr.privateKeyDecryptAsStr(publicEncrypted, keyMgr.getPrivateToPem()));

        byte[] privateEncrypted = KeyMgr.privateKeyEncrypt(data, keyMgr.getPrivateToPem());
        assertArrayEquals(data, KeyMgr.publicKeyDecrypt(privateEncrypted, keyMgr.getPublicToPem()));
        assertArrayEquals(data, KeyMgr.action(false, true, privateEncrypted, keyMgr.getPublicToPem()));
    }

    @Test
    void stringSignatureAndVerificationSettersRoundTrip() {
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        keyMgr.generateKeyPair();
        String signature = new DoSignature(Constant.SHA256_RSA)
                .setStrData("signed content")
                .setPrivateKeyStr(keyMgr.getPrivateToPem())
                .signToString();

        assertTrue(new DoVerify(Constant.SHA256_RSA)
                .setStrData("signed content")
                .setSignatureBase64(signature)
                .setPublicKeyStr(keyMgr.getPublicToPem())
                .verify());
    }

    @Test
    void packageHelpersValidateRequiredState() {
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 2048);
        assertThrows(IllegalStateException.class, keyMgr::requireGeneratedKeyPair);
        assertThrows(IllegalStateException.class, new DoSignature(Constant.SHA256_RSA)::validateState);
        assertThrows(IllegalStateException.class, new DoVerify(Constant.SHA256_RSA)::validateState);
    }
}
