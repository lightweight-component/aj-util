package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.io.DataWriter;
import com.ajaxjs.util.io.FileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public class RestoreKey {
    public static PublicKey restorePublicKey(String key) {
        Objects.requireNonNull(key, "restorePublicKey.key");
        byte[] bytes = decodePemOrBase64(key, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");

        try {
            KeyFactory factory = KeyFactory.getInstance(Constant.RSA);

            return factory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid RSA public key encoding. Expected X.509 SubjectPublicKeyInfo.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(Constant.NO_SUCH_ALGORITHM + Constant.RSA, e);
        }
    }

    public static PrivateKey restorePrivateKey(String key) {
        Objects.requireNonNull(key, "restorePrivateKey.key");
        byte[] bytes = decodePemOrBase64(key, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

        try {
            KeyFactory factory = KeyFactory.getInstance(Constant.RSA);

            return factory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid RSA private key encoding. Expected PKCS#8.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(Constant.NO_SUCH_ALGORITHM + Constant.RSA, e);
        }
    }

    private static byte[] decodePemOrBase64(String key, String begin, String end) {
        String value = key.trim();

        if (value.contains("-----BEGIN RSA PUBLIC KEY-----") || value.contains("-----BEGIN RSA PRIVATE KEY-----"))
            throw new IllegalArgumentException("PKCS#1 RSA keys are not supported.");

        value = value.replace(begin, "").replace(end, "").replaceAll("\\s", "");

        if (value.isEmpty())
            throw new IllegalArgumentException("RSA key content is empty.");

        try {
            return new Base64Utils(value).decode();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid Base64 RSA key.", e);
        }
    }

    /**
     * Loads a UTF-8 Base64 or PEM-encoded private key from an input stream.
     *
     * @param in the stream containing the private key
     * @return the restored private key
     * @throws IllegalArgumentException if the key encoding is invalid
     * @throws UncheckedIOException     if the stream cannot be read
     */
    public static PrivateKey loadPrivateKey(InputStream in) {
        return loadPrivateKey(in, CommonConstant.UTF8);
    }

    /**
     * Loads a Base64 or PEM-encoded private key from an input stream using the requested charset.
     *
     * @param in      the stream containing the private key
     * @param charset the charset name used to decode the stream
     * @return the restored private key
     * @throws IllegalArgumentException if the key encoding or charset is invalid
     * @throws UncheckedIOException     if the stream cannot be read
     */
    public static PrivateKey loadPrivateKey(InputStream in, String charset) {
        String privateKey;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(2048)) {
            new DataWriter(out).write(in);
            privateKey = out.toString(charset);
        } catch (IOException e) {
            throw new UncheckedIOException("Invalid private key.", e);
        }

        return restorePrivateKey(privateKey);
    }

    /**
     * Loads a private key from the given file path.
     *
     * @param filePath the path to the file containing the private key
     * @return the loaded private key
     * @throws IllegalArgumentException if the key encoding is invalid
     * @throws RuntimeException         if the file cannot be read
     */
    public static PrivateKey loadPrivateKey(String filePath) {
        String fileContent = new FileHelper(filePath).getFileContent();

        return restorePrivateKey(fileContent);
    }
}
