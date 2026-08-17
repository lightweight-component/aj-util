package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StringBytes;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.cert.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides X.509 certificate loading and AES-GCM certificate-payload decryption utilities.
 */
public class CertificateUtils {
    /**
     * Loads a PEM- or DER-encoded X.509 certificate from a file path and checks its validity period.
     *
     * @param filePath File path
     * @return Certificate Object
     * @throws UncheckedIOException if the certificate file cannot be opened or read
     * @throws RuntimeException if the certificate is invalid or outside its validity period
     */
    public static X509Certificate getCert(String filePath) {
        try {
            return getCert(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("File Not Found:" + filePath, e);
        }
    }

    /**
     * Loads a PEM- or DER-encoded X.509 certificate from an input stream and checks its validity period.
     *
     * @param in Input stream, which contains a certificate. When it's done, it will be closed.
     * @return Certificate Object
     * @throws UncheckedIOException if an I/O error occurs while reading the certificate
     * @throws RuntimeException if the certificate is invalid or outside its validity period
     */
    public static X509Certificate getCert(InputStream in) {
        try (InputStream input = in) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(in);
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
     * @throws ClassCastException if the response contains values of unexpected types
     */
    @SuppressWarnings("unchecked")
    public static Map<BigInteger, X509Certificate> deserializeToCerts(String apiV3Key, Map<String, Object> pMap) {
        byte[] apiV3KeyByte = new StringBytes(apiV3Key).getUTF8_Bytes();
        List<Map<String, Object>> list = (List<Map<String, Object>>) pMap.get("data");
        Map<BigInteger, X509Certificate> newCertList = new HashMap<>();

        if (!ObjectHelper.isEmpty(list)) {
            for (Map<String, Object> map : list) {
                Map<String, Object> certificate = (Map<String, Object>) map.get("encrypt_certificate");
                String cert = aesDecryptToString(apiV3KeyByte, // 解密
                        remove(certificate.get("associated_data")),
                        remove(certificate.get("nonce")),
                        remove(certificate.get("ciphertext")));

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

    /**
     * Decrypts an AEAD AES-256-GCM payload.
     *
     * @param aesKey         the 32-byte API v3 AES key
     * @param associatedData the additional authenticated data
     * @param nonce          the GCM nonce
     * @param cipherText     the Base64-encoded ciphertext
     * @return the decrypted UTF-8 text
     * @throws IllegalArgumentException if the key, nonce, associated data, ciphertext, or authentication tag is invalid
     */
//    public static String aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
//        if (aesKey.length != 32)
//            throw new IllegalArgumentException("无效的 ApiV3Key，长度必须为32个字节");
//
//        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
//
//        return doCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, aesKey, spec, cipherText, associatedData);
//    }

    /**
     * Decrypts the given ciphertext using AEAD_AES_256_GCM with string associated data and nonce.
     *
     * @param aesKey         the AES key, must be 32 bytes long
     * @param associatedData the associated data as a string
     * @param nonce          the nonce as a string
     * @param cipherText     the Base64-encoded ciphertext
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the key, nonce, associated data, ciphertext, or authentication tag is invalid
     */
    public static String aesDecryptToString(byte[] aesKey, String associatedData, String nonce, String cipherText) {
        if (associatedData == null)
            throw new IllegalArgumentException("GCM associated data 不能为空");

        if (nonce == null)
            throw new IllegalArgumentException("GCM nonce 不能为空");

        return aesDecryptToString(aesKey, new StringBytes(associatedData).getUTF8_Bytes(), new StringBytes(nonce).getUTF8_Bytes(), cipherText);
    }

    /**
     * Decrypts the given ciphertext using AEAD_AES_256_GCM.
     *
     * @param aesKey         the AES key, must be 32 bytes long
     * @param associatedData the associated data
     * @param nonce          the nonce
     * @param cipherText     the Base64-encoded ciphertext
     * @return the decrypted plaintext
     * @throws IllegalArgumentException if the key, nonce, associated data, ciphertext, or authentication tag is invalid
     */
    public static String aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
        if (aesKey == null || aesKey.length != 32)
            throw new IllegalArgumentException("The length of ApiV3Key should be 32 bytes");

        if (nonce == null || nonce.length != 12)
            throw new IllegalArgumentException("The length of GCM nonce should be 12 bytes");

        if (associatedData == null)
            throw new IllegalArgumentException("GCM associated data can't be null.");

        if (cipherText == null || cipherText.trim().isEmpty())
            throw new IllegalArgumentException("GCM ciphertext can't be null.");

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(aesKey, Constant.AES)); // little odd, it's AES.
        cryptography.setSpec(new GCMParameterSpec(128, nonce));
        cryptography.setDataStrBase64(cipherText);
        cryptography.setAssociatedData(associatedData);

        return cryptography.doCipherAsStr();
    }
}
