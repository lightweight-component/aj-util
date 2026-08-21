package com.ajaxjs.s3client.factory;

/**
 * Backblaze B2 S3-compatible client with explicit Host signing enabled.
 */
public class Backblaze extends CloudflareR2 {
    {
        setSetHost(true);
    }
}
