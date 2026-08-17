package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/**
 * Configures and performs AES, DES, Triple DES, and password-based cipher operations.
 * <p>
 * Instances are mutable and are not thread-safe.
 */
@Data
@RequiredArgsConstructor
public class Cryptography {
    /**
     * The recommended salt length in bytes for PBE operations.
     */
    public static final int PBE_SALT_LENGTH = 16;

    /**
     * The minimum recommended iteration count for PBE key derivation.
     */
    public static final int MIN_PBE_ITERATIONS = 100_000;

    /**
     * The derived PBE key length in bits.
     */
    private static final int PBE_KEY_LENGTH = 128;

    /**
     * The nonce length in bytes for GCM mode.
     */
    private static final int GCM_NONCE_LENGTH = 12;

    /**
     * The authentication tag length in bits for GCM mode.
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * The name of the algorithm
     */
    private final String algorithmName;

    /**
     * The cipher mode, normally {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}.
     */
    private final int mode;

    /**
     * The cryptographic key used for cipher operations.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Key key;

    /**
     * Sets the cryptographic key from raw key bytes.
     *
     * @param keyData the raw key bytes
     * @throws IllegalStateException if the configured cipher algorithm is missing
     * @throws IllegalArgumentException if the key bytes are null or invalid
     */
    public void setKeyData(byte[] keyData) {
        key = new SecretKeySpec(keyData, getKeyAlgorithm());
    }

    /**
     * The source secret key whose encoded bytes are used to rebuild the cipher key.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SecretKey secretKey;

    /**
     * Sets the secret key and rebuilds the cipher key from its encoded bytes using
     * the configured cipher algorithm name.
     *
     * @param secretKey the secret key
     * @throws NullPointerException if the secret key is null
     * @throws IllegalStateException if the configured cipher algorithm is missing
     */
    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        key = new SecretKeySpec(secretKey.getEncoded(), getKeyAlgorithm());
    }

    /**
     * The input data for the cipher operation.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] data;

    /**
     * Sets the cipher data from a plain string (UTF-8 encoded).
     *
     * @param dataStr the input string
     * @throws NullPointerException if the input string is null
     */
    public void setDataStr(String dataStr) {
        data = new StringBytes(dataStr).getUTF8_Bytes();
    }

    /**
     * Sets the cipher data from a Base64-encoded string.
     *
     * @param dataStrBase64 the Base64-encoded input string
     * @throws RuntimeException if the input is not valid Base64
     */
    public void setDataStrBase64(String dataStrBase64) {
        data = new Base64Utils(dataStrBase64).decode();
    }

    /**
     * The algorithm parameter specification, such as an initialization vector or GCM parameters.
     */
    private AlgorithmParameterSpec spec;

    /**
     * Additional authenticated data used with AEAD ciphers such as GCM.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] associatedData;

    /**
     * Performs the configured cipher operation and returns the result as bytes.
     *
     * @return the encrypted or decrypted bytes
     * @throws IllegalStateException if required cipher state is missing or invalid
     * @throws IllegalArgumentException if the key, parameters, input length, padding, or authentication tag is invalid
     * @throws RuntimeException if the configured transformation is unavailable
     */
    public byte[] doCipher() {
        validateState();

        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            if (associatedData != null)
                cipher.updateAAD(associatedData);

            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (AEADBadTagException e) {
            throw new IllegalArgumentException("Authentication failed: the key, parameters, associated data, or ciphertext is invalid.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Invalid input length for transformation: " + algorithmName, e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("Cipher operation failed because the key, padding, or ciphertext is invalid.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
        }
    }

    /**
     * Returns the base key algorithm from the configured cipher transformation.
     *
     * @return the key algorithm, such as {@code AES}
     * @throws IllegalStateException if the cipher algorithm is missing
     */
    String getKeyAlgorithm() {
        if (algorithmName == null || algorithmName.trim().isEmpty())
            throw new IllegalStateException("Cipher algorithm is required.");

        int separator = algorithmName.indexOf('/');

        return separator < 0 ? algorithmName : algorithmName.substring(0, separator);
    }

    /**
     * Validates the state required to perform a cipher operation.
     *
     * @throws IllegalStateException if the algorithm, mode, key, or input data is invalid
     */
    void validateState() {
        if (algorithmName == null || algorithmName.trim().isEmpty())
            throw new IllegalStateException("Cipher algorithm is required.");

        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE)
            throw new IllegalStateException("Cipher mode must be ENCRYPT_MODE or DECRYPT_MODE.");

        if (key == null)
            throw new IllegalStateException("Cipher key is required.");

        if (data == null)
            throw new IllegalStateException("Cipher data is required.");
    }

    /**
     * Performs the configured cipher operation and returns the result as a UTF-8 string.
     *
     * @return the encrypted or decrypted string
     * @throws IllegalStateException if required cipher state is missing or invalid
     * @throws IllegalArgumentException if the cipher operation fails for invalid input or parameters
     * @throws RuntimeException if the configured transformation is unavailable
     */
    public String doCipherAsStr() {
        return new StringBytes(doCipher()).getUTF8_String();
    }

    /**
     * Performs the configured cipher operation and returns the result as a Base64-encoded string.
     *
     * @return the Base64-encoded result
     * @throws IllegalStateException if required cipher state is missing or invalid
     * @throws IllegalArgumentException if the cipher operation fails for invalid input or parameters
     * @throws RuntimeException if the configured transformation is unavailable
     */
    public String doCipherAsBase64Str() {
        return new Base64Utils(doCipher()).encodeAsString();
    }

    /**
     * Get hex string of cipher, which is good for encrypt.
     *
     * @return Hex string of cipher.
     * @throws IllegalStateException if required cipher state is missing or invalid
     * @throws IllegalArgumentException if the cipher operation fails for invalid input or parameters
     * @throws RuntimeException if the configured transformation is unavailable
     */
    public String doCipherAsHexStr() {
        return BytesHelper.bytesToHexStr(doCipher());
    }

    /**
     * Do encrypt
     *
     * @param data The text to be encrypted
     * @param key  The key
     * @return The encrypted string
     * @throws RuntimeException if AES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static String AES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    /**
     * Do decrypt
     *
     * @param data The text to be decrypted
     * @param key  The key
     * @return The decrypted string
     * @throws RuntimeException if AES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key, hexadecimal input, or ciphertext is invalid
     */
    public static String AES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(BytesHelper.parseHexStr2Byte(data));

        return cryptography.doCipherAsStr();
    }

    /**
     * Encrypts the given data using DES and returns the result as a hex string.
     *
     * @param data the plaintext to encrypt
     * @param key  the encryption key
     * @return the encrypted hex string
     * @throws RuntimeException if DES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key or input is invalid
     */
    public static String DES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    /**
     * Decrypts the given DES-encrypted hex string.
     *
     * @param data the encrypted hex string
     * @param key  the decryption key
     * @return the decrypted plaintext
     * @throws RuntimeException if DES or the configured secure-random algorithm is unavailable
     * @throws IllegalArgumentException if the key, hexadecimal input, or ciphertext is invalid
     */
    public static String DES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(BytesHelper.parseHexStr2Byte(data));

        return cryptography.doCipherAsStr();
    }

    /**
     * Encrypts the given data using Triple DES and returns the raw bytes.
     *
     * @param data the plaintext to encrypt
     * @param key  the encryption key bytes
     * @return the encrypted bytes
     * @throws IllegalArgumentException if the key or input is invalid
     * @throws RuntimeException if Triple DES is unavailable
     */
    public static byte[] tripleDES_encode(String data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.ENCRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setDataStr(data);

        return cryptography.doCipher();
    }

    /**
     * Decrypts the given Triple DES-encrypted bytes.
     *
     * @param data the encrypted bytes
     * @param key  the decryption key bytes
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the key or ciphertext is invalid
     * @throws RuntimeException if Triple DES is unavailable
     */
    public static String tripleDES_decode(byte[] data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    /**
     * Generates a random salt for password-based encryption.
     *
     * @return a new random salt
     */
    public static byte[] initSalt() {
        byte[] salt = new byte[PBE_SALT_LENGTH];
        RandomTools.RANDOM.nextBytes(salt);

        return salt;
    }

    /**
     * Encrypts the given data using password-based encryption (PBE) with AES/GCM.
     *
     * @param data           the plaintext to encrypt
     * @param key            the password used to derive the encryption key
     * @param salt           the salt for key derivation
     * @param iterationCount the iteration count for key derivation
     * @return the encrypted bytes, with the GCM nonce prepended
     * @throws IllegalArgumentException if the password, salt, iteration count, or plaintext is invalid
     * @throws RuntimeException if a required cryptographic algorithm is unavailable
     */
    public static byte[] PBE_encode(String data, String key, byte[] salt, int iterationCount) {
        validatePbeParameters(salt, iterationCount);
        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.ENCRYPT_MODE);
        cryptography.setKey(derivePbeKey(key, salt, iterationCount));
        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        RandomTools.RANDOM.nextBytes(nonce);
        cryptography.setSpec(new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
        cryptography.setDataStr(data);

        byte[] encrypted = cryptography.doCipher();
        byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
        System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);

        return result;
    }

    /**
     * Decrypts data produced by {@link #PBE_encode(String, String, byte[], int)}.
     *
     * @param data           the encrypted bytes, with the GCM nonce prepended
     * @param key            the password used to derive the decryption key
     * @param salt           the salt for key derivation
     * @param iterationCount the iteration count for key derivation
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the password, salt, iteration count, ciphertext, or tag is invalid
     * @throws RuntimeException if a required cryptographic algorithm is unavailable
     */
    public static String PBE_decode(byte[] data, String key, byte[] salt, int iterationCount) {
        validatePbeParameters(salt, iterationCount);
        if (data == null || data.length < GCM_NONCE_LENGTH + GCM_TAG_LENGTH / Byte.SIZE)
            throw new IllegalArgumentException("PBE ciphertext is missing or too short.");

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        cryptography.setKey(derivePbeKey(key, salt, iterationCount));
        cryptography.setSpec(new GCMParameterSpec(GCM_TAG_LENGTH, Arrays.copyOf(data, GCM_NONCE_LENGTH)));
        cryptography.setData(Arrays.copyOfRange(data, GCM_NONCE_LENGTH, data.length));

        return cryptography.doCipherAsStr();
    }

    /**
     * Decrypts data created by the former PBEWithMD5AndDES implementation.
     * This method must not be used to encrypt new data.
     *
     * @param data           the data to be decoded
     * @param key            the password used to derive the decryption key
     * @param salt           the legacy 8-byte PBE salt
     * @param iterationCount the positive legacy key-derivation iteration count
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the password, salt, iteration count, key, or ciphertext is invalid
     * @throws RuntimeException if the legacy algorithm is unavailable
     */
    @Deprecated
    public static String PBE_legacy_decode(byte[] data, String key, byte[] salt, int iterationCount) {
        if (salt == null || salt.length != 8)
            throw new IllegalArgumentException("Legacy PBE salt must contain exactly 8 bytes.");

        if (iterationCount <= 0)
            throw new IllegalArgumentException("Legacy PBE iteration count must be greater than zero.");

        Cryptography cryptography = new Cryptography(Constant.PBE_LEGACY, Cipher.DECRYPT_MODE);
        PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());

        try {
            cryptography.setKey(SecretKeyMgr.getSecretKey(Constant.PBE_LEGACY, keySpec));
        } finally {
            keySpec.clearPassword();
        }

        cryptography.setSpec(new PBEParameterSpec(salt, iterationCount));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    /**
     * Derives an AES secret key from the given password, salt and iteration count.
     *
     * @param password       the PBE password
     * @param salt           the salt
     * @param iterationCount the iteration count
     * @return the derived AES secret key
     * @throws IllegalArgumentException if the password is null or empty or the key specification is invalid
     * @throws RuntimeException if PBKDF2 with HMAC-SHA-256 is unavailable
     */
    static SecretKeySpec derivePbeKey(String password, byte[] salt, int iterationCount) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("PBE password must not be empty.");

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, PBE_KEY_LENGTH);

        try {
            Key derivedKey = SecretKeyMgr.getSecretKey(Constant.PBE, keySpec);
            return new SecretKeySpec(derivedKey.getEncoded(), Constant.AES);
        } finally {
            keySpec.clearPassword();
        }
    }

    /**
     * Validates the PBE salt and iteration count parameters.
     *
     * @param salt           the salt to validate
     * @param iterationCount the iteration count to validate
     * @throws IllegalArgumentException if the salt is too short or the iteration count is too small
     */
    static void validatePbeParameters(byte[] salt, int iterationCount) {
        if (salt == null || salt.length < PBE_SALT_LENGTH)
            throw new IllegalArgumentException("PBE salt must contain at least " + PBE_SALT_LENGTH + " bytes.");

        if (iterationCount < MIN_PBE_ITERATIONS)
            throw new IllegalArgumentException("PBE iteration count must be at least " + MIN_PBE_ITERATIONS + ".");
    }
}
