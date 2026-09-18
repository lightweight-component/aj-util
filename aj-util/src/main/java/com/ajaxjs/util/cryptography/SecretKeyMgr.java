/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.StringBytes;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Objects;

/**
 * Utility methods for generating, reconstructing, and encoding symmetric
 * secret keys.
 *
 * <p>This class provides convenience wrappers around the Java Cryptography
 * Architecture (JCA/JCE) APIs {@link KeyGenerator},
 * {@link SecretKeyFactory} and {@link SecureRandom}.</p>
 *
 * <p>Typical use cases include:</p>
 *
 * <ul>
 *     <li>generating a random symmetric key such as AES or HMAC keys</li>
 *     <li>creating a {@link SecretKey} from a {@link KeySpec}</li>
 *     <li>creating a configured {@link SecureRandom} instance</li>
 *     <li>encoding generated key material as Base64 text</li>
 * </ul>
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * SecretKey key =
 *         SecretKeyMgr.getSecretKey(
 *                 "AES",
 *                 256,
 *                 new SecureRandom()
 *         );
 *
 * String base64 =
 *         SecretKeyMgr.getSecretKeyAsStr(
 *                 "AES",
 *                 256,
 *                 new SecureRandom()
 *         );
 * }</pre>
 *
 * <p><strong>Important:</strong> {@link #getRandom(String, String)} merely
 * supplements the internal state of a {@link SecureRandom} instance using the
 * supplied string. It must not be treated as a password-based key derivation
 * function. Applications that need to derive a reproducible cryptographic key
 * from a password should use a dedicated KDF such as PBKDF2 instead.</p>
 *
 * <p>This class contains only static utility methods.</p>
 */
public class SecretKeyMgr {
    /**
     * Generates a symmetric secret key.
     *
     * <p>The requested algorithm is passed directly to
     * {@link KeyGenerator#getInstance(String)}. Common examples include
     * {@code AES}, {@code DES}, {@code DESede}, {@code HmacSHA256} and other
     * algorithms provided by the active JCA/JCE providers.</p>
     *
     * <p>The initialization behavior depends on {@code keySize} and
     * {@code secure}:</p>
     *
     * <ul>
     *     <li>
     *         {@code keySize > 0} and {@code secure != null}:
     *         initializes the generator with both the requested key size and
     *         random source
     *     </li>
     *     <li>
     *         {@code keySize > 0} and {@code secure == null}:
     *         initializes the generator with the requested key size and lets
     *         the provider choose its default random source
     *     </li>
     *     <li>
     *         {@code keySize == 0} and {@code secure != null}:
     *         lets the provider choose its default key size while using the
     *         supplied random source
     *     </li>
     *     <li>
     *         {@code keySize == 0} and {@code secure == null}:
     *         no explicit initialization is performed; the provider chooses
     *         both its default key size and random source
     *     </li>
     * </ul>
     *
     * <p>The exact supported key sizes depend on the requested algorithm and
     * installed security provider.</p>
     *
     * @param algorithmName key-generation algorithm name
     * @param keySize       requested key size in bits, or {@code 0} to use
     *                      the provider default
     * @param secure        optional secure random generator; may be
     *                      {@code null}
     * @return generated symmetric secret key
     * @throws RuntimeException         if the requested key-generation
     *                                  algorithm is unavailable
     * @throws IllegalArgumentException if the requested key size is not valid
     *                                  for the selected algorithm or provider
     */
    public static SecretKey getSecretKey(String algorithmName, int keySize, SecureRandom secure) {
        Objects.requireNonNull(algorithmName, "getSecretKey.algorithmName");

        KeyGenerator kg;

        try {
            kg = KeyGenerator.getInstance(algorithmName);

            if (keySize > 0) {
                if (secure == null)
                    kg.init(keySize);
                else
                    kg.init(keySize, secure);
            } else if (secure != null)
                kg.init(secure);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(DoCipher.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        return kg.generateKey();
    }

    /**
     * Advanced Encryption Standard algorithm name.
     */
    public final static String AES = "AES";

    public static SecretKey getSecretKey(SecureRandom secure) {
        return getSecretKey(AES, 128, secure);
    }

    /**
     * Generates or reconstructs a secret key from a key specification.
     *
     * <p>The supplied algorithm name is passed to
     * {@link SecretKeyFactory#getInstance(String)}. The concrete
     * {@link KeySpec} type must be compatible with the selected
     * {@link SecretKeyFactory} implementation.</p>
     *
     * <p>This method is commonly used with password-based or provider-specific
     * key specifications. For example, a {@code PBEKeySpec} may be processed
     * by a PBKDF2 {@link SecretKeyFactory}.</p>
     *
     * <p>The returned value is the {@link SecretKey} generated by
     * {@link SecretKeyFactory#generateSecret(KeySpec)}.</p>
     *
     * @param algorithmName secret-key factory algorithm name
     * @param spec          key specification containing or describing the key material
     * @return generated or reconstructed secret key
     * @throws IllegalArgumentException if the supplied key specification is invalid for the selected algorithm
     * @throws RuntimeException         if the requested {@link SecretKeyFactory} algorithm is unavailable
     */
    public static SecretKey getSecretKey(String algorithmName, KeySpec spec) {
        Objects.requireNonNull(algorithmName, "getSecretKey.algorithmName");
        Objects.requireNonNull(spec, "getSecretKey.spec");

        try {
            return SecretKeyFactory.getInstance(algorithmName).generateSecret(spec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid Key Spec.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(DoCipher.NO_SUCH_ALGORITHM + algorithmName, e);
        }
    }

    /**
     * Password-Based Key Derivation Function 2 with HMAC-SHA-256.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private final static String PBE = "PBKDF2WithHmacSHA256";

    public static SecretKey getSecretKey(KeySpec spec) {
        return getSecretKey(PBE, spec);
    }

    /**
     * Creates a {@link SecureRandom} instance and supplements its seed with
     * bytes derived from the supplied string.
     *
     * <p>The requested algorithm is passed to {@link SecureRandom#getInstance(String)}. One historically common
     * example is {@code SHA1PRNG}, although the available algorithms depend on
     * the installed security providers.</p>
     *
     * <p>The supplied string is converted to UTF-8 bytes and passed to {@link SecureRandom#setSeed(byte[])}.</p>
     *
     * <p><strong>Important:</strong> {@code setSeed(...)} supplements the
     * current internal random state. It does not necessarily replace or reset
     * the existing seed. Therefore, callers must not assume that the same input
     * string always produces the same random byte sequence across different
     * JVM versions, operating systems, providers, or runtime instances.</p>
     *
     * <p>This method should therefore be used only when additional seed
     * material needs to be mixed into a {@link SecureRandom}. It should not be
     * used as a deterministic password-to-key conversion mechanism.</p>
     *
     * <p>For reproducible password-derived cryptographic keys, use a proper
     * key derivation function such as PBKDF2 with a salt and iteration count.</p>
     *
     * @param algorithmName secure-random algorithm name
     * @param key           string whose UTF-8 bytes are mixed into the random generator's seed state
     * @return initialized secure random generator
     * @throws RuntimeException     if the requested secure-random algorithm is unavailable
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public static SecureRandom getRandom(String algorithmName, String key) {
        Objects.requireNonNull(algorithmName, "getRandom.algorithmName");
        Objects.requireNonNull(key, "getRandom.key");

        SecureRandom random;

        try {
            random = SecureRandom.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(DoCipher.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        random.setSeed(new StringBytes(key).getUTF8_Bytes());

        return random;
    }

    /**
     * Secure random number generator algorithm name.
     */
    private final static String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

    public static SecureRandom getRandom(String key) {
        return getRandom(SECURE_RANDOM_ALGORITHM, key);
    }

    /**
     * Generates a symmetric secret key and returns its encoded key material as a Base64 string.
     *
     * <p>The key is created using {@link #getSecretKey(String, int, SecureRandom)} and the raw encoded key
     * bytes returned by {@link SecretKey#getEncoded()} are then Base64 encoded.</p>
     *
     * <p>For ordinary software-generated keys such as AES or HMAC keys,
     * {@code getEncoded()} typically returns the raw key bytes. Some
     * provider-specific, hardware-backed or non-exportable key
     * implementations may instead return {@code null}. Such keys cannot be
     * represented by this method without additional handling.</p>
     *
     * <p>The returned Base64 string contains encoded key material and should
     * therefore be treated as sensitive information. Avoid logging or
     * exposing it unnecessarily.</p>
     *
     * @param algorithmName key-generation algorithm name
     * @param keySize       requested key size in bits, or {@code 0} to use the provider default
     * @param secure        optional secure random generator; may be {@code null}
     * @return Base64 representation of the encoded secret key
     * @throws RuntimeException         if the requested algorithm is unavailable
     * @throws IllegalArgumentException if the requested key size is invalid for the selected algorithm or provider
     */
    public static String getSecretKeyAsStr(String algorithmName, int keySize, SecureRandom secure) {
        byte[] encoded = getSecretKey(algorithmName, keySize, secure).getEncoded();

        return new Base64Utils(encoded).encodeAsString();
    }
}
