package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static com.ajaxjs.util.cryptography.Aes.pbeDecrypt;
import static com.ajaxjs.util.cryptography.Aes.pbeEncrypt;
import static org.junit.jupiter.api.Assertions.*;

class TestAes {
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
        DoCipher cryptography = new DoCipher(Constant.AES_GCM, Cipher.DECRYPT_MODE, new SecretKeySpec(new byte[16], Constant.AES));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cryptography.doCipher(new byte[16],
                new GCMParameterSpec(128, new byte[12]), null));
        assertTrue(exception.getMessage().startsWith("Authentication failed"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testRejectsWeakRsaKeySize() {
        assertThrows(IllegalArgumentException.class, () -> Rsa.generateKeyPair(2047));
    }

    @Test
    void testInvalidPrivateKeyIsNotIncludedInException() {
        String invalidKey = "c2VjcmV0LXByaXZhdGUta2V5";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RestoreKey.restorePrivateKey(invalidKey));

        assertFalse(exception.getMessage().contains(invalidKey));
        assertNotNull(exception.getCause());
    }

//    @Test
//    void testVerifyStateValidation() {
//        Verify2 verify = new Verify2(Constant.SHA256_RSA);
//        assertEquals("Data to verify is required.", assertThrows(IllegalStateException.class, verify::verify).getMessage());
//
//        verify.setData("data".getBytes(StandardCharsets.UTF_8));
//        assertEquals("Signature data is required.", assertThrows(IllegalStateException.class, verify::verify).getMessage());
//    }

    @Test
    void testPbeRejectsTamperedCiphertextAndWrongPassword() {
        byte[] salt = Aes.randomBytes(Aes.PBE_SALT_LENGTH);
        byte[] encrypted = pbeEncrypt(
                "authenticated content",
                "correct password",
                salt,
                Cryptography.MIN_PBE_ITERATIONS
        );
        byte[] tampered = encrypted.clone();
        tampered[tampered.length - 1] ^= 1;

        IllegalArgumentException tamperError = assertThrows(IllegalArgumentException.class, () -> pbeDecrypt(tampered, "correct password", salt, Cryptography.MIN_PBE_ITERATIONS));
        IllegalArgumentException passwordError = assertThrows(IllegalArgumentException.class, () -> pbeDecrypt(encrypted, "wrong password", salt, Cryptography.MIN_PBE_ITERATIONS));

        assertTrue(tamperError.getMessage().startsWith("Authentication failed"));
        assertTrue(passwordError.getMessage().startsWith("Authentication failed"));
    }

    @Test
    void testTransformationAcceptsRawAesKeyData() {
        byte[] key = new byte[16];
        byte[] nonce = new byte[12];
        byte[] plaintext = "GCM content".getBytes(StandardCharsets.UTF_8);

        Cryptography encrypt = new Cryptography(Constant.AES_GCM, Cipher.ENCRYPT_MODE);
        encrypt.setKeyData(key);
        encrypt.setSpec(new GCMParameterSpec(128, nonce));
        encrypt.setData(plaintext);
        byte[] encrypted = encrypt.doCipher();

        Cryptography decrypt = new Cryptography(Constant.AES_GCM, Cipher.DECRYPT_MODE);
        decrypt.setKeyData(key);
        decrypt.setSpec(new GCMParameterSpec(128, nonce));
        decrypt.setData(encrypted);

        assertArrayEquals(plaintext, decrypt.doCipher());
    }

    @Test
    void testCipherRejectsMissingKeyClearly() {
        Cryptography missingKey = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        missingKey.setData(new byte[0]);
        assertEquals("Cipher key is required.", assertThrows(IllegalStateException.class, missingKey::doCipher).getMessage());
    }

    @Test
    void testCipherRejectsMissingDataClearly() {
        Cryptography missingData = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        missingData.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        assertEquals("Cipher data is required.", assertThrows(IllegalStateException.class, missingData::doCipher).getMessage());
    }

    @Test
    void testCipherRejectsMissingAlgorithmClearly() {
        Cryptography missingAlgorithm = new Cryptography(null, Cipher.ENCRYPT_MODE);
        missingAlgorithm.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        missingAlgorithm.setData(new byte[0]);

        assertEquals("Cipher algorithm is required.", assertThrows(IllegalStateException.class, missingAlgorithm::doCipher).getMessage());
    }

    @Test
    void testCipherRejectsInvalidModeClearly() {
        Cryptography invalidMode = new Cryptography(Constant.AES, Cipher.WRAP_MODE);
        invalidMode.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        invalidMode.setData(new byte[0]);

        assertEquals("Cipher mode must be ENCRYPT_MODE or DECRYPT_MODE.", assertThrows(IllegalStateException.class, invalidMode::doCipher).getMessage());
    }

    @Test
    void testSensitiveFieldsAreExcludedFromToString() throws Exception {
        String plaintext = "plain-secret";
        String privateKey = "private-key-secret";
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(new byte[16], Constant.AES));
        cryptography.setData(plaintext.getBytes(StandardCharsets.UTF_8));

        assertFalse(cryptography.toString().contains(plaintext));
    }

    @Test
    void testCertificateGcmRejectsInvalidParameters() {
        byte[] key = new byte[32];
        byte[] aad = new byte[0];
        byte[] nonce = new byte[12];

        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.aesDecryptToString(null, aad, nonce, "AA=="));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.aesDecryptToString(key, aad, new byte[8], "AA=="));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.aesDecryptToString(key, null, nonce, "AA=="));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.aesDecryptToString(key, aad, nonce, " "));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.aesDecryptToString(key, null, "123456789012", "AA=="));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.aesDecryptToString(key, "", null, "AA=="));
    }

    @Test
    void testCertificateFieldUnquotingOnlyRemovesSurroundingQuotes() {
        assertEquals("a\"b", CertificateUtils.remove("\"a\"b\""));
        assertEquals("a\"b", CertificateUtils.remove("a\"b"));
        assertThrows(IllegalArgumentException.class, () -> CertificateUtils.remove(null));
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

    @Test
    void certificateAesGcmDecryptsValidPayload() {
        byte[] aad = "certificate".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        byte[] nonce = "123456789012".getBytes(StandardCharsets.US_ASCII);

        String ciphertext = Aes.aesEncrypt("certificate payload", key, nonce, aad);

        assertEquals("certificate payload", Aes.aesDecrypt(ciphertext, key, nonce, aad));
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

    /* ----------------- 敏感信息加密 ------------------- */
    /* <a href="https://pay.weixin.qq.com/doc/global/v3/zh/4012354992">...</a> */

    /**
     * 敏感信息加密
     *
     * @param message     数据
     * @param certificate 证书
     * @return 加密后的文本
     */
    public static String encryptOAEP(String message, X509Certificate certificate) {
        Cryptography cryptography = new Cryptography(Constant.RSAES_OAEP, Cipher.ENCRYPT_MODE);
        cryptography.setKey(certificate.getPublicKey());
        cryptography.setDataStr(message);

        return cryptography.doCipherAsBase64Str();
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param privateKey 商户私钥
     * @return 解密后的文本
     */
    public static String decryptOAEP(String cipherText, PrivateKey privateKey) {
        Cryptography cryptography = new Cryptography(Constant.RSAES_OAEP, Cipher.DECRYPT_MODE);
        cryptography.setKey(privateKey);
        cryptography.setDataStrBase64(cipherText);

        return cryptography.doCipherAsStr();
    }

    static String text = "Hello world";

    @Test
    void testEncryptOAEP() {
        // 公钥加密
        String result = encryptOAEP(text, CertificateUtils.getCert("D:\\sp42\\code\\ajaxjs\\aj-util\\src\\test\\java\\com\\ajaxjs\\util\\cryptography\\1623777099_20251021_cert\\apiclient_cert.pem"));
        System.out.println(result);

        // 私钥解密
        PrivateKey privateKey = RestoreKey.loadPrivateKey("D:\\sp42\\code\\ajaxjs\\aj-util\\src\\test\\java\\com\\ajaxjs\\util\\cryptography\\1623777099_20251021_cert\\apiclient_key.pem");

        String decryptOAEP = decryptOAEP(result, privateKey);
        assertEquals(text, decryptOAEP);
    }
}
