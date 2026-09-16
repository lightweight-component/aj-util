package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.*;

class TestRsa {
    @Test
    void testDoSignature() {
        PrivateKey privateKey = Rsa.generateKeyPair(2048).getPrivate();

        byte[] helloWorlds = new DoSignature(privateKey).sign("hello world");
        String privateKeyStr = PemUtils.privateKeyToPem(privateKey);
        String result = new DoSignature(privateKeyStr).signToBase64("hello world");

        assertEquals(new Base64Utils(helloWorlds).encodeAsString(), result);
    }

    @Test
    void testDoVerify() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        String publicKeyStr = PemUtils.publicKeyToPem(pair.getPublic());
        String privateKeyStr = PemUtils.privateKeyToPem(pair.getPrivate());

        String result = new DoSignature(privateKeyStr).signToBase64("hello world");
        boolean verified = new DoVerify(publicKeyStr).verify("hello world", result);

        assertTrue(verified);
    }

    @Test
    void testRSA() {
        // 生成公钥私钥
        KeyPair pair = Rsa.generateKeyPair(2048);
        String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
        String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());


        String word = "你好，世界！";

        byte[] encWord = new Rsa(word).encryptWithPublicKey(publicPem);

        String eBody = new Rsa(word).encryptWithPublicKeyToBase64(publicPem);
        String decWord2 = new Rsa(encWord).decryptToString(privatePem);
        System.out.println("加密前: " + word + "\n\r密文：" + eBody + "\n解密后: " + decWord2);
        assertEquals(word, decWord2);

        String english = "Hello, World!";
        byte[] encEnglish = new Rsa(english).encryptWithPublicKey(pair.getPublic());
        String decEnglish = new Rsa(encEnglish).decryptToString(pair.getPrivate());
        System.out.println("加密前: " + english + "\n\r" + "解密后: " + decEnglish);

        assertEquals(english, decEnglish);

        // 产生签名
        String sign = new DoSignature(privatePem).signToBase64(encEnglish);
        // 验证签名
        assertTrue(new DoVerify(publicPem).verify(encEnglish, sign));
    }

    @Test
    void testRsaPemRoundTripEncryptionAndSignature() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
        String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());
        byte[] plaintext = "RSA round trip".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(pair.getPublic().getEncoded(), RestoreKey.restorePublicKey(publicPem).getEncoded());
        assertArrayEquals(pair.getPrivate().getEncoded(), RestoreKey.restorePrivateKey(privatePem).getEncoded());

        byte[] encrypted = new Rsa(plaintext).encryptWithPublicKey(publicPem);
        assertArrayEquals(plaintext, new Rsa(encrypted).decryptWithPrivateKey(privatePem));

        byte[] signature = new DoSignature(pair.getPrivate()).sign(plaintext);
        assertTrue(new DoVerify(pair.getPublic()).verify(plaintext, signature));

        byte[] changed = "RSA round trip!".getBytes(StandardCharsets.UTF_8);
        assertFalse(new DoVerify(pair.getPublic()).verify(changed, signature));
    }

    @TempDir
    Path tempDir;

    @Test
    void keyRepresentationsAndLoadersRoundTrip() throws Exception {
        KeyPair pair = Rsa.generateKeyPair(2048);
        String publicBase64 = PemUtils.encodeKeyBase64(pair.getPublic());
        String privateBase64 = PemUtils.encodeKeyBase64(pair.getPrivate());
        String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());

        assertArrayEquals(pair.getPublic().getEncoded(), RestoreKey.restorePublicKey(publicBase64).getEncoded());
        assertTrue(PemUtils.publicKeyToPem(pair.getPublic()).contains("BEGIN PUBLIC KEY"));
        assertTrue(PemUtils.publicKeyToPem(publicBase64).contains("BEGIN PUBLIC KEY"));
        assertTrue(PemUtils.privateKeyToPem(privateBase64).contains("BEGIN PRIVATE KEY"));
        assertArrayEquals(pair.getPrivate().getEncoded(), RestoreKey.loadPrivateKey(new ByteArrayInputStream(privatePem.getBytes(StandardCharsets.UTF_8))).getEncoded());
        assertArrayEquals(pair.getPrivate().getEncoded(), RestoreKey.loadPrivateKey(new ByteArrayInputStream(privatePem.getBytes(StandardCharsets.UTF_8)), "UTF-8").getEncoded());

        Path keyFile = tempDir.resolve("private.pem");
        Files.write(keyFile, privatePem.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(pair.getPrivate().getEncoded(), RestoreKey.loadPrivateKey(keyFile.toString()).getEncoded());
    }

    @Test
    void rsaConvenienceMethodsCoverBothOperationDirections() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        byte[] data = "rsa content".getBytes(StandardCharsets.UTF_8);

        byte[] publicEncrypted = new Rsa(data).encryptWithPublicKey(pair.getPublic());
        assertEquals("rsa content", new Rsa(publicEncrypted).decryptToString(pair.getPrivate()));
    }

    @Test
    void stringSignatureAndVerificationSettersRoundTrip() {
        KeyPair pair = Rsa.generateKeyPair(2048);
        String signature = new DoSignature(pair.getPrivate()).signToBase64("signed content");

        assertTrue(new DoVerify(pair.getPublic()).verify("signed content", signature));

        assertTrue(new DoVerify(PemUtils.publicKeyToPem(pair.getPublic())).verify("signed content", signature));
    }
}
