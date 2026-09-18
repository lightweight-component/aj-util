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
package com.ajaxjs.util.cryptography.aes;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.CipherResult;
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.SecretKeyMgr;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

/**
 * Provides AES encryption and decryption using CBC mode with PKCS#5 padding.
 *
 * <p>The cipher transformation used by this implementation is
 * {@code AES/CBC/PKCS5Padding}. AES-CBC operates on 16-byte blocks and therefore
 * requires a 16-byte initialization vector (IV).</p>
 *
 * <p>A new unpredictable IV should be generated for every encryption operation
 * performed with the same key. The IV is not secret and may be stored or
 * transmitted together with the ciphertext, but it must be available during
 * decryption.</p>
 *
 * <p><strong>Security notice:</strong> AES-CBC provides confidentiality only.
 * It does not provide integrity or authenticity protection. Modification of
 * ciphertext cannot reliably be detected by CBC itself. For new protocols where
 * authenticated encryption is required, AES-GCM should normally be preferred.</p>
 *
 * <p>The AES key may be supplied either as raw bytes or as a Base64-encoded
 * string. Valid AES key lengths are 16, 24 and 32 bytes, corresponding to
 * AES-128, AES-192 and AES-256 respectively. Actual key-length validation is
 * ultimately performed by the configured JCA provider.</p>
 *
 * <p>Instances retain the AES key but do not retain cipher state. A new
 * {@link Cipher} instance is created by {@link DoCipher} for each encryption
 * or decryption operation.</p>
 *
 * @see DoCipher
 * @see IvParameterSpec
 * @see AesGcm
 */
public class AesCbc extends DoCipher {
    /**
     * JCA/JCE transformation used for AES-CBC encryption and decryption.
     */
    public final static String AES_CBC = "AES/CBC/PKCS5Padding";

    /**
     * AES block size and required CBC initialization-vector length, in bytes.
     *
     * <p>AES always uses a 128-bit block size regardless of whether the key
     * length is 128, 192 or 256 bits. Consequently, the CBC IV must always
     * contain exactly 16 bytes.</p>
     */
    private static final int CBC_IV_LENGTH = 16;

    /**
     * Creates an AES-CBC cipher using raw AES key bytes.
     *
     * <p>The supplied bytes are wrapped in a {@link SecretKeySpec} using the
     * AES algorithm. Standard AES key lengths are 16, 24 and 32 bytes.</p>
     *
     * @param key raw AES key bytes
     * @throws NullPointerException     if {@code key} is {@code null}
     * @throws IllegalArgumentException when the key length is rejected by the cryptographic provider during use
     */
    public AesCbc(byte[] key) {
        super(AES_CBC, new SecretKeySpec(key, SecretKeyMgr.AES));
    }

    /**
     * Creates an AES-CBC cipher from a Base64-encoded AES key.
     *
     * <p>The supplied string is Base64-decoded to obtain the raw AES key bytes
     * and then delegated to {@link #AesCbc(byte[])}.</p>
     *
     * @param base64Key Base64-encoded AES key
     * @throws NullPointerException     if {@code key} is {@code null}
     * @throws IllegalArgumentException if the value is not valid Base64 or the decoded key is not valid for AES
     */
    public AesCbc(String base64Key) {
        this(new Base64Utils(Objects.requireNonNull(base64Key, "DoAes.key")).decode());
    }

    /**
     * Encrypts UTF-8 text using AES-CBC with a newly generated initialization vector.
     *
     * <p>A cryptographically secure 16-byte IV is generated for this operation.
     * The returned {@link AesCipherResult} contains both the encrypted bytes and
     * the IV required for subsequent decryption.</p>
     *
     * <p>The IV is not secret and may be transmitted alongside the ciphertext.
     * A new IV is generated for every invocation.</p>
     *
     * @param data plaintext text to encrypt; interpreted as UTF-8
     * @return encryption result containing the ciphertext and generated IV
     * @throws NullPointerException     if {@code data} is {@code null}
     * @throws IllegalArgumentException if the AES key or cipher parameters are invalid
     * @throws IllegalStateException    if the cipher cannot be initialized or executed
     */
    public AesCipherResult encrypt(String data) {
        return encrypt(data, randomBytes(CBC_IV_LENGTH));
    }

    /**
     * Encrypts UTF-8 text using AES-CBC with the supplied initialization vector.
     *
     * <p>The IV must contain exactly 16 bytes because AES has a fixed 128-bit
     * block size. The same IV should not be reused with the same AES key for
     * independent messages.</p>
     *
     * <p>The returned {@link AesCipherResult} contains the ciphertext and the
     * supplied IV so that both values can be transported or stored together.</p>
     *
     * <p>AES-CBC does not authenticate the ciphertext. Applications requiring
     * confidentiality together with integrity and authenticity protection
     * should normally use AES-GCM instead.</p>
     *
     * @param data plaintext text to encrypt; interpreted as UTF-8
     * @param iv   16-byte AES-CBC initialization vector
     * @return encryption result containing the ciphertext and supplied IV
     * @throws NullPointerException     if {@code data} or {@code iv} is {@code null}
     * @throws IllegalArgumentException if {@code iv} is not exactly 16 bytes or the key/cipher parameters are invalid
     * @throws IllegalStateException    if the cipher cannot be initialized or executed
     */
    public AesCipherResult encrypt(String data, byte[] iv) {
        Objects.requireNonNull(iv, "AesCbc.iv");

        if (iv.length != CBC_IV_LENGTH)
            throw new IllegalArgumentException("AES-CBC IV must contain exactly " + CBC_IV_LENGTH + " bytes.");

        CipherResult result = doCipher(Cipher.ENCRYPT_MODE, data, new IvParameterSpec(iv), null);
        AesCipherResult encrypted = new AesCipherResult(result.getResult());
        encrypted.setNonce(new CipherResult(iv));

        return encrypted;
    }

    /**
     * Decrypts Base64-encoded AES-CBC ciphertext using a Base64-encoded IV.
     *
     * <p>The ciphertext is Base64-decoded before decryption. The supplied
     * {@code iv} value must represent the same 16-byte initialization vector
     * that was used during encryption.</p>
     *
     * <p>The decrypted bytes are interpreted as UTF-8 text.</p>
     *
     * @param data Base64-encoded AES-CBC ciphertext
     * @param iv   Base64-encoded 16-byte initialization vector
     * @return decrypted plaintext interpreted as UTF-8
     * @throws NullPointerException     if {@code data} or {@code iv} is {@code null}
     * @throws IllegalArgumentException if either value is invalid Base64, the IV
     *                                  is not 16 bytes, or decryption fails
     * @throws IllegalStateException    if the cipher cannot be initialized or executed
     */
    public String decrypt(String data, String iv) {
        Objects.requireNonNull(data, "AesCbc.data");
        Objects.requireNonNull(iv, "AesCbc.iv");

        byte[] ivBytes = new Base64Utils(iv).decode();

        if (ivBytes.length != CBC_IV_LENGTH)
            throw new IllegalArgumentException("AES-CBC IV must contain exactly " + CBC_IV_LENGTH + " bytes.");

        CipherResult result = doCipherFromBase64(Cipher.DECRYPT_MODE, data, new IvParameterSpec(ivBytes), null);

        return result.toUtf8();
    }
}
