package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StringBytes;
import lombok.RequiredArgsConstructor;

import javax.crypto.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

@RequiredArgsConstructor
public class DoCipher {
    /**
     * The name of the algorithm
     */
    private final String algorithmName;

    /**
     * The cipher mode, normally {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}.
     */
    private final int mode;

    /**
     * The cryptographic key used for cipher operations.
     */

    private final Key key;

    /**
     * Performs the configured cipher operation and returns the result as bytes.
     *
     * @return the encrypted or decrypted bytes
     * @throws IllegalStateException    if required, cipher state is missing or invalid
     * @throws IllegalArgumentException if the key, parameters, input length, padding, or authentication tag is invalid
     * @throws RuntimeException         if the configured transformation is unavailable
     */
    public byte[] doCipher(byte[] data, AlgorithmParameterSpec spec, byte[] associatedData) {
        if (ObjectHelper.isEmptyText(algorithmName))
            throw new IllegalStateException("Cipher algorithm is required.");

        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE)
            throw new IllegalStateException("Cipher mode must be ENCRYPT_MODE or DECRYPT_MODE.");

        if (key == null)
            throw new IllegalStateException("Cipher key is required.");

        if (data == null)
            throw new IllegalStateException("Cipher data is required.");

        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            if (associatedData != null)
                cipher.updateAAD(associatedData);

            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (AEADBadTagException e) {
            throw new IllegalArgumentException("Authentication failed: the key, parameters, associated data, or ciphertext is invalid.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Invalid input length for transformation: " + algorithmName, e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("Cipher operation failed because the key, padding, or ciphertext is invalid.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
        }
    }

    public byte[] doCipher(String dataStr, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(dataStr, "doCipher.dataStr");

        return doCipher(new StringBytes(dataStr).getUTF8_Bytes(), spec, associatedData);
    }

    public byte[] doCipherFromBase64(String base64Str, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(base64Str, "doCipher.base64Str");

        return doCipher(new Base64Utils(base64Str).decode(), spec, associatedData);
    }

    public enum OutputType {
        UTF8_STR,

        BASE64,

        HEX
    }

    public String doCipher(byte[] data, OutputType outputType, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(outputType, "doCipher.outputType");

        if (outputType == OutputType.UTF8_STR && mode == Cipher.ENCRYPT_MODE)
            throw new IllegalArgumentException("UTF8_STR output is not suitable for encrypted binary data.");

        byte[] result = doCipher(data, spec, associatedData);

        switch (outputType) {
            case UTF8_STR:
                return new StringBytes(result).getUTF8_String();
            case BASE64:
                return new Base64Utils(result).encodeAsString();
            case HEX:
                return StringBytes.bytesToHex(result);
        }

        throw new UnsupportedOperationException("Unsupported output type: " + outputType);
    }

    public String doCipher(String dataStr, OutputType outputType, AlgorithmParameterSpec spec, byte[] associatedData) {
        return doCipher(new StringBytes(dataStr).getUTF8_Bytes(), outputType, spec, associatedData);
    }

    public String doCipherFromBase64(String base64Str, OutputType outputType, AlgorithmParameterSpec spec, byte[] associatedData) {
        return doCipher(new Base64Utils(base64Str).decode(), outputType, spec, associatedData);
    }
}
