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
package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.cryptography.Constant;
import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Objects;

/**
 * Verifies RSA digital signatures using a configured signature algorithm
 * and public key.
 *
 * <p>The default signature algorithm is {@link Constant#SHA256_RSA}, which
 * typically represents {@code SHA256withRSA}. A different signature algorithm
 * may be supplied through the all-arguments constructor when compatibility
 * with another RSA signature scheme is required.</p>
 *
 * <p>This class supports verification of:</p>
 *
 * <ul>
 *     <li>binary input data</li>
 *     <li>UTF-8 string input data</li>
 *     <li>raw binary signature bytes</li>
 *     <li>Base64-encoded signature strings</li>
 * </ul>
 *
 * <p>String data is always converted to bytes using UTF-8 so that signature
 * verification is independent of the platform default charset.</p>
 *
 * <p>The verification operation uses the standard JCA
 * {@link Signature} API:</p>
 *
 * <ol>
 *     <li>Create a {@link Signature} instance for the configured algorithm.</li>
 *     <li>Initialize it with the configured public key.</li>
 *     <li>Supply the original data.</li>
 *     <li>Verify the supplied signature bytes.</li>
 * </ol>
 *
 * <p>A return value of {@code false} means the supplied signature does not
 * match the supplied data and public key. Invalid configuration, malformed
 * input, unsupported algorithms, or unusable keys are reported as
 * exceptions.</p>
 *
 * <p>Instances are immutable with respect to the public key, but the
 * signature algorithm field may be supplied explicitly through the generated
 * all-arguments constructor.</p>
 */
@AllArgsConstructor
public class DoVerify {
    /**
     * Signature algorithm used for verification.
     *
     * <p>The default value is {@link Rsa#SHA256_RSA}. The value is passed
     * directly to {@link Signature#getInstance(String)}.</p>
     */
    private String algorithmName = Rsa.SHA256_RSA;

    /**
     * Public key used to verify digital signatures.
     *
     * <p>The key must correspond to the private key that generated the
     * signature.</p>
     */
    private final PublicKey publicKey;

    /**
     * Creates a verifier using the default signature algorithm and the given
     * public key.
     *
     * @param publicKey the public key used for signature verification
     * @throws NullPointerException if {@code publicKey} is {@code null}
     */
    public DoVerify(PublicKey publicKey) {
        this.publicKey = Objects.requireNonNull(publicKey, "Verify2.publicKey");
    }

    /**
     * Creates a verifier from a Base64- or PEM-encoded public key string.
     *
     * <p>The supplied key is restored using
     * {@link RestoreKey#restorePublicKey(String)} with public-key mode enabled.
     * The default signature algorithm {@link Constant#SHA256_RSA} is used.</p>
     *
     * <p>The supported public-key encoding depends on
     * {@link RestoreKey#restorePublicKey(String)}. Typically, this includes X.509 SubjectPublicKeyInfo / PEM values using
     * {@code -----BEGIN PUBLIC KEY-----} and
     * {@code -----END PUBLIC KEY-----} boundaries.</p>
     *
     * @param publicKeyStr the Base64- or PEM-encoded public key
     * @throws NullPointerException     if {@code publicKeyStr} is {@code null}
     * @throws IllegalArgumentException if the key encoding is invalid or
     *                                  cannot be restored as a public key
     */
    public DoVerify(String publicKeyStr) {
        this(RestoreKey.restorePublicKey(Objects.requireNonNull(publicKeyStr, "Verify2.publicKeyStr")));
    }

    /**
     * Verifies a Base64-encoded digital signature against binary input data.
     *
     * <p>The supplied Base64 text is decoded to raw signature bytes and then
     * verified using {@link #verify(byte[], byte[])}.</p>
     *
     * @param data            the original binary data
     * @param signatureBase64 the Base64-encoded signature
     * @return {@code true} if the signature is valid for the supplied data and public key; {@code false} otherwise
     * @throws NullPointerException     if {@code signatureBase64} is {@code null}
     * @throws IllegalArgumentException if the Base64 signature is invalid,
     *                                  the configured algorithm is invalid,
     *                                  the public key is invalid, or required
     *                                  verification data is missing
     * @throws IllegalStateException    if the underlying signature engine
     *                                  fails during verification
     */
    public boolean verify(byte[] data, String signatureBase64) {
        Objects.requireNonNull(signatureBase64, "verify.signatureBase64");
        byte[] signatureData = new Base64Utils(signatureBase64).decode();

        return verify(data, signatureData);
    }

    /**
     * Verifies a Base64-encoded digital signature against UTF-8 text.
     *
     * <p>The input string is converted to bytes using
     * {@link StandardCharsets#UTF_8} before verification.</p>
     *
     * @param dataStr         the original text
     * @param signatureBase64 the Base64-encoded signature
     * @return {@code true} if the signature is valid; {@code false} otherwise
     * @throws NullPointerException     if {@code dataStr} or {@code signatureBase64} is {@code null}
     * @throws IllegalArgumentException if the Base64 signature, algorithm,
     *                                  public key, or verification input is invalid
     * @throws IllegalStateException    if the underlying signature engine
     *                                  fails during verification
     */
    public boolean verify(String dataStr, String signatureBase64) {
        Objects.requireNonNull(dataStr, "verify.dataStr");

        return verify(dataStr.getBytes(StandardCharsets.UTF_8), signatureBase64);
    }

    /**
     * Verifies raw signature bytes against UTF-8 text.
     *
     * <p>The supplied text is converted to UTF-8 bytes and then passed to
     * {@link #verify(byte[], byte[])}.</p>
     *
     * @param dataStr       the original text
     * @param signatureData the raw binary signature
     * @return {@code true} if the signature is valid; {@code false} otherwise
     * @throws NullPointerException     if {@code dataStr} is {@code null}
     * @throws IllegalArgumentException if the signature data, algorithm, public key, or verification input is invalid
     * @throws IllegalStateException    if the underlying signature engine fails during verification
     */
    public boolean verify(String dataStr, byte[] signatureData) {
        Objects.requireNonNull(dataStr, "verify.dataStr");

        return verify(dataStr.getBytes(StandardCharsets.UTF_8), signatureData);
    }

    /**
     * Verifies raw digital-signature bytes against binary input data.
     *
     * <p>This is the core verification method. A new JCA {@link Signature}
     * instance is created for each invocation. The configured public key is
     * used to initialize the verifier, the supplied data is processed, and
     * the supplied signature bytes are then checked.</p>
     *
     * <p>A normal signature mismatch is not treated as an exception.
     * {@link Signature#verify(byte[])} returns {@code false} when the
     * signature does not authenticate the supplied data.</p>
     *
     * @param data          the original binary data; an empty byte array is  valid
     * @param signatureData the raw binary signature bytes
     * @return {@code true} if the signature is valid for the supplied data and
     * configured public key; {@code false} if verification fails
     * because the signature does not match
     * @throws IllegalArgumentException if the algorithm name is missing or
     *                                  unsupported, the public key is invalid,
     *                                  {@code data} is {@code null}, or
     *                                  {@code signatureData} is {@code null}
     * @throws IllegalStateException    if the signature engine fails while
     *                                  processing or verifying the signature
     */
    public boolean verify(byte[] data, byte[] signatureData) {
        if (ObjectHelper.isEmptyText(algorithmName))
            throw new IllegalArgumentException("Signature algorithm is required.");

        if (data == null)
            throw new IllegalArgumentException("Data to verify is required.");

        if (signatureData == null)
            throw new IllegalArgumentException("Signature data is required.");

        if (publicKey == null)
            throw new IllegalArgumentException("Public key is required.");

        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initVerify(publicKey);
            signature.update(data);

            return signature.verify(signatureData);
        } catch (SignatureException e) {
            throw new IllegalStateException("Signature verification failed.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Public Key", e);
        }
    }
}
