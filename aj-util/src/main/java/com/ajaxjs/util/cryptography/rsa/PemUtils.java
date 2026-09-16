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

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public class PemUtils {
    /**
     * Formats a Base64 string using 64 characters per line,
     * as commonly used in PEM-style Base64 data.
     *
     * @param base64 the Base64 string to format
     * @return the formatted Base64 string with line breaks
     */
    public static String formatPemBase64(String base64) {
        int len = base64.length();

        if (len <= 64)
            return base64;

        int lineBreaks = (len - 1) / 64;
        StringBuilder sb = new StringBuilder(len + lineBreaks);

        for (int i = 0; i < len; i += 64) {
            if (i > 0)
                sb.append('\n');

            sb.append(base64, i, Math.min(i + 64, len));
        }

        return sb.toString();
    }

    /**
     * Converts a private key to a PEM text.
     *
     * @param privateKey the private key to convert
     * @return the PEM-encoded private key
     * @throws NullPointerException if the private key is null
     */
    public static String privateKeyToPem(PrivateKey privateKey) {
        return privateKeyToPem(encodeKeyBase64(privateKey));
    }

    /**
     * Wraps a Base64-encoded private key in PEM boundaries.
     *
     * @param encoded the Base64-encoded private key
     * @return the PEM-encoded private key
     * @throws NullPointerException if the encoded key is null
     */
    public static String privateKeyToPem(String encoded) {
        return "-----BEGIN PRIVATE KEY-----\n" + formatPemBase64(encoded) + "\n-----END PRIVATE KEY-----";
    }

    /**
     * Converts a public key to a PEM text.
     *
     * @param publicKey the public key to convert
     * @return the PEM-encoded public key
     * @throws NullPointerException if the public key is null
     */

    public static String publicKeyToPem(PublicKey publicKey) {
        return publicKeyToPem(encodeKeyBase64(publicKey));
    }

    /**
     * Wraps a Base64-encoded public key in PEM boundaries.
     *
     * @param encoded the Base64-encoded public key
     * @return the PEM-encoded public key
     * @throws NullPointerException if the encoded key is null
     */
    public static String publicKeyToPem(String encoded) {
        return "-----BEGIN PUBLIC KEY-----\n" + formatPemBase64(encoded) + "\n-----END PUBLIC KEY-----";
    }

    public static String encodeKeyBase64(Key key) {
        return new Base64Utils(key.getEncoded()).encodeAsString();
    }
}
