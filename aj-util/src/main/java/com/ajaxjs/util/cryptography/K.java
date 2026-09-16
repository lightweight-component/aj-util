package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.DoCipher;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;

/**
 * Provides RSA key generation, public-key encryption and private-key
 * decryption utilities.
 *
 * <p>The primary encryption implementation in this class uses RSA-OAEP with
 * SHA-256:</p>
 *
 * <pre>
 * RSA/ECB/OAEPWithSHA-256AndMGF1Padding
 * </pre>
 *
 * <p>The OAEP parameters are explicitly configured to use SHA-256 both for
 * the OAEP message digest and for MGF1. Explicit parameters are used to avoid
 * provider-specific differences in the default MGF1 digest.</p>
 *
 * <p>The normal RSA encryption workflow supported by this class is:</p>
 *
 * <pre>
 * plaintext
 *     |
 *     v
 * RSA public key
 *     |
 *     v
 * ciphertext
 *     |
 *     v
 * RSA private key
 *     |
 *     v
 * plaintext
 * </pre>
 *
 * <p>RSA is intended for relatively short input values. It should not be used
 * to directly encrypt large messages, files, or arbitrary-length payloads.
 * For large data, use hybrid encryption: encrypt the payload using a symmetric
 * cipher such as AES-GCM and use RSA only to encrypt the symmetric key.</p>
 *
 * <p>For RSA-OAEP with SHA-256, the maximum plaintext size is:</p>
 *
 * <pre>
 * modulusBytes - 2 * hashLength - 2
 * </pre>
 *
 * <p>For example, a 2048-bit RSA key with SHA-256 OAEP can encrypt at most
 * approximately 190 bytes in one operation.</p>
 *
 * <p>This class also contains compatibility methods for protocols that
 * specifically require OAEP with SHA-1, such as certain WeChat Pay sensitive
 * information encryption operations. Those methods use
 * {@link #RSAES_OAEP} independently from the primary SHA-256 OAEP
 * transformation.</p>
 *
 * <p>Instances defensively copy the supplied input byte array and therefore
 * are not affected by later modifications to the original array.</p>
 */
public class Rsa {

    /**
     * Base RSA key algorithm name.
     *
     * <p>This value is used with APIs such as
     * {@link KeyPairGenerator#getInstance(String)} and
     * {@link java.security.KeyFactory#getInstance(String)}.</p>
     *
     * <p>It is a key algorithm name, not a complete cipher transformation.</p>
     */
    static final String RSA = "RSA";

    /**
     * RSA digital-signature algorithm using SHA-256.
     *
     * <p>This value is intended for the JCA {@link Signature} API and is
     * independent from the RSA encryption transformation used by this
     * class.</p>
     */
    static final String SHA256_RSA = "SHA256withRSA";

    /**
     * Primary RSA cipher transformation.
     *
     * <p>This transformation uses RSA OAEP padding with SHA-256.</p>
     */
    private static final String RSA_CIPHER =
            "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    /**
     * Explicit OAEP parameters used by the primary RSA encryption and
     * decryption methods.
     *
     * <p>SHA-256 is used both as the OAEP digest and as the MGF1 digest.
     * {@link PSource.PSpecified#DEFAULT} represents the default empty OAEP
     * label.</p>
     */
    private static final OAEPParameterSpec OAEP_SPEC =
            new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT
            );

    /**
     * Input data to encrypt or decrypt.
     *
     * <p>The constructor stores a defensive copy of the supplied bytes.</p>
     */
    private final byte[] data;

    /**
     * Creates an RSA operation from UTF-8 text.
     *
     * <p>The supplied text is converted to bytes using UTF-8 before being
     * stored.</p>
     *
     * @param dataStr the text to encrypt or decrypt
     * @throws NullPointerException if {@code dataStr} is {@code null}
     */
    public Rsa(String dataStr) {
        this(dataStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates an RSA operation from binary data.
     *
     * <p>The supplied byte array is defensively copied so later changes to the
     * caller's array do not modify the input used by this instance.</p>
     *
     * @param data the binary input data
     * @throws IllegalArgumentException if {@code data} is {@code null}
     */
    public Rsa(byte[] data) {
        if (data == null)
            throw new IllegalArgumentException(
                    "RSA data must not be empty."
            );

        this.data = data.clone();
    }

    /**
     * Encrypts the configured data using an RSA public key supplied as
     * Base64 or PEM text.
     *
     * <p>The public key is restored using
     * {@link RestoreKey#restorePublicKey(String)} and encryption is then
     * performed using RSA-OAEP with SHA-256.</p>
     *
     * @param publicKeyStr the Base64- or PEM-encoded RSA public key
     * @return the encrypted binary data
     * @throws IllegalArgumentException if the public key encoding is invalid,
     *                                  the input is too large for the RSA key,
     *                                  or encryption parameters are invalid
     */
    public byte[] encryptWithPublicKey(String publicKeyStr) {
        PublicKey publicKey =
                RestoreKey.restorePublicKey(publicKeyStr);

        return encryptWithPublicKey(publicKey);
    }

    /**
     * Encrypts the configured data using an RSA public key and returns the
     * ciphertext as Base64.
     *
     * <p>Base64 is only a textual representation of the binary RSA ciphertext;
     * it does not provide additional encryption.</p>
     *
     * @param publicKeyStr the Base64- or PEM-encoded RSA public key
     * @return the Base64-encoded RSA ciphertext
     * @throws IllegalArgumentException if the public key or plaintext is
     *                                  invalid
     */
    public String encryptWithPublicKeyToBase64(
            String publicKeyStr) {

        return new Base64Utils(
                encryptWithPublicKey(publicKeyStr)
        ).encodeAsString();
    }

    /**
     * Encrypts the configured data using the supplied RSA public key.
     *
     * <p>Encryption uses
     * {@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding} together with explicit
     * SHA-256/MGF1-SHA256 OAEP parameters.</p>
     *
     * <p>The supplied plaintext must fit within the RSA-OAEP input-size limit
     * for the key size being used.</p>
     *
     * @param publicKey the RSA public key
     * @return the encrypted binary data
     * @throws IllegalArgumentException if the key, plaintext length, or OAEP
     *                                  parameters are invalid
     */
    public byte[] encryptWithPublicKey(PublicKey publicKey) {
        return new DoCipher(
                RSA_CIPHER,
                Cipher.ENCRYPT_MODE,
                publicKey
        ).doCipher(
                data,
                OAEP_SPEC,
                null
        );
    }

    /**
     * Encrypts the configured data using an RSA public key and returns the
     * ciphertext as Base64.
     *
     * @param publicKey the RSA public key
     * @return the Base64-encoded RSA ciphertext
     * @throws IllegalArgumentException if the key or plaintext is invalid
     */
    public String encryptWithPublicKeyToBase64(
            PublicKey publicKey) {

        return new Base64Utils(
                encryptWithPublicKey(publicKey)
        ).encodeAsString();
    }

    /**
     * Decrypts the configured RSA ciphertext using a private key supplied as
     * Base64 or PEM text.
     *
     * <p>The key is restored using
     * {@link RestoreKey#restorePrivateKey(String)}. The ciphertext must have
     * been produced using matching RSA-OAEP SHA-256 parameters.</p>
     *
     * @param privateKeyStr the Base64- or PEM-encoded RSA private key
     * @return the decrypted binary plaintext
     * @throws IllegalArgumentException if the private key, ciphertext, or OAEP
     *                                  parameters are invalid
     */
    public byte[] decryptWithPrivateKey(String privateKeyStr) {
        return decryptWithPrivateKey(
                RestoreKey.restorePrivateKey(privateKeyStr)
        );
    }

    /**
     * Decrypts the configured RSA ciphertext using the supplied private key.
     *
     * <p>The ciphertext must have been encrypted with the corresponding public
     * key using the same OAEP parameters.</p>
     *
     * @param privateKey the RSA private key
     * @return the decrypted binary plaintext
     * @throws IllegalArgumentException if the private key, ciphertext, or OAEP
     *                                  parameters are invalid
     */
    public byte[] decryptWithPrivateKey(PrivateKey privateKey) {
        return new DoCipher(
                RSA_CIPHER,
                Cipher.DECRYPT_MODE,
                privateKey
        ).doCipher(
                data,
                OAEP_SPEC,
                null
        );
    }

    /**
     * Decrypts the configured RSA ciphertext and interprets the resulting
     * plaintext as UTF-8 text.
     *
     * <p>This convenience method should only be used when the original
     * plaintext is known to be UTF-8 text. Use
     * {@link #decryptWithPrivateKey(String)} when the decrypted content is
     * arbitrary binary data.</p>
     *
     * @param privateKeyStr the Base64- or PEM-encoded RSA private key
     * @return the decrypted UTF-8 text
     * @throws IllegalArgumentException if the private key or ciphertext is
     *                                  invalid
     */
    public String decryptToString(String privateKeyStr) {
        return decryptToString(
                RestoreKey.restorePrivateKey(privateKeyStr)
        );
    }

    /**
     * Decrypts the configured RSA ciphertext and interprets the result as
     * UTF-8 text.
     *
     * <p>This method is appropriate only when the encrypted plaintext was
     * originally UTF-8 text. Binary plaintext should be retrieved using
     * {@link #decryptWithPrivateKey(PrivateKey)} instead.</p>
     *
     * @param privateKey the RSA private key
     * @return the decrypted UTF-8 text
     * @throws IllegalArgumentException if the private key or ciphertext is
     *                                  invalid
     */
    public String decryptToString(PrivateKey privateKey) {
        return new String(
                decryptWithPrivateKey(privateKey),
                StandardCharsets.UTF_8
        );
    }

    /**
     * Generates an RSA public/private key pair.
     *
     * <p>Supported key sizes are {@code 2048}, {@code 3072}, and
     * {@code 4096} bits. The resulting {@link KeyPair} contains both the
     * public key and its corresponding private key.</p>
     *
     * <p>The JCA provider supplies the secure random source used by
     * {@link KeyPairGenerator} during key generation.</p>
     *
     * @param keySize the RSA modulus size in bits; must be {@code 2048},
     *                {@code 3072}, or {@code 4096}
     * @return the generated RSA key pair
     * @throws IllegalArgumentException if {@code keySize} is unsupported
     * @throws IllegalStateException    if the RSA key-pair generator is not
     *                                  available in the current runtime
     */
    public static KeyPair generateKeyPair(int keySize) {
        if (keySize != 2048
                && keySize != 3072
                && keySize != 4096)
            throw new IllegalArgumentException(
                    "RSA key size must be 2048, 3072, or 4096 bits: "
                            + keySize
            );

        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance(RSA);

            generator.initialize(keySize);

            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    Constant.NO_SUCH_ALGORITHM + RSA,
                    e
            );
        }
    }

    /* ----------------- Sensitive information encryption ------------------- */

    /**
     * RSA OAEP transformation required by certain external protocols.
     *
     * <p>This transformation uses SHA-1 for OAEP and MGF1 according to the
     * provider/protocol requirements. It is retained separately from
     * {@link #RSA_CIPHER}, which uses SHA-256 and is preferred for the primary
     * RSA encryption API in this class.</p>
     *
     * <p>Do not replace this transformation with the SHA-256 variant when an
     * external protocol explicitly requires OAEP with SHA-1, because the two
     * ciphertext formats are not interoperable.</p>
     */
    static final String RSAES_OAEP =
            "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * Encrypts sensitive UTF-8 text using the public key contained in an
     * X.509 certificate.
     *
     * <p>This method is intended for external protocols that explicitly
     * require RSAES-OAEP with SHA-1, such as compatible sensitive-information
     * encryption flows. The certificate public key is used for encryption and
     * the resulting ciphertext is returned as Base64 text.</p>
     *
     * <p>This method uses a different OAEP transformation from
     * {@link #encryptWithPublicKey(PublicKey)}. The two methods must not be
     * mixed when communicating with a protocol that specifies one particular
     * OAEP configuration.</p>
     *
     * @param message     the UTF-8 plaintext to encrypt
     * @param certificate the X.509 certificate containing the RSA public key
     * @return the Base64-encoded RSA-OAEP ciphertext
     * @throws IllegalArgumentException if the certificate key, message,
     *                                  plaintext length, or encryption
     *                                  parameters are invalid
     */
    public static String encryptOAEP(
            String message,
            X509Certificate certificate) {

        DoCipher cryptography =
                new DoCipher(
                        RSAES_OAEP,
                        Cipher.ENCRYPT_MODE,
                        certificate.getPublicKey()
                );

        return cryptography.doCipher(
                message,
                DoCipher.OutputType.BASE64,
                null,
                null
        );
    }

    /**
     * Decrypts Base64-encoded RSA-OAEP ciphertext using the supplied private
     * key.
     *
     * <p>This method corresponds to {@link #encryptOAEP(String,
     * X509Certificate)} and uses the
     * {@code RSA/ECB/OAEPWithSHA-1AndMGF1Padding} transformation.</p>
     *
     * <p>The decrypted bytes are interpreted as UTF-8 text.</p>
     *
     * @param cipherText the Base64-encoded RSA-OAEP ciphertext
     * @param privateKey the RSA private key corresponding to the public key
     *                   used for encryption
     * @return the decrypted UTF-8 plaintext
     * @throws IllegalArgumentException if the ciphertext, private key, or OAEP
     *                                  parameters are invalid
     */
    public static String decryptOAEP(
            String cipherText,
            PrivateKey privateKey) {

        DoCipher cryptography =
                new DoCipher(
                        RSAES_OAEP,
                        Cipher.DECRYPT_MODE,
                        privateKey
                );

        return cryptography.doCipherFromBase64(
                cipherText,
                DoCipher.OutputType.UTF8_STR,
                null,
                null
        );
    }
}