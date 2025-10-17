package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.CollUtils;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.cryptography.WeiXinCrypto;
import com.ajaxjs.util.io.Resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/**
 * Private key utils
 */
public class PrivateKeyUtils {
    /**
     * 反序列化证书并解密
     *
     * @param apiV3Key APIv3 密钥
     * @param pMap     下载证书的请求返回体
     * @return 证书 list
     */
    @SuppressWarnings("unchecked")
    public static Map<BigInteger, X509Certificate> deserializeToCerts(String apiV3Key, Map<String, Object> pMap) {
        byte[] apiV3KeyByte = StrUtil.getUTF8_Bytes(apiV3Key);
        List<Map<String, Object>> list = (List<Map<String, Object>>) pMap.get("data");
        Map<BigInteger, X509Certificate> newCertList = new HashMap<>();

        if (!CollUtils.isEmpty(list)) {
            for (Map<String, Object> map : list) {
                Map<String, Object> certificate = (Map<String, Object>) map.get("encrypt_certificate");

                // 解密
                String cert = WeiXinCrypto.aesDecryptToString(apiV3KeyByte, StrUtil.getUTF8_Bytes(remove(certificate.get("associated_data"))), StrUtil.getUTF8_Bytes(remove(certificate.get("nonce"))),
                        remove(certificate.get("ciphertext")));

                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X509");
                    X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
                    x509Cert.checkValidity();
                    newCertList.put(x509Cert.getSerialNumber(), x509Cert);
                } catch (CertificateExpiredException | CertificateNotYetValidException ignored) {
                } catch (CertificateException e) {
                    throw new RuntimeException("当证书过期或尚未生效时", e);
                }
            }
        }

        return newCertList;
    }

    private static String remove(Object v) {
        return v.toString().replace("\"", "");
    }

    /**
     * 转换为 Java 里面的 PrivateKey 对象
     *
     * @param privateKey 私钥内容
     * @return 私钥对象
     */
    public static PrivateKey loadPrivateKey(String privateKey) {
        Objects.requireNonNull(privateKey, "没有私钥内容");
        privateKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", StrUtil.EMPTY_STRING)
                .replace("-----END PRIVATE KEY-----", StrUtil.EMPTY_STRING)
                .replaceAll("\\s+", StrUtil.EMPTY_STRING);

        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式", e);
        }
    }

    private static String privateKeyContent;

    /**
     * 从 classpath 上指定私钥文件的路径
     *
     * @param privateKeyPath 私钥文件的路径
     * @return 私钥文件 PrivateKey
     */
    public static PrivateKey loadPrivateKeyByPath(String privateKeyPath) {
        if (privateKeyContent == null)
            privateKeyContent = Resources.getResourceText(privateKeyPath); // cache it

        return loadPrivateKey(privateKeyContent);
    }

    /**
     * 从输入流中加载私钥
     * 该方法首先将输入流中的字节读取到 ByteArrayOutputStream 中，然后将其转换为字符串形式的私钥，
     * 最后调用另一方法 loadPrivateKey(String) 来解析并返回私钥对象
     *
     * @param inputStream 包含私钥信息的输入流
     * @return 解析后的 PrivateKey 对象
     * @throws IllegalArgumentException 如果输入流中的数据无法被正确读取或解析为私钥，则抛出此异常
     */
    public static PrivateKey loadPrivateKey(InputStream inputStream) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
        byte[] buffer = new byte[1024];
        String privateKey;

        try {
            for (int length; (length = inputStream.read(buffer)) != -1; )
                os.write(buffer, 0, length);

            privateKey = os.toString(EncodeTools.UTF8_SYMBOL);
        } catch (IOException e) {
            throw new IllegalArgumentException("无效的密钥", e);
        }

        return loadPrivateKey(privateKey);
    }
}
