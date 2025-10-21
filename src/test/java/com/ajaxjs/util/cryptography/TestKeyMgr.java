package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import com.ajaxjs.util.io.FileHelper;
import org.junit.jupiter.api.Test;

import java.security.Key;

/**
 * Private key utils
 */
public class TestKeyMgr {
    @Test
    void testRestore() {
        String fileContent = FileHelper.readFileContent("C:\\Users\\Z\\Downloads\\WXCertUtil\\cert\\1623777099_20251021_cert\\apiclient_key.pem");
        Key key = KeyMgr.restorePrivateKey(fileContent);

        System.out.println(key.getAlgorithm());
    }
}
