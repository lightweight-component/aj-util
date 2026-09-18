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

import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.SecretKeyMgr;

import javax.crypto.Cipher;

/**
 * Provides legacy AES encryption and decryption for compatibility with data
 * produced by the former AES implementation.
 *
 * <p>This class derives an AES secret key from a string using the legacy
 * {@link SecretKeyMgr} random-based key-generation mechanism and performs
 * cipher operations using the {@code AES} transformation.</p>
 *
 * <p>The encrypted result is represented as a hexadecimal string. During
 * decryption, the hexadecimal ciphertext is converted back to its original
 * binary representation before being passed to the AES cipher. The resulting
 * plaintext bytes are interpreted as UTF-8 text.</p>
 *
 * <p><strong>Legacy compatibility only:</strong> the transformation
 * {@code AES} does not explicitly specify the cipher mode or padding and
 * therefore relies on the defaults selected by the installed JCA/JCE provider.
 * Common providers typically interpret it as an ECB-based AES transformation.
 * ECB does not provide semantic security for structured or repeated plaintext
 * blocks and does not provide authentication or integrity protection.</p>
 *
 * <p>This class should therefore only be used to read or produce data that must
 * remain compatible with the historical encryption format. New encrypted data
 * should use an authenticated encryption mode such as AES-GCM.</p>
 *
 * <p>The same key string and compatible cryptographic provider behavior are
 * required to decrypt data previously encrypted by this implementation.</p>
 *
 * @see DoCipher
 * @see SecretKeyMgr
 * @see AesGcm
 */
public class AesLegacy extends DoCipher {
    /**
     * Creates a legacy AES cipher using a secret key derived from the supplied
     * string.
     *
     * <p>The string is used by the legacy {@link SecretKeyMgr} key-generation
     * mechanism. It is not used directly as the raw AES key bytes.</p>
     *
     * @param key string used by the legacy AES key-generation mechanism
     * @throws NullPointerException     if {@code key} is {@code null}
     * @throws IllegalArgumentException if the required random or AES algorithm
     *                                  cannot be initialized
     */

    public AesLegacy(String key) {
        super("AES/ECB/PKCS5Padding", SecretKeyMgr.getSecretKey(SecretKeyMgr.getRandom(key)));
    }

    /**
     * Encrypts UTF-8 text using the legacy AES implementation.
     *
     * <p>The raw encrypted bytes are encoded as hexadecimal text before being
     * returned.</p>
     *
     * <p>This method exists for legacy compatibility. New encrypted data should
     * normally use AES-GCM instead.</p>
     *
     * @param data plaintext to encrypt, interpreted as UTF-8 text
     * @return hexadecimal representation of the encrypted bytes
     * @throws NullPointerException     if {@code data} is {@code null}
     * @throws IllegalArgumentException if the key, input or cipher operation is invalid
     * @throws IllegalStateException    if the cipher operation cannot be completed
     */
    public String encrypt(String data) {
        return doCipher(Cipher.ENCRYPT_MODE, data, null, null).toHex();
    }

    /**
     * Decrypts hexadecimal ciphertext produced by {@link #encrypt(String)}.
     *
     * <p>The hexadecimal input is first converted to raw encrypted bytes and
     * then decrypted using the same legacy AES key-generation and cipher
     * configuration. The decrypted bytes are interpreted as UTF-8 text.</p>
     *
     * @param cipherText hexadecimal ciphertext produced by the legacy AES implementation
     * @return decrypted plaintext interpreted as UTF-8 text
     * @throws NullPointerException     if {@code cipherText} is {@code null}
     * @throws IllegalArgumentException if the hexadecimal input, key or ciphertext is invalid
     * @throws IllegalStateException    if the cipher operation cannot be completed
     */
    public String decrypt(String cipherText) {
        return doCipher(Cipher.DECRYPT_MODE, StringBytes.hexToBytes(cipherText), null, null).toUtf8();
    }
}
