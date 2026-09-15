package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.Constant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.*;

@RequiredArgsConstructor
public class Signature2 {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The private key
     */
    private final PrivateKey privateKey;

    @Getter
    private byte[] result;

    public Signature2 sign(String data) {
        sign(data.getBytes());

        return this;
    }

    public Signature2 sign(byte[] data) {
        if (algorithmName == null || algorithmName.trim().isEmpty())
            throw new IllegalStateException("Signature algorithm is required.");

        if (data == null)
            throw new IllegalStateException("Data to sign is required.");

        if (privateKey == null)
            throw new IllegalStateException("Private key is required.");

        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initSign(privateKey);
            signature.update(data);

            result = signature.sign();
        } catch (SignatureException e) {
            throw new IllegalStateException("Signature failed.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Private Key", e);
        }
        return this;
    }

    public String signToString() {
        return new Base64Utils(result).encodeAsString();
    }
}
