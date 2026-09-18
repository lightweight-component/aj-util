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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the raw result of a cryptographic cipher operation.
 *
 * <p>The underlying result is binary data. It may be obtained directly as
 * bytes or represented as UTF-8, Base64 or hexadecimal text.</p>
 *
 * <p>Encrypted data is arbitrary binary data and should normally be represented
 * using {@link #toBase64()} or {@link #toHex()}. {@link #toUtf8()} is primarily
 * intended for decrypted plaintext known to contain UTF-8 encoded text.</p>
 */
@RequiredArgsConstructor
public class CipherResult {
    /**
     * Raw result bytes produced by the cipher operation.
     */
    @Getter
    private final byte[] result;

    /**
     * Interprets the result bytes as UTF-8 text.
     *
     * <p>This method is primarily intended for decrypted plaintext known to
     * contain UTF-8 encoded text. Arbitrary encrypted binary data should normally
     * be represented using {@link #toBase64()} or {@link #toHex()}.</p>
     *
     * @return the result interpreted as a UTF-8 string
     */
    public String toUtf8() {
        return new StringBytes(result).getUTF8_String();
    }

    /**
     * Encodes the result bytes as Base64.
     *
     * @return Base64-encoded result
     */
    public String toBase64() {
        return new Base64Utils(result).encodeAsString();
    }

    /**
     * Encodes the result bytes as hexadecimal text.
     *
     * @return hexadecimal representation of the result
     */
    public String toHex() {
        return StringBytes.bytesToHex(result);
    }
}
