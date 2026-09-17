package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StringBytes;
import lombok.Getter;
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
    @Getter
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
    public Result doCipher(byte[] data, AlgorithmParameterSpec spec, byte[] associatedData) {
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

            return new Result(cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalArgumentException(NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (AEADBadTagException e) {
            throw new IllegalArgumentException("Authentication failed: the key, parameters, associated data, or ciphertext is invalid.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Invalid input length for transformation: " + algorithmName, e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("Cipher operation failed because the key, padding, or ciphertext is invalid.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter: " + algorithmName, e);
        }
    }

    public Result doCipher(String dataStr, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(dataStr, "doCipher.dataStr");

        return doCipher(new StringBytes(dataStr).getUTF8_Bytes(), spec, associatedData);
    }

    public Result doCipherFromBase64(String base64Str, AlgorithmParameterSpec spec, byte[] associatedData) {
        Objects.requireNonNull(base64Str, "doCipher.base64Str");

        return doCipher(new Base64Utils(base64Str).decode(), spec, associatedData);
    }

    /**
     * Error message prefix used when a requested algorithm is not available.
     */
    public final static String NO_SUCH_ALGORITHM = "No Such Algorithm in this Java. ";
}
