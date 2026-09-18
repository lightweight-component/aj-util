/**
 * Provides AES encryption helpers.
 *
 * <p>{@link com.ajaxjs.util.cryptography.aes.AesGcm AesGcm} provides authenticated encryption
 * with {@code AES/GCM/NoPadding} and should normally be used for new data. Its ciphertext must
 * be stored or transmitted together with the nonce; decryption also requires the identical
 * associated authenticated data, when one was used. A nonce must never be reused with the same
 * AES key.</p>
 *
 * <p>{@link com.ajaxjs.util.cryptography.aes.AesPbe AesPbe} derives an AES key from a password
 * with PBKDF2 and encrypts with AES-GCM. It prepends its generated nonce to the returned binary
 * ciphertext; the caller must persist the salt and iteration count separately. {@link
 * com.ajaxjs.util.cryptography.aes.AesCbc AesCbc} supports AES-CBC for interoperability, but
 * CBC does not authenticate ciphertext and should be used only where a compatible protocol
 * requires it.</p>
 *
 * <p>{@link com.ajaxjs.util.cryptography.aes.AesLegacy AesLegacy} exists for compatibility with
 * its historical AES format. Prefer AES-GCM for new applications. Base64 and hexadecimal values
 * returned by these helpers are encodings of binary data, not additional cryptographic
 * protection.</p>
 */
package com.ajaxjs.util.cryptography.aes;
