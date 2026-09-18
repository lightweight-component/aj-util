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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

/**
 * Provides authenticated AES encryption and decryption using Galois/Counter
 * Mode (GCM).
 *
 * <p>The cipher transformation used by this implementation is
 * {@code AES/GCM/NoPadding}. AES-GCM provides both confidentiality and
 * authentication, allowing modification of the ciphertext or associated data
 * to be detected during decryption.</p>
 *
 * <p>Each encryption operation normally generates a new random 12-byte
 * (96-bit) nonce. A nonce must not be reused for different encryption
 * operations performed with the same AES key. Reusing a nonce with the same
 * key can seriously compromise the security of AES-GCM.</p>
 *
 * <p>The GCM authentication tag length used by this implementation is
 * 128 bits (16 bytes). The JCA/JCE cipher output contains the ciphertext
 * followed by the authentication tag:</p>
 *
 * <pre>
 * [ciphertext][16-byte authentication tag]
 * </pre>
 *
 * <p>The nonce is not included in the raw cipher result. Instead,
 * {@link #encrypt(String)} and its overloads return an
 * {@link AesCipherResult} containing the encrypted result and the nonce
 * separately. The same nonce must be supplied when decrypting the ciphertext.</p>
 *
 * <p>Additional authenticated data (AAD) may optionally be supplied. AAD is
 * authenticated but not encrypted. If AAD is supplied during encryption, the
 * exact same bytes must be supplied during decryption or authentication will
 * fail.</p>
 *
 * <p>The AES key may be supplied either as raw bytes or as a Base64-encoded
 * string. Standard AES key lengths are 16, 24 and 32 bytes, corresponding to
 * AES-128, AES-192 and AES-256 respectively.</p>
 *
 * <p>Instances retain the AES key but do not retain nonce or cipher state.
 * A new cipher operation is performed for each encryption or decryption call.</p>
 *
 * @see DoCipher
 * @see GCMParameterSpec
 * @see AesCipherResult
 */
public class AesGcm extends DoCipher {
    /**
     * JCA/JCE transformation used for AES-GCM encryption and decryption.
     */
    public final static String AES_GCM = "AES/GCM/NoPadding";

    /**
     * Required nonce length for AES-GCM operations performed by this class.
     *
     * <p>A 12-byte (96-bit) nonce is the standard recommended size for GCM.</p>
     */
    static final int GCM_NONCE_LENGTH = 12;

    /**
     * Length of the AES-GCM authentication tag, in bits.
     *
     * <p>The configured 128-bit tag occupies 16 bytes in the encrypted output.</p>
     */
    static final int GCM_TAG_LENGTH = 128;

    /**
     * Creates an AES-GCM cipher using raw AES key bytes.
     *
     * <p>Standard AES key lengths are 16, 24 and 32 bytes, corresponding to
     * AES-128, AES-192 and AES-256 respectively.</p>
     *
     * @param key raw AES key bytes
     * @throws NullPointerException     if {@code key} is {@code null}
     * @throws IllegalArgumentException if the key length is invalid for AES
     */
    public AesGcm(byte[] key) {
        super(AES_GCM, new SecretKeySpec(key, SecretKeyMgr.AES));
    }

    /**
     * Creates an AES-GCM cipher from a Base64-encoded AES key.
     *
     * <p>The supplied value is Base64-decoded to obtain the raw AES key bytes
     * and then delegated to {@link #AesGcm(byte[])}.</p>
     *
     * @param base64Key Base64-encoded AES key
     * @throws NullPointerException     if {@code base64Key} is {@code null}
     * @throws IllegalArgumentException if the value is invalid Base64 or the
     *                                  decoded key is invalid for AES
     */
    public AesGcm(String base64Key) {
        this(new Base64Utils(Objects.requireNonNull(base64Key, "DoAes.base64Key")).decode());
    }

    /**
     * Encrypts UTF-8 text using AES-GCM.
     *
     * <p>A new cryptographically secure 12-byte nonce is generated for this
     * operation. No additional authenticated data (AAD) is used.</p>
     *
     * <p>The returned {@link AesCipherResult} contains:</p>
     *
     * <ul>
     *     <li>the AES-GCM ciphertext followed by the 16-byte authentication tag</li>
     *     <li>the generated 12-byte nonce</li>
     * </ul>
     *
     * @param data plaintext to encrypt, interpreted as UTF-8
     * @return encrypted result containing the ciphertext/tag and generated nonce
     * @throws NullPointerException     if {@code data} is {@code null}
     * @throws IllegalArgumentException if encryption parameters are invalid
     * @throws IllegalStateException    if the cipher operation cannot be completed
     */
    public AesCipherResult encrypt(String data) {
        return encrypt(data, null);
    }

    /**
     * Encrypts UTF-8 text using AES-GCM with optional additional authenticated
     * data (AAD).
     *
     * <p>A new cryptographically secure 12-byte nonce is generated for every
     * invocation.</p>
     *
     * <p>The associated data is authenticated but is not included in or
     * encrypted with the ciphertext. If non-null AAD is supplied here, the
     * exact same bytes must be supplied when decrypting the result.</p>
     *
     * @param data           plaintext to encrypt, interpreted as UTF-8
     * @param associatedData optional additional authenticated data; may be {@code null}
     * @return encrypted result containing the ciphertext/tag and generated nonce
     * @throws NullPointerException     if {@code data} is {@code null}
     * @throws IllegalArgumentException if encryption parameters are invalid
     * @throws IllegalStateException    if the cipher operation cannot be completed
     */
    public AesCipherResult encrypt(String data, byte[] associatedData) {
        byte[] nonce = randomBytes(GCM_NONCE_LENGTH);

        return encrypt(data, nonce, associatedData);
    }

    /**
     * Encrypts UTF-8 text using AES-GCM with an explicitly supplied nonce and
     * optional additional authenticated data (AAD).
     *
     * <p>The nonce must contain exactly 12 bytes (96 bits). The caller is
     * responsible for ensuring that the nonce is not reused for another
     * encryption operation with the same AES key.</p>
     *
     * <p>The returned cipher result contains the ciphertext followed by the
     * 128-bit GCM authentication tag. The nonce is stored separately in the
     * returned {@link AesCipherResult}.</p>
     *
     * <p>This overload is intended for protocols that manage or transport the
     * nonce explicitly. In most cases, {@link #encrypt(String)} or
     * {@link #encrypt(String, byte[])} should be preferred because those
     * methods generate a fresh random nonce automatically.</p>
     *
     * @param data           plaintext to encrypt, interpreted as UTF-8
     * @param nonce          12-byte AES-GCM nonce
     * @param associatedData optional additional authenticated data; may be {@code null}
     * @return encrypted result containing the ciphertext/tag and supplied nonce
     * @throws NullPointerException     if {@code data} or {@code nonce} is {@code null}
     * @throws IllegalArgumentException if {@code nonce} is not exactly 12 bytes
     *                                  or the cipher parameters are invalid
     * @throws IllegalStateException    if the cipher operation cannot be completed
     */
    public AesCipherResult encrypt(String data, byte[] nonce, byte[] associatedData) {
        Objects.requireNonNull(nonce, "AesGcm.nonce");

        if (nonce.length != GCM_NONCE_LENGTH)
            throw new IllegalArgumentException("AES-GCM nonce must contain exactly " + GCM_NONCE_LENGTH + " bytes.");

        AlgorithmParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        CipherResult result = doCipher(Cipher.ENCRYPT_MODE, data, spec, associatedData);
        AesCipherResult ar = new AesCipherResult(result.getResult());
        ar.setNonce(new CipherResult(nonce));

        return ar;
    }

    /**
     * Decrypts Base64-encoded AES-GCM ciphertext using the supplied nonce and
     * optional additional authenticated data (AAD).
     *
     * <p>The {@code cipherText} value must represent the complete AES-GCM
     * encrypted output:</p>
     *
     * <pre>
     * Base64([ciphertext][16-byte authentication tag])
     * </pre>
     *
     * <p>The nonce must be the same 12-byte value used during encryption. If
     * associated data was supplied during encryption, the exact same bytes must
     * also be supplied here.</p>
     *
     * <p>During decryption AES-GCM verifies the authentication tag. Decryption
     * fails if the key, nonce, ciphertext, authentication tag or associated data
     * has been modified or does not match the encryption operation.</p>
     *
     * <p>The successfully decrypted bytes are interpreted as UTF-8 text.</p>
     *
     * @param cipherText     Base64-encoded ciphertext and authentication tag
     * @param nonce          12-byte nonce used during encryption
     * @param associatedData additional authenticated data used during encryption;
     *                       may be {@code null} if no AAD was used
     * @return decrypted plaintext interpreted as UTF-8
     * @throws NullPointerException     if {@code cipherText} or {@code nonce} is {@code null}
     * @throws IllegalArgumentException if the nonce length is invalid, the
     *                                  Base64 ciphertext is invalid, or GCM
     *                                  authentication fails
     * @throws IllegalStateException    if the cipher operation cannot be completed
     */
    public String decrypt(String cipherText, byte[] nonce, byte[] associatedData) {
        Objects.requireNonNull(cipherText, "AesGcm.cipherText");
        Objects.requireNonNull(nonce, "AesGcm.nonce");

        if (nonce.length != GCM_NONCE_LENGTH)
            throw new IllegalArgumentException("AES-GCM nonce must contain exactly " + GCM_NONCE_LENGTH + " bytes.");

        AlgorithmParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

        return doCipherFromBase64(Cipher.DECRYPT_MODE, cipherText, spec, associatedData).toUtf8();
    }
}
