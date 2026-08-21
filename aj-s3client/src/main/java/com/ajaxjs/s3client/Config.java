package com.ajaxjs.s3client;

import lombok.Data;

/**
 * Mutable endpoint and credential configuration shared by provider clients.
 */
@Data
public class Config {
    /**
     * Provider endpoint, with or without a scheme depending on the client implementation.
     */
    private String endPoint;

    /**
     * Public access-key identifier.
     */
    private String accessKey;

    /**
     * Secret signing key; callers must not log or expose this value.
     */
    private String secretKey;

    /**
     * Default bucket used by convenience methods.
     */
    private String bucketName;

    /**
     * Provider-specific SigV2 authorization prefix, such as {@code OSS}.
     */
    private String remark;

    /**
     * SigV4 region; defaults to {@code auto} for compatible providers.
     */
    private String region = "auto";
}
