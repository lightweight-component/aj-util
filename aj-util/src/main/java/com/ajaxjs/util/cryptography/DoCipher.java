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
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;
import lombok.RequiredArgsConstructor;

import javax.crypto.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

/**
 * Provides a lightweight wrapper around the JCA/JCE {@link Cipher} API.
 *
 * <p>A {@code DoCipher} instance is configured with a cipher transformation and
 * cryptographic key. The operation mode, input data, optional algorithm
 * parameters and optional additional authenticated data (AAD) are supplied for
 * each cipher operation.</p>
 *
 * <p>Typical cipher transformations include:</p>
 *
 * <pre>{@code
 * AES/GCM/NoPadding
 * AES/CBC/PKCS5Padding
 * RSA/ECB/OAEPWithSHA-256AndMGF1Padding
 * }</pre>
 *
 * <p>The transformation name is passed directly to
 * {@link Cipher#getInstance(String)}. The supplied {@link Key} must therefore
 * be compatible with the selected transformation.</p>
 *
 * <p>Algorithm-specific parameters may be supplied through
 * {@link AlgorithmParameterSpec}. Examples include {@code GCMParameterSpec}
 * for AES-GCM, {@code IvParameterSpec} for AES-CBC and
 * {@code OAEPParameterSpec} for RSA-OAEP.</p>
 *
 * <p>Additional authenticated data (AAD) may be supplied for authenticated
 * encryption modes such as AES-GCM. AAD is authenticated but not encrypted.
 * The exact same AAD must be supplied during decryption. Algorithms that do not
 * support AAD should pass {@code null}.</p>
 *
 * <p>A new {@link Cipher} instance is created for every operation. This class
 * therefore does not retain mutable cipher state between calls.</p>
 *
 * @see Cipher
 * @see CipherResult
 * @see AlgorithmParameterSpec
 */
@RequiredArgsConstructor
public class DoCipher {
    /**
     * Cipher transformation passed to {@link Cipher#getInstance(String)}.
     *
     * <p>The value may include the algorithm, operation mode and padding, for
     * example {@code AES/GCM/NoPadding}.</p>
     */
    private final String algorithmName;

    /**
     * Cryptographic key used to initialize cipher operations.
     *
     * <p>The concrete key type must be compatible with
     * {@link #algorithmName}. AES operations normally use a secret key, while
     * RSA operations normally use a public or private key.</p>
     */
    private final Key key;

    /**
     * Performs a cipher operation on raw binary data.
     *
     * <p>The operation mode must be either {@link Cipher#ENCRYPT_MODE} or
     * {@link Cipher#DECRYPT_MODE}. A new {@link Cipher} instance is obtained
     * from the configured transformation for every invocation.</p>
     *
     * <p>If {@code spec} is non-null, the cipher is initialized using
     * {@link Cipher#init(int, Key, AlgorithmParameterSpec)}. Otherwise, it is
     * initialized using {@link Cipher#init(int, Key)}.</p>
     *
     * <p>If {@code associatedData} is non-null, it is supplied to
     * {@link Cipher#updateAAD(byte[])} before the input data is processed.
     * The selected cipher transformation must support AAD.</p>
     *
     * <p>For AEAD algorithms such as AES-GCM, authentication is verified during
     * decryption. An invalid key, nonce, associated data, ciphertext or
     * authentication tag causes the operation to fail.</p>
     *
     * @param mode           cipher operation mode; either
     *                       {@link Cipher#ENCRYPT_MODE} or
     *                       {@link Cipher#DECRYPT_MODE}
     * @param data           raw input bytes to encrypt or decrypt
     * @param spec           optional algorithm-specific parameters; may be {@code null}
     * @param associatedData optional additional authenticated data (AAD);
     *                       may be {@code null}
     * @return the raw cipher result wrapped in a {@link CipherResult}
     * @throws IllegalStateException    if the transformation, key, mode or input
     *                                  data is missing or invalid
     * @throws IllegalArgumentException if the transformation is unavailable,
     *                                  the key or parameters are invalid, the
     *                                  input length is invalid, padding fails,
     *                                  or AEAD authentication fails
     */
    public CipherResult doCipher(int mode, byte[] data, AlgorithmParameterSpec spec, byte[] associatedData) {
        if (ObjectHelper.isEmptyText(algorithmName))
            throw new IllegalStateException("Cipher algorithm is required.");

        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE)
            throw new IllegalStateException("Cipher mode must be ENCRYPT_MODE or DECRYPT_MODE.");

        if (key == null)
            throw new IllegalStateException("Cipher key is required.");

        if (data == null)
            throw new IllegalStateException("Cipher data is required.");

        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            if (associatedData != null)
                cipher.updateAAD(associatedData);

            return new CipherResult(cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException(NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (AEADBadTagException e) {
            throw new IllegalArgumentException("Authentication failed: the key, parameters, associated data, or ciphertext is invalid.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Invalid input length for transformation: " + algorithmName, e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("Cipher operation failed because the key, padding, or ciphertext is invalid.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter: " + algorithmName, e);
        }
    }

    /**
     * Performs a cipher operation on UTF-8 text.
     *
     * <p>The supplied string is converted to UTF-8 bytes and then delegated to
     * {@link #doCipher(int, byte[], AlgorithmParameterSpec, byte[])}.</p>
     *
     * <p>This overload is primarily intended for encrypting textual plaintext.
     * Binary ciphertext should normally be supplied as raw bytes or through
     * {@link #doCipherFromBase64(int, String, AlgorithmParameterSpec, byte[])}.</p>
     *
     * @param mode           cipher operation mode
     * @param dataStr        input text, encoded as UTF-8 before processing
     * @param spec           optional algorithm-specific parameters; may be {@code null}
     * @param associatedData optional additional authenticated data (AAD);
     *                       may be {@code null}
     * @return the cipher result
     * @throws NullPointerException     if {@code dataStr} is {@code null}
     * @throws IllegalStateException    if the cipher configuration is invalid
     * @throws IllegalArgumentException if the cipher operation fails
     */
    public CipherResult doCipher(int mode, String dataStr, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(dataStr, "doCipher.dataStr");

        return doCipher(mode, new StringBytes(dataStr).getUTF8_Bytes(), spec, associatedData);
    }

    /**
     * Performs a cipher operation on Base64-encoded binary input.
     *
     * <p>The supplied Base64 value is decoded into raw bytes before being
     * delegated to
     * {@link #doCipher(int, byte[], AlgorithmParameterSpec, byte[])}.</p>
     *
     * <p>This overload is particularly useful for decrypting ciphertext
     * transported through text-based protocols such as JSON, HTTP or MQTT.</p>
     *
     * @param mode           cipher operation mode
     * @param base64Str      Base64-encoded input data
     * @param spec           optional algorithm-specific parameters; may be {@code null}
     * @param associatedData optional additional authenticated data (AAD);
     *                       may be {@code null}
     * @return the cipher result
     * @throws NullPointerException     if {@code base64Str} is {@code null}
     * @throws IllegalArgumentException if the Base64 value is invalid or the
     *                                  cipher operation fails
     * @throws IllegalStateException    if the cipher configuration is invalid
     */
    public CipherResult doCipherFromBase64(int mode, String base64Str, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(base64Str, "doCipher.base64Str");

        return doCipher(mode, new Base64Utils(base64Str).decode(), spec, associatedData);
    }

    /**
     * Generates cryptographically secure random bytes.
     *
     * <p>The random data is generated using {@link RandomTools#RANDOM}. This
     * method may be used to create cryptographic nonces, initialization vectors,
     * salts or secret-key material where an appropriate random value is
     * required.</p>
     *
     * @param length number of bytes to generate
     * @return a newly allocated array containing cryptographically secure random bytes
     * @throws IllegalArgumentException if {@code length} is zero or negative
     */
    public static byte[] randomBytes(int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length must be greater than zero.");

        byte[] bytes = new byte[length];
        RandomTools.RANDOM.nextBytes(bytes);

        return bytes;
    }

    /**
     * Error-message prefix used when the requested cipher transformation is
     * unavailable from the installed JCA/JCE providers.
     */
    public final static String NO_SUCH_ALGORITHM = "Unsupported cryptographic algorithm or transformation:";
}
