package com.ajaxjs.util.cryptography;

/**
 * Common cryptographic algorithm names and encoding/digest enumerations.
 */
public interface Constant {
    /**
     * Advanced Encryption Standard algorithm name.
     */
    String AES = "AES";

    /**
     * AES transformation used by WeChat mini programs: CBC mode with PKCS5 padding.
     */
    String AES_WX_MINI_APP = "AES/CBC/PKCS5Padding";

    /**
     * AES transformation used by WeChat mini programs: GCM mode with no padding.
     */
    String AES_WX_MINI_APP2 = "AES/GCM/NoPadding";

    /**
     * Data Encryption Standard algorithm name.
     */
    String DES = "DES";

    /**
     * Triple DES (also known as DESede) algorithm name.
     */
    @SuppressWarnings("SpellCheckingInspection")
    String TRIPLE_DES = "DESede";

    /**
     * Password-Based Key Derivation Function 2 with HMAC-SHA-256.
     */
    @SuppressWarnings("SpellCheckingInspection")
    String PBE = "PBKDF2WithHmacSHA256";

    /**
     * Legacy PBE algorithm retained only for decrypting existing data.
     */
    @Deprecated
    @SuppressWarnings("SpellCheckingInspection")
    String PBE_LEGACY = "PBEWITHMD5andDES";

    /**
     * RSA algorithm name.
     */
    String RSA = "RSA";// "RSA/ECB/PKCS1Padding"

    /**
     * RSA signature algorithm using MD5 with RSA.
     */
    String MD5_RSA = "MD5withRSA";

    /**
     * RSA signature algorithm using SHA-256 with RSA.
     */
    String SHA256_RSA = "SHA256withRSA";

    /**
     * Secure random number generator algorithm name.
     */
    String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

    /**
     * Optimal Asymmetric Encryption Padding
     */
    String RSAES_OAEP = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * Error message prefix used when a requested algorithm is not available.
     */
    String NO_SUCH_ALGORITHM = "No Such Algorithm in this Java. ";

    /**
     * Common binary-to-text encodings.
     */
    enum Encode {
        /**
         * Base16 (hexadecimal) encoding.
         */
        BASE16,

        /**
         * Base32 encoding.
         */
        BASE32,

        /**
         * Base58 encoding.
         */
        BASE58,

        /**
         * Base64 encoding.
         */
        BASE64,

        /**
         * Base91 encoding.
         */
        BASE91,
    }

    /**
     * Message digest algorithms.
     */
    enum Digest {
        /**
         * Standard MD5 digest.
         */
        Md5,

        /**
         * MD5 digest with an additional salt.
         */
        Md5WithSalt,
    }
}
