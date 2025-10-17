package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.EncodeTools;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AesWeiXinUtilTest {
    /**
     * 解密小程序提供的加密数据，返回包含手机号码等信息的 JSON 对象
     *
     * @param iv         前端给的
     * @param cipherText 前端给的，密文
     * @param sessionKey 后端申请返回
     * @return 解密后的文本
     */
    public static String aesDecryptPhone(String iv, String cipherText, String sessionKey) {
        byte[] keyData = EncodeTools.base64Decode(sessionKey);

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(keyData, Constant.AES)); // little odd, it's AES, differs with AES_WX_MINI_APP.
        cryptography.setSpec(new IvParameterSpec(EncodeTools.base64Decode(iv)));
        cryptography.setDataStrBase64(cipherText);

        return cryptography.doCipherAsStr();
    }

    @Test
    void testAesDecryptPhone() {
        String sessionKey = "IOv62NY75gNbTYVEe1ogWQ==";
        String iv = "b/+OsOf6+y4Hl6RXJW+CjQ==";
        String ciphertext = "6fxmM2gjyAk5v9mzSnPXw3xv4WTYywHH/JK9A78Zb2K8i9kehzGLd3xalzx8qNgkZ/SG4/kfL8DgpvQBEoygi7K7YNguUW7HNYHkESUiGXId+DGpziBjmxmoPquFZ8N2XF71kn6MYfXVUiwxCHRu5YYlTbKr4IjA2xqKMgAhaK6YsyD1NE9iOH4eYnT9Ky7B54BW0yWVH3NFgkTmEBQTNg==";
        String decryptedText = aesDecryptPhone(iv, ciphertext, sessionKey);
        // Add assertions to validate the decrypted text
        System.out.println(decryptedText);
    }

}