package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Verifies a digital signature using an algorithm, input data, signature bytes, and a public key.
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Data
public class DoVerify {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The data to be verified, in bytes.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] data;

    /**
     * The data to be verified, in string. It'll be converted to bytes in UTF-8 by default.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String strData;

    /**
     * Sets the data to be verified from a string and updates the byte representation.
     *
     * @param strData the data to be verified
     * @return this builder instance
     * @throws NullPointerException if the input string is null
     */
    public DoVerify setStrData(String strData) {
        this.strData = strData;
        this.data = strData.getBytes(StandardCharsets.UTF_8);

        return this;
    }

    /**
     * The signature bytes to be verified.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] signatureData;

    /**
     * @param signatureBase64 The signature string should be a Base64 string.
     * @return This
     * @throws IllegalArgumentException if the signature is not valid Base64
     */
    public DoVerify setSignatureBase64(String signatureBase64) {
        signatureData = new Base64Utils(signatureBase64).decode();

        return this;
    }

    /**
     * The public key
     */
    private PublicKey publicKey;

    /**
     * The public key, in string.
     */
    private String publicKeyStr;

    /**
     * Sets the public key from a Base64/PEM-encoded string.
     *
     * @param publicKeyStr the public key string
     * @return this builder instance
     * @throws IllegalArgumentException if the key encoding is invalid
     */
    public DoVerify setPublicKeyStr(String publicKeyStr) {
        this.publicKeyStr = publicKeyStr;
        publicKey = (PublicKey) KeyMgr.restoreKey(true, publicKeyStr);

        return this;
    }

    /**
     * Verifies the configured digital signature.
     *
     * @return {@code true} if the signature is valid; {@code false} otherwise
     * @throws IllegalStateException    if the algorithm, data, signature, or public key is missing
     * @throws RuntimeException         if the algorithm is unavailable or verification fails
     * @throws IllegalArgumentException if the public key is invalid
     */
    public boolean verify() {
        validateState();

        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initVerify(publicKey);
            signature.update(data);

            return signature.verify(signatureData);
        } catch (SignatureException e) {
            throw new RuntimeException("Signature failed.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Public Key", e);
        }
    }

    /**
     * Validates that all required fields for verification are set.
     *
     * @throws IllegalStateException if any required field is missing
     */
    void validateState() {
        if (algorithmName == null || algorithmName.trim().isEmpty())
            throw new IllegalStateException("Signature algorithm is required.");

        if (data == null)
            throw new IllegalStateException("Data to verify is required.");

        if (signatureData == null)
            throw new IllegalStateException("Signature data is required.");

        if (publicKey == null)
            throw new IllegalStateException("Public key is required.");
    }
}
