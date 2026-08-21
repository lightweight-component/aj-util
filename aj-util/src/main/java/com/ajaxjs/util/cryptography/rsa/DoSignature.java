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
 * Generates a digital signature from an algorithm, input data, and a private key.
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Data
public class DoSignature {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The data to be signed, in bytes.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] data;

    /**
     * The data to be signed, in string. It'll be converted to bytes in UTF-8 by default.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String strData;

    /**
     * Sets the data to be signed from a string and updates the byte representation.
     *
     * @param strData the data to be signed
     * @return this builder instance
     * @throws NullPointerException if the input string is null
     */
    public DoSignature setStrData(String strData) {
        this.strData = strData;
        this.data = strData.getBytes(StandardCharsets.UTF_8);

        return this;
    }

    /**
     * The private key
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PrivateKey privateKey;

    /**
     * The private key, in string.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String privateKeyStr;

    /**
     * Sets the private key from a Base64/PEM-encoded string.
     *
     * @param privateKeyStr the private key string
     * @return this builder instance
     * @throws IllegalArgumentException if the key encoding is invalid
     */
    public DoSignature setPrivateKeyStr(String privateKeyStr) {
        this.privateKeyStr = privateKeyStr;
        privateKey = (PrivateKey) KeyMgr.restoreKey(false, privateKeyStr);

        return this;
    }

    /**
     * Sign the data.
     *
     * @return The signature in bytes.
     * @throws IllegalStateException    if the algorithm, data, or private key is missing
     * @throws IllegalArgumentException if the private key is invalid
     * @throws RuntimeException         if the algorithm is unavailable or signing fails
     */
    public byte[] sign() {
        validateState();

        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initSign(privateKey);
            signature.update(data);

            return signature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException("Signature failed.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Private Key", e);
        }
    }

    /**
     * Validates that all required fields for signing are set.
     *
     * @throws IllegalStateException if any required field is missing
     */
    void validateState() {
        if (algorithmName == null || algorithmName.trim().isEmpty())
            throw new IllegalStateException("Signature algorithm is required.");

        if (data == null)
            throw new IllegalStateException("Data to sign is required.");

        if (privateKey == null)
            throw new IllegalStateException("Private key is required.");
    }

    /**
     * Sign the data then returns it as Base64 string.
     *
     * @return The signature in Base64 string.
     * @throws IllegalStateException    if the algorithm, data, or private key is missing
     * @throws IllegalArgumentException if the private key is invalid
     * @throws RuntimeException         if the algorithm is unavailable or signing fails
     */
    public String signToString() {
        return new Base64Utils(sign()).encodeAsString();
    }
}
