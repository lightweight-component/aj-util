package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.RandomTools;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

public class DoAes {
    /**
     * The name of the algorithm
     */
    private final String algorithmName;

    private final byte[] key;

    public final static String AES_GCM = "AES/GCM/NoPadding";

    public final static String AES_CBC = "AES/CBC/PKCS5Padding";

    /**
     * The nonce length in bytes for GCM mode.
     */
    private static final int GCM_NONCE_LENGTH = 12;

    /**
     * Generates cryptographically secure random bytes.
     *
     * @param length number of bytes to generate
     * @return newly generated random bytes
     */
    public static byte[] randomBytes(int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length must be greater than zero.");

        byte[] salt = new byte[length];
        RandomTools.RANDOM.nextBytes(salt);

        return salt;
    }

    public DoAes(String algorithmName, byte[] key) {
        Objects.requireNonNull(algorithmName, "DoAes.algorithmName");

        this.algorithmName = algorithmName;
        this.key = key;
    }

    public DoAes(String algorithmName, String key) {
        this(algorithmName, new Base64Utils(Objects.requireNonNull(key, "DoAes.key")).decode());
    }

    public DoAes(byte[] key) {
        this(AES_GCM, key);
    }

    public DoAes(String key) {
        this(AES_GCM, key);
    }

    public Result encrypt(String data) {
        if (algorithmName.equals(AES_GCM)) {
            byte[] nonce = randomBytes(GCM_NONCE_LENGTH);

            return encrypt(data, nonce, null);
        } else
            return encrypt(data, null, null);
    }

    public Result encrypt(String data, byte[] nonce, byte[] associatedData) {
        AlgorithmParameterSpec spec = null;

        if (algorithmName.equals(AES_GCM))
            spec = new GCMParameterSpec(128, nonce);

        SecretKeySpec keySpec = new SecretKeySpec(key, SecretKeyMgr.AES);
        DoCipher cipher = new DoCipher(algorithmName, Cipher.ENCRYPT_MODE, keySpec);
//        byte[] result = cipher.doCipher(data,  spec, associatedData).toBase64();
//
//        return new Result(result);
        return null;
    }
}
