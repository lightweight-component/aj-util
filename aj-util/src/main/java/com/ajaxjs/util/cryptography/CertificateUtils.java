package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StringBytes;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provides X.509 certificate loading and AES-GCM certificate-payload decryption utilities.
 */
public class CertificateUtils {
    /**
     * Loads a PEM or DER-encoded X.509 certificate from a file path and checks its validity period.
     *
     * @param filePath File path
     * @return Certificate Object
     * @throws UncheckedIOException if the certificate file cannot be opened or read
     * @throws RuntimeException     if the certificate is invalid or outside its validity period
     */
    public static X509Certificate getCert(String filePath) {
        Objects.requireNonNull(filePath, "getCert.filePath");

        try {
            return getCert(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("File Not Found:" + filePath, e);
        }
    }

    /**
     * Loads a PEM or DER-encoded X.509 certificate from an input stream and checks its validity period.
     *
     * @param in Input stream, which contains a certificate. When it's done, it will be closed.
     * @return Certificate Object
     * @throws UncheckedIOException if an I/O error occurs while reading the certificate
     * @throws RuntimeException     if the certificate is invalid or outside its validity period
     */
    public static X509Certificate getCert(InputStream in) {
        try (InputStream input = in) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);
            cert.checkValidity();

            return cert;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate has been expired", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid", e); // 证书尚未生效
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate validity failed.", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Decrypts certificate entries from a platform response and indexes them by serial number.
     *
     * @param apiV3Key the API v3 key
     * @param pMap     the certificate-download response body
     * @return a map of certificate serial numbers to certificates
     * @throws IllegalArgumentException if a required response field or GCM parameter is invalid
     * @throws ClassCastException       if the response contains values of unexpected types
     */
    @SuppressWarnings("unchecked")
    public static Map<BigInteger, X509Certificate> deserializeToCerts(String apiV3Key, Map<String, Object> pMap) {
        if (apiV3Key == null)
            throw new IllegalArgumentException("ApiV3Key must not be null.");

        byte[] apiV3KeyByte = new StringBytes(apiV3Key).getUTF8_Bytes();

        if (apiV3KeyByte.length != 32)
            throw new IllegalArgumentException("ApiV3Key must contain exactly 32 bytes.");

        Object data = pMap.get("data");

        if (!(data instanceof List))
            throw new IllegalArgumentException("Certificate response field 'data' must be a list.");

        List<Map<String, Object>> list = (List<Map<String, Object>>) data;
        Map<BigInteger, X509Certificate> newCertList = new HashMap<>();

        if (!ObjectHelper.isEmpty(list)) {
            for (Map<String, Object> map : list) {
                Map<String, Object> certificate = (Map<String, Object>) map.get("encrypt_certificate");
                String _data = remove(certificate.get("ciphertext"));
                byte[] nonce = removeAsByte(certificate.get("nonce")),
                        associatedData = removeAsByte(certificate.get("associated_data"));

                String cert = Aes.aesDecrypt(_data, apiV3KeyByte, nonce, associatedData);

                X509Certificate x509Cert = getCert(new ByteArrayInputStream(new StringBytes(cert).getUTF8_Bytes()));
                newCertList.put(x509Cert.getSerialNumber(), x509Cert);
            }
        }

        return newCertList;
    }

    /**
     * Removes surrounding double-quote characters from the given value.
     *
     * @param v the object whose string representation will be unquoted
     * @return the unquoted string
     * @throws IllegalArgumentException if the value is null
     */
    static String remove(Object v) {
        if (v == null)
            throw new IllegalArgumentException("Certificate response field is required.");

        String value = v.toString();
        return value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"'
                ? value.substring(1, value.length() - 1) : value;
    }

    static byte[] removeAsByte(Object v) {
        return remove(v).getBytes(StandardCharsets.UTF_8);
    }
}
