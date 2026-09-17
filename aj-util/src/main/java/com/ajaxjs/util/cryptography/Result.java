package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.StringBytes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Result {
    @Getter
    private final byte[] result;

    public String toUtf8() { // UTF8_STR output is not suitable for encrypted binary data.
        return new StringBytes(result).getUTF8_String();
    }

    public String toBase64() {
        return new Base64Utils(result).encodeAsString();
    }

    public String toHex() {
        return StringBytes.bytesToHex(result);
    }
}
