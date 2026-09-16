package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.Cryptography;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;

public class Rsa {
    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final OAEPParameterSpec OAEP_SPEC = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private final byte[] data;

    public Rsa(String dataStr) {
        this(dataStr.getBytes(StandardCharsets.UTF_8));
    }

    public Rsa(byte[] data) {
        if (data == null)
            throw new IllegalArgumentException("RSA data must not be empty.");

        this.data = data.clone();
    }

    public byte[] encryptWithPublicKey(String publicKeyStr) {
        PublicKey publicKey = RestoreKey.restorePublicKey(publicKeyStr);

        return encryptWithPublicKey(publicKey);
    }

    public String encryptWithPublicKeyToBase64(String publicKeyStr) {
        return new Base64Utils(encryptWithPublicKey(publicKeyStr)).encodeAsString();
    }

    public byte[] encryptWithPublicKey(PublicKey publicKey) {
        Cryptography cryptography = new Cryptography(RSA_CIPHER, Cipher.ENCRYPT_MODE);
        cryptography.setSpec(OAEP_SPEC);
        cryptography.setKey(publicKey);
        cryptography.setData(data);

        return cryptography.doCipher();
    }

    public String encryptWithPublicKeyToBase64(PublicKey publicKey) {
        return new Base64Utils(encryptWithPublicKey(publicKey)).encodeAsString();
    }

    public byte[] decryptWithPrivateKey(String privateKeyStr) {
        return decryptWithPrivateKey(RestoreKey.restorePrivateKey(privateKeyStr));
    }

    public byte[] decryptWithPrivateKey(PrivateKey privateKey) {
        Cryptography cryptography = new Cryptography(RSA_CIPHER, Cipher.DECRYPT_MODE);
        cryptography.setSpec(OAEP_SPEC);
        cryptography.setKey(privateKey);
        cryptography.setData(data);

        return cryptography.doCipher();
    }

    public String decryptToString(String privateKeyStr) {
        return decryptToString(RestoreKey.restorePrivateKey(privateKeyStr));
    }

    public String decryptToString(PrivateKey privateKey) {
        return new String(decryptWithPrivateKey(privateKey), StandardCharsets.UTF_8);
    }

    /**
     * Get a pair of keys: public key and private key
     *
     * @return Key pair object
     * @throws IllegalArgumentException if the key size is not 2048, 3072, or 4096 bits
     * @throws IllegalStateException    if the requested key-pair algorithm is unavailable
     */
    public static KeyPair generateKeyPair(int keySize) {
        if (keySize != 2048 && keySize != 3072 && keySize != 4096)
            throw new IllegalArgumentException("RSA key size must be 2048, 3072, or 4096 bits: " + keySize);

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(Constant.RSA);
            generator.initialize(keySize);

            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(Constant.NO_SUCH_ALGORITHM + Constant.RSA, e);
        }
    }
}
