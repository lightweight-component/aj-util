/**
 * Provides general cryptography utilities built on the Java Cryptography Architecture (JCA).
 *
 * <p>The package contains reusable cipher-operation support, encrypted-result and secret-key
 * helpers, AES and password-based-encryption entry points, and X.509 certificate utilities.
 * AES implementations are available in the {@code aes} subpackage; RSA encryption, key,
 * PEM, and signature utilities are available in the {@code rsa} subpackage.</p>
 *
 * <p>Use authenticated encryption such as AES-GCM for new encrypted data, generate a fresh
 * nonce for every encryption with a given key, and retain the nonce and any associated
 * authenticated data required for decryption. Encoded values such as Base64 or hexadecimal
 * are transport representations, not encryption.</p>
 *
 * <p>Cryptographic operations depend on the algorithms and providers installed in the running
 * JRE. Callers are responsible for protecting keys and passwords and for selecting algorithms,
 * key sizes, and protocol parameters appropriate to their security requirements.</p>
 */
package com.ajaxjs.util.cryptography;
