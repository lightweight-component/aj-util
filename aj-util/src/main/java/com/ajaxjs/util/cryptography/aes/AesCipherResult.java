package com.ajaxjs.util.cryptography.aes;

import com.ajaxjs.util.cryptography.CipherResult;
import lombok.Getter;
import lombok.Setter;

public class AesCipherResult extends CipherResult {
    public AesCipherResult(byte[] result) {
        super(result);
    }

    @Getter
    @Setter
    private CipherResult nonce;
}
