package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.StringBytes;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * Provides symmetric secret-key generation, derivation, and encoding utilities.
 */
public class SecretKeyMgr {
    /**
     * Generates a symmetric secret key using the requested algorithm and optional initialization parameters.
     *
     * @param algorithmName the key-generation algorithm
     * @param keySize       the requested key size, or zero to use the provider default
     * @param secure        the optional secure random generator
     * @return the generated symmetric secret key
     * @throws RuntimeException         if the requested algorithm is unavailable
     * @throws IllegalArgumentException if the key size or random parameters are invalid
     */
    public static SecretKey getSecretKey(String algorithmName, int keySize, SecureRandom secure) {
        KeyGenerator kg;

        try {
            kg = KeyGenerator.getInstance(algorithmName);

            if (keySize != 0 && secure != null)
                kg.init(keySize, secure);
            else if (keySize == 0 && secure != null)
                kg.init(secure);
            else if (keySize != 0)
                kg.init(keySize);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        return kg.generateKey();
    }

    /**
     * Derives or reconstructs a secret key from a key specification.
     *
     * @param algorithmName the secret-key factory algorithm
     * @param spec          the specification of the key material
     * @return the generated secret key
     * @throws IllegalArgumentException if the key specification is invalid
     * @throws RuntimeException         if the requested algorithm is unavailable
     */
    public static Key getSecretKey(String algorithmName, KeySpec spec) {
        try {
            return SecretKeyFactory.getInstance(algorithmName).generateSecret(spec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid Key Spec.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }
    }

    /**
     * Creates a secure random generator for the requested algorithm and supplements its seed with the given string.
     *
     * @param algorithmName the secure-random algorithm name, such as {@code SHA1PRNG}
     * @param key           the string whose UTF-8 bytes supplement the random seed
     * @return the initialized secure random generator
     * @throws RuntimeException     if the requested algorithm is unavailable
     * @throws NullPointerException if the seed string is null
     */
    public static SecureRandom getRandom(String algorithmName, String key) {
        SecureRandom random;

        try {
            random = SecureRandom.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        random.setSeed(new StringBytes(key).getUTF8_Bytes());

        return random;
    }

    /**
     * Generates a secret key and returns its encoded bytes as a Base64 string.
     *
     * @param algorithmName the key-generation algorithm
     * @param keySize       the requested key size, or zero to use the provider default
     * @param secure        the optional secure random generator
     * @return the Base64-encoded secret key
     * @throws RuntimeException         if the requested algorithm is unavailable
     * @throws IllegalArgumentException if the key size or random parameters are invalid
     */
    public static String getSecretKeyAsStr(String algorithmName, int keySize, SecureRandom secure) {
        byte[] encoded = getSecretKey(algorithmName, keySize, secure).getEncoded();

        return new Base64Utils(encoded).encodeAsString();
    }
}
