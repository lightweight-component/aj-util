package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.cryptography.CipherResult;
import com.ajaxjs.util.cryptography.DoCipher;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.util.Collections;
import java.util.Map;

/**
 * A key-bound RSA cipher using SHA-256 OAEP by default.
 *
 * <p>Create an instance with either a recipient public key for encryption or
 * a recipient private key for decryption, then invoke the operation matching
 * that key. </p>
 *
 * <p>The default transformation is
 * {@code RSA/ECB/OAEPWithSHA-256AndMGF1Padding}. The OAEP message digest and
 * MGF1 digest are both explicitly SHA-256, with the default empty label. The
 * explicit parameters avoid provider-dependent MGF1 defaults and must match
 * on both encryption and decryption.</p>
 *
 * <p>The custom-transformation constructors support only the transformations
 * registered in this class's parameter map: PKCS#1 v1.5 padding, generic
 * OAEP with explicit SHA-1/MGF1-SHA-1 parameters, and OAEP with SHA-1,
 * SHA-256, or SHA-384 used for both the OAEP and MGF1 digests. PKCS#1 v1.5 is
 * retained only for interoperability with an existing protocol; use OAEP for
 * new integrations.</p>
 *
 * <p>RSA is suitable only for short values, such as an AES key. Do not encrypt
 * files or arbitrary-length messages directly. Use hybrid encryption for
 * larger data: encrypt the payload with a symmetric cipher such as AES-GCM,
 * then encrypt only that symmetric key with RSA. With a 2048-bit key and
 * SHA-256 OAEP, one operation can encrypt approximately 190 bytes.</p>
 *
 * <p>The returned ciphertext is arbitrary binary data. Use
 * {@link CipherResult#toBase64()} when placing it in JSON, HTTP, a database
 * text column, or another text protocol, and decrypt it with
 * {@link #decrypt(String)}. {@link #decryptRawUtf8Ciphertext(String)} accepts
 * UTF-8 text as raw cipher input and is normally inappropriate for
 * ciphertext.</p>
 *
 * <p>Instances keep an immutable key reference and create a new JCA
 * {@link Cipher} for each operation through {@code DoCipher}; no mutable
 * cipher state is shared between operations.</p>
 *
 * @see RestoreKey
 * @see DoCipher
 * @see CipherResult
 */
public class Rsa extends DoCipher {
    /**
     * Creates a default SHA-256 OAEP cipher from a Base64 or PEM RSA key.
     *
     * <p>When {@code isPublicKey} is {@code true}, the key is restored as an
     * X.509 SubjectPublicKeyInfo public key and the instance can encrypt.
     * Otherwise it is restored as a PKCS#8 private key and the instance can
     * decrypt. PKCS#1 {@code RSA PUBLIC KEY} and {@code RSA PRIVATE KEY} PEM
     * documents are not supported; see {@link RestoreKey}.</p>
     *
     * @param keyStr      Base64 or PEM key text
     * @param isPublicKey {@code true} for a public encryption key;
     *                    {@code false} for a private decryption key
     * @throws NullPointerException     if {@code keyStr} is {@code null}
     * @throws IllegalArgumentException if the key encoding or declared key
     *                                  type is invalid or unsupported
     */
    public Rsa(String keyStr, boolean isPublicKey) {
        this(isPublicKey ? RestoreKey.restorePublicKey(keyStr) : RestoreKey.restorePrivateKey(keyStr));
    }

    /**
     * Creates an RSA cipher from Base64 or PEM key text with a custom JCA
     * transformation.
     *
     * <p>The transformation must be one of the values registered in
     * {@link #OAEP_SPECS}. The matching OAEP parameters are supplied for each
     * operation; {@link #RSA_PKCS1} deliberately receives no OAEP parameters.
     * Use {@link DoCipher} directly for an unregistered transformation or a
     * non-default OAEP label/digest combination.</p>
     *
     * @param algorithmName JCA cipher transformation
     * @param keyStr        Base64 or PEM RSA key text
     * @param isPublicKey   {@code true} to restore a public key; {@code false}
     *                      to restore a private key
     * @throws NullPointerException     if {@code keyStr} is {@code null}
     * @throws IllegalArgumentException if the key encoding is invalid or the
     *                                  transformation/key/parameters are incompatible
     */
    public Rsa(String algorithmName, String keyStr, boolean isPublicKey) {
        this(algorithmName, isPublicKey ? RestoreKey.restorePublicKey(keyStr) : RestoreKey.restorePrivateKey(keyStr));
    }

    /**
     * Creates a default SHA-256 OAEP encryption cipher from a certificate's
     * public key.
     *
     * <p>The certificate is used only as a public-key container; certificate
     * chain validation, validity-period checks, and trust decisions remain the
     * responsibility of the caller.</p>
     *
     * @param certificate certificate containing an RSA public key
     * @throws NullPointerException if {@code certificate} is {@code null}
     */
    public Rsa(X509Certificate certificate) {
        this(certificate.getPublicKey());
    }

    /**
     * Creates the default SHA-256 OAEP cipher with the supplied key.
     *
     * @param key RSA public key for encryption or private key for decryption
     */
    private Rsa(Key key) {
        super(RSA_CIPHER_SHA256, key);
    }

    /**
     * Creates an RSA cipher from a certificate public key and a custom JCA
     * transformation.
     *
     * <p>The certificate is not validated by this constructor. The supplied
     * transformation must be registered in {@link #OAEP_SPECS} and compatible
     * with the certificate key.</p>
     *
     * @param algorithmName JCA cipher transformation
     * @param certificate   certificate containing an RSA public key
     * @throws NullPointerException if {@code certificate} is {@code null}
     */
    public Rsa(String algorithmName, X509Certificate certificate) {
        this(algorithmName, certificate.getPublicKey());
    }

    /**
     * Creates an RSA cipher from a key and a custom JCA transformation.
     *
     * <p>No key-type check is performed here. {@link #encrypt(String)} and
     * {@link #encrypt(byte[])} require a {@link PublicKey}; decryption methods
     * require a {@link PrivateKey}. The transformation is resolved only when
     * an operation is performed.</p>
     *
     * <p>The transformation must be registered in {@link #OAEP_SPECS}. Its
     * map value determines the OAEP parameters; the {@link #RSA_PKCS1} entry
     * has no OAEP parameters. For a transformation or parameter combination
     * not in the map, use {@link DoCipher#doCipher(int, byte[],
     * java.security.spec.AlgorithmParameterSpec, byte[])} directly.</p>
     *
     * @param algorithmName JCA cipher transformation
     * @param key           key compatible with the selected transformation
     */
    public Rsa(String algorithmName, Key key) {
        super(algorithmName, key);
    }

    /**
     * Encrypts UTF-8 text with this instance's RSA public key.
     *
     * <p>The text is UTF-8 encoded and encrypted using this instance's
     * configured transformation and mapped parameters. Convert the result with
     * {@link CipherResult#toBase64()} before sending or storing it as text.</p>
     *
     * @param data plaintext text to encrypt
     * @return binary ciphertext wrapped in a {@link CipherResult}
     * @throws NullPointerException          if {@code data} is {@code null}
     * @throws IllegalArgumentException      if the configured key is not public,
     *                                       the plaintext is too large, or the
     *                                       cipher operation fails
     * @throws UnsupportedOperationException if the configured transformation
     *                                       is not in this class's parameter map
     */
    public CipherResult encrypt(String data) {
        if (!(getKey() instanceof PublicKey))
            throw new IllegalArgumentException("To encrypt, it should be public key.");

        return doCipher(Cipher.ENCRYPT_MODE, data, getSpec(), null);
    }

    /**
     * Encrypts raw binary data with this instance's RSA public key.
     *
     * <p>The bytes must fit within the OAEP plaintext capacity for the key.
     * For large data, encrypt the data with a symmetric cipher and encrypt
     * only the symmetric key with RSA.</p>
     *
     * @param data binary plaintext to encrypt
     * @return binary ciphertext wrapped in a {@link CipherResult}
     * @throws IllegalArgumentException      if the configured key is not public, the input is too large, or the cipher operation fails
     * @throws UnsupportedOperationException if the configured transformation
     *                                       is not in this class's parameter map
     */
    public CipherResult encrypt(byte[] data) {
        if (!(getKey() instanceof PublicKey))
            throw new IllegalArgumentException("To encrypt, it should be public key.");

        return doCipher(Cipher.ENCRYPT_MODE, data, getSpec(), null);
    }

    /**
     * Decrypts ciphertext text with this instance's RSA private key.
     *
     * <p>When {@code isBase64} is {@code true}, {@code cipherText} is Base64
     * decoded before decryption and is suitable for ordinary text transports.
     * When it is {@code false}, the string itself is UTF-8 encoded and treated
     * as raw ciphertext bytes. The latter is only valid when those bytes were
     * deliberately represented as UTF-8 text; it is not a safe general
     * representation for encrypted data.</p>
     *
     * @param cipherText ciphertext represented as Base64 or raw UTF-8 text
     * @param isBase64   whether {@code cipherText} is Base64 encoded
     * @return recovered plaintext bytes wrapped in a {@link CipherResult}
     * @throws NullPointerException          if {@code cipherText} is {@code null}
     * @throws IllegalArgumentException      if the configured key is not private, the Base64 data is invalid, or the
     *                                       key, OAEP parameters, or ciphertext does not match
     * @throws UnsupportedOperationException if the configured transformation
     *                                       is not in this class's parameter map
     */
    public CipherResult decryptToResult(String cipherText, boolean isBase64) {
        if (!(getKey() instanceof PrivateKey))
            throw new IllegalArgumentException("To decrypt, it should be private key.");

        OAEPParameterSpec spec = getSpec();

        return isBase64 ?
                doCipherFromBase64(Cipher.DECRYPT_MODE, cipherText, spec, null) :
                doCipher(Cipher.DECRYPT_MODE, cipherText, spec, null);
    }

    /**
     * Decrypts raw binary ciphertext with this instance's RSA private key.
     *
     * <p>The ciphertext must have been produced by the corresponding public
     * key using this instance's matching transformation and parameters.</p>
     *
     * @param cipherData raw RSA ciphertext
     * @return recovered plaintext bytes wrapped in a {@link CipherResult}
     * @throws IllegalArgumentException      if the configured key is not private or the key, parameters, or ciphertext is invalid
     * @throws UnsupportedOperationException if the configured transformation
     *                                       is not in this class's parameter map
     */
    public CipherResult decryptToResult(byte[] cipherData) {
        if (!(getKey() instanceof PrivateKey))
            throw new IllegalArgumentException("To decrypt, it should be private key.");

        return doCipher(Cipher.DECRYPT_MODE, cipherData, getSpec(), null);
    }

    /**
     * Decrypts Base64-encoded RSA ciphertext and returns UTF-8 plaintext.
     *
     * <p>This is the normal counterpart to
     * {@code encrypt(plaintext).toBase64()}. Use
     * {@link #decryptToResult(String, boolean)} when the recovered plaintext
     * is arbitrary binary data.</p>
     *
     * @param cipherText Base64-encoded ciphertext
     * @return recovered plaintext interpreted as UTF-8
     * @throws NullPointerException     if {@code cipherText} is {@code null}
     * @throws IllegalArgumentException if the key is not private, Base64 is
     *                                  invalid, or decryption fails
     */
    public String decrypt(String cipherText) {
        return decryptToResult(cipherText, true).toUtf8();
    }

    /**
     * Decrypts UTF-8 text that represents raw ciphertext bytes and returns
     * the recovered plaintext as UTF-8.
     *
     * <p>Encrypted bytes are generally not valid UTF-8. This method exists
     * only for an unusual legacy representation in which ciphertext bytes
     * were deliberately converted to UTF-8 text. For normal text transport,
     * use {@link #decrypt(String)}. Use {@link #decryptToResult(byte[])} for
     * binary plaintext.</p>
     *
     * @param cipherText raw ciphertext represented as UTF-8 text
     * @return recovered plaintext interpreted as UTF-8
     */
    public String decryptRawUtf8Ciphertext(String cipherText) {
        return decryptToResult(cipherText, false).toUtf8();
    }

    /**
     * RSAES-PKCS1-v1_5 transformation; it does not use OAEP parameters.
     */
    private static final String RSA_PKCS1 = "RSA/ECB/PKCS1Padding";

    /**
     * Generic OAEP transformation, explicitly configured as SHA-1/MGF1-SHA-1.
     */
    private static final String RSA_OAEP_SHA1 = "RSA/ECB/OAEPPadding";

    /**
     * RSA-OAEP compatibility transformation using SHA-1 for both the OAEP digest and MGF1 digest.
     *
     * <p>Use only when an external protocol explicitly requires SHA-1 OAEP.
     * New integrations should use the default SHA-256 transformation.</p>
     */
    public static final String RSA_CIPHER_SHA1 = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * Primary RSA cipher transformation.
     *
     * <p>This transformation uses RSA OAEP padding with SHA-256.</p>
     */
    private static final String RSA_CIPHER_SHA256 = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    /**
     * RSA-OAEP transformation using SHA-384 for both OAEP and MGF1 digests.
     */
    private static final String RSA_CIPHER_SHA384 = "RSA/ECB/OAEPWithSHA-384AndMGF1Padding";

    /**
     * Maps supported cipher transformations to their explicit OAEP parameters.
     *
     * <p>The {@link #RSA_PKCS1} entry deliberately has a {@code null} value:
     * PKCS#1 v1.5 is not OAEP and must initialize the cipher without an
     * {@link OAEPParameterSpec}. All OAEP entries use the empty default label.
     * A map lookup must therefore use {@link Map#containsKey(Object)} to
     * distinguish this supported null value from an unsupported transformation.</p>
     */
    private static final Map<String, OAEPParameterSpec> OAEP_SPECS = createOaepSpecs();

    private static Map<String, OAEPParameterSpec> createOaepSpecs() {
        Map<String, OAEPParameterSpec> specs = ObjectHelper.mapOf(
                RSA_PKCS1, null,
                RSA_OAEP_SHA1, oaepSpec("SHA-1", MGF1ParameterSpec.SHA1),
                RSA_CIPHER_SHA1, oaepSpec("SHA-1", MGF1ParameterSpec.SHA1)
        );

        specs.put(RSA_CIPHER_SHA256, oaepSpec("SHA-256", MGF1ParameterSpec.SHA256));
        specs.put(RSA_CIPHER_SHA384, oaepSpec("SHA-384", MGF1ParameterSpec.SHA384));

        return Collections.unmodifiableMap(specs);
    }

    /**
     * Creates an OAEP parameter set using the supplied digest for OAEP and
     * the supplied MGF1 digest, with an empty OAEP label.
     *
     * @param digest     OAEP message-digest algorithm name
     * @param mgf1Digest digest configuration for MGF1
     * @return an explicit OAEP parameter set
     */
    private static OAEPParameterSpec oaepSpec(String digest, MGF1ParameterSpec mgf1Digest) {
        return new OAEPParameterSpec(digest, "MGF1", mgf1Digest, PSource.PSpecified.DEFAULT);
    }

    /**
     * Returns the explicit OAEP parameters for the configured transformation.
     *
     * @return matching OAEP parameters, or {@code null} for supported
     * {@code RSA/ECB/PKCS1Padding}
     * @throws UnsupportedOperationException if the transformation is not in the supported parameter map
     */
    OAEPParameterSpec getSpec() {
        if (!OAEP_SPECS.containsKey(getAlgorithmName()))
            throw new UnsupportedOperationException("No OAEP Spec for " + getAlgorithmName());

        return OAEP_SPECS.get(getAlgorithmName());
    }
}
