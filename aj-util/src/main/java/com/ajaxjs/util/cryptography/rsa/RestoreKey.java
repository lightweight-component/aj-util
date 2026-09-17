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
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.io.DataWriter;
import com.ajaxjs.util.io.FileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * Provides utilities for restoring and loading RSA public and private keys.
 *
 * <p>This class supports RSA keys supplied either as raw Base64 text or as
 * PEM-encoded text.</p>
 *
 * <p>The following key formats are supported:</p>
 *
 * <ul>
 *     <li>
 *         RSA public keys encoded as X.509 SubjectPublicKeyInfo and optionally
 *         wrapped in {@code -----BEGIN PUBLIC KEY-----} /
 *         {@code -----END PUBLIC KEY-----} PEM boundaries
 *     </li>
 *     <li>
 *         RSA private keys encoded as PKCS#8 PrivateKeyInfo and optionally
 *         wrapped in {@code -----BEGIN PRIVATE KEY-----} /
 *         {@code -----END PRIVATE KEY-----} PEM boundaries
 *     </li>
 * </ul>
 *
 * <p>The following PKCS#1 PEM formats are intentionally not supported:</p>
 *
 * <pre>
 * -----BEGIN RSA PUBLIC KEY-----
 * ...
 * -----END RSA PUBLIC KEY-----
 *
 * -----BEGIN RSA PRIVATE KEY-----
 * ...
 * -----END RSA PRIVATE KEY-----
 * </pre>
 *
 * <p>Although these are also PEM representations, their underlying DER
 * structures differ from X.509 SubjectPublicKeyInfo and PKCS#8. They therefore
 * cannot be directly processed by {@link X509EncodedKeySpec} or
 * {@link PKCS8EncodedKeySpec}.</p>
 *
 * <p>PEM input is normalized by removing the expected PEM boundaries and
 * whitespace before Base64 decoding.</p>
 */
public class RestoreKey {
    /**
     * Restores an RSA public key from Base64 or PEM text.
     *
     * <p>The decoded key material must use the X.509 SubjectPublicKeyInfo
     * structure expected by {@link X509EncodedKeySpec}.</p>
     *
     * <p>Supported PEM form:</p>
     *
     * <pre>
     * -----BEGIN PUBLIC KEY-----
     * ...
     * -----END PUBLIC KEY-----
     * </pre>
     *
     * <p>A plain Base64 string containing the same DER-encoded key material is
     * also accepted.</p>
     *
     * <p>PKCS#1 RSA public keys using
     * {@code -----BEGIN RSA PUBLIC KEY-----} are not supported.</p>
     *
     * @param key the Base64- or PEM-encoded an RSA public key
     * @return the restored RSA public key
     * @throws NullPointerException     if {@code key} is {@code null}
     * @throws IllegalArgumentException if the key is empty, is not valid Base64, uses an unsupported PKCS#1
     *                                  format, or does not contain a valid X.509 RSA public key
     * @throws IllegalStateException    if the RSA {@link KeyFactory} algorithm is unavailable in the current runtime
     */
    public static PublicKey restorePublicKey(String key) {
        Objects.requireNonNull(key, "restorePublicKey.key");
        byte[] bytes = decodePemOrBase64(key, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");

        try {
            KeyFactory factory = KeyFactory.getInstance(Rsa.RSA);

            return factory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid RSA public key encoding. Expected X.509 SubjectPublicKeyInfo.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(DoCipher.NO_SUCH_ALGORITHM + Rsa.RSA, e);
        }
    }

    /**
     * Restores an RSA private key from Base64 or PEM text.
     *
     * <p>The decoded key material must use the PKCS#8 PrivateKeyInfo structure
     * expected by {@link PKCS8EncodedKeySpec}.</p>
     *
     * <p>Supported PEM form:</p>
     *
     * <pre>
     * -----BEGIN PRIVATE KEY-----
     * ...
     * -----END PRIVATE KEY-----
     * </pre>
     *
     * <p>A plain Base64 string containing the same DER-encoded PKCS#8 key
     * material is also accepted.</p>
     *
     * <p>PKCS#1 RSA private keys using
     * {@code -----BEGIN RSA PRIVATE KEY-----} are not supported.</p>
     *
     * @param key the Base64- or PEM-encoded an RSA private key
     * @return the restored RSA private key
     * @throws NullPointerException     if {@code key} is {@code null}
     * @throws IllegalArgumentException if the key is empty, is not valid
     *                                  Base64, uses an unsupported PKCS#1
     *                                  format, or does not contain a valid
     *                                  PKCS#8 RSA private key
     * @throws IllegalStateException    if the RSA {@link KeyFactory} algorithm
     *                                  is unavailable in the current runtime
     */
    public static PrivateKey restorePrivateKey(String key) {
        Objects.requireNonNull(key, "restorePrivateKey.key");
        byte[] bytes = decodePemOrBase64(key, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

        try {
            KeyFactory factory = KeyFactory.getInstance(Rsa.RSA);

            return factory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid RSA private key encoding. Expected PKCS#8.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(DoCipher.NO_SUCH_ALGORITHM + Rsa.RSA, e);
        }
    }

    /**
     * Decodes RSA key material supplied either as PEM text or as plain Base64.
     *
     * <p>The supplied PEM boundaries are removed when present. All whitespace
     * is then removed before Base64 decoding.</p>
     *
     * <p>This helper deliberately rejects PKCS#1 PEM forms such as
     * {@code RSA PUBLIC KEY} and {@code RSA PRIVATE KEY}, because the
     * corresponding DER structures are not directly compatible with
     * {@link X509EncodedKeySpec} or {@link PKCS8EncodedKeySpec}.</p>
     *
     * @param key   the PEM- or Base64-encoded key
     * @param begin the expected PEM begin boundary
     * @param end   the expected PEM end boundary
     * @return the decoded DER key bytes
     * @throws IllegalArgumentException if the key is empty, uses an unsupported
     *                                  PKCS#1 PEM format, or contains invalid Base64 data
     */
    private static byte[] decodePemOrBase64(String key, String begin, String end) {
        String value = key.trim();

        if (value.contains("-----BEGIN RSA PUBLIC KEY-----") || value.contains("-----BEGIN RSA PRIVATE KEY-----"))
            throw new IllegalArgumentException("PKCS#1 RSA keys are not supported.");

        value = value.replace(begin, "").replace(end, "").replaceAll("\\s", "");

        if (value.isEmpty())
            throw new IllegalArgumentException("RSA key content is empty.");

        try {
            return new Base64Utils(value).decode();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid Base64 RSA key.", e);
        }
    }

    /**
     * Loads an RSA private key from an input stream using UTF-8.
     *
     * <p>The stream content may contain either a plain Base64-encoded PKCS#8
     * private key or a PEM-encoded private key using
     * {@code -----BEGIN PRIVATE KEY-----} boundaries.</p>
     *
     * <p>The decoded text is passed to
     * {@link #restorePrivateKey(String)} for validation and conversion to a
     * {@link PrivateKey}.</p>
     *
     * @param in the input stream containing the private-key text
     * @return the restored RSA private key
     * @throws IllegalArgumentException if the private-key encoding is invalid or unsupported
     * @throws UncheckedIOException     if an I/O error occurs while reading the stream
     */
    public static PrivateKey loadPrivateKey(InputStream in) {
        return loadPrivateKey(in, CommonConstant.UTF8);
    }

    /**
     * Loads an RSA private key from an input stream using the specified
     * character encoding.
     *
     * <p>The stream is read completely into text and then passed to
     * {@link #restorePrivateKey(String)}. The text may contain either raw
     * Base64 PKCS#8 data or a {@code PRIVATE KEY} PEM document.</p>
     *
     * <p>The supplied character encoding applies only to the textual
     * representation of the key. The decoded key itself is binary DER data.</p>
     *
     * @param in      the input stream containing the private-key text
     * @param charset the character-set name used to decode the stream content
     * @return the restored RSA private key
     * @throws IllegalArgumentException if the private-key text, Base64 data,
     *                                  PKCS#8 encoding, or character-set name is invalid
     * @throws UncheckedIOException     if an I/O error occurs while reading the stream
     */
    public static PrivateKey loadPrivateKey(InputStream in, String charset) {
        String privateKey;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(2048)) {
            new DataWriter(out).write(in);
            privateKey = out.toString(charset);
        } catch (IOException e) {
            throw new UncheckedIOException("Invalid private key.", e);
        }

        return restorePrivateKey(privateKey);
    }

    /**
     * Loads an RSA private key from a text file.
     *
     * <p>The file may contain either a Base64-encoded PKCS#8 private key or a
     * PEM document using the following form:</p>
     *
     * <pre>
     * -----BEGIN PRIVATE KEY-----
     * ...
     * -----END PRIVATE KEY-----
     * </pre>
     *
     * <p>The file content is read through {@link FileHelper} and then passed
     * to {@link #restorePrivateKey(String)}.</p>
     *
     * @param filePath the path of the file containing the private key
     * @return the restored RSA private key
     * @throws NullPointerException     if {@code filePath} is {@code null}
     * @throws IllegalArgumentException if the file contains an invalid or
     *                                  unsupported private-key encoding
     * @throws RuntimeException         if the file cannot be read
     */
    public static PrivateKey loadPrivateKey(String filePath) {
        String fileContent = new FileHelper(filePath).getFileContent();

        return restorePrivateKey(fileContent);
    }
}
