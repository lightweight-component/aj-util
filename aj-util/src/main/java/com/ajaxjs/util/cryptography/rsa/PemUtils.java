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
     * Converts a private key to PEM text.
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
     * Converts a public key to PEM text.
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
