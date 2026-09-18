/**
 * Provides RSA key generation, key restoration and PEM encoding, encryption, decryption,
 * digital-signature, and signature-verification utilities.
 *
 * <p>{@link com.ajaxjs.util.cryptography.rsa.Rsa Rsa} uses RSA-OAEP with SHA-256 for its
 * primary encryption API. RSA is suitable only for short values, such as an AES key; encrypt
 * larger payloads with an authenticated symmetric cipher and use RSA to protect that symmetric
 * key. The package also retains SHA-1 OAEP compatibility methods for protocols that explicitly
 * require that scheme; they are not interchangeable with the primary SHA-256 OAEP methods.</p>
 *
 * <p>{@link com.ajaxjs.util.cryptography.rsa.DoSignature DoSignature} and
 * {@link com.ajaxjs.util.cryptography.rsa.DoVerify DoVerify} implement RSA digital signatures,
 * which provide integrity and authenticity but do not encrypt the signed data. Keys can be
 * supplied as JCA key objects or restored from supported Base64 and PEM representations through
 * {@link com.ajaxjs.util.cryptography.rsa.RestoreKey RestoreKey}; {@link
 * com.ajaxjs.util.cryptography.rsa.PemUtils PemUtils} formats keys as PEM text.</p>
 *
 * <p>Base64 and PEM are encodings only, not encryption. Keep private keys confidential and
 * select key sizes and algorithms appropriate to the application's security requirements.</p>
 */
package com.ajaxjs.util.cryptography.rsa;
