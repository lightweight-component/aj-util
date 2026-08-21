package com.ajaxjs.s3client;

import java.util.Map;

/**
 * Minimal operations supported by an S3-compatible client.
 */
public interface S3Client {
    /**
     * Lists buckets or objects as the provider's raw XML text.
     *
     * @return XML List
     */
    String listBucket();

    /**
     * Lists buckets or objects and converts the XML response to a map.
     *
     * @return XML List Map
     */
    Map<String, String> listBucketXml();

    /**
     * Creates a bucket.
     *
     * @param bucketName globally unique bucket name
     * @return whether the provider accepted the operation
     */
    boolean createBucket(String bucketName);

    /**
     * Deletes a bucket.
     *
     * @param bucketName bucket name
     * @return whether the provider accepted the operation
     */
    boolean deleteBucket(String bucketName);

    /**
     * Uploads bytes to a named bucket.
     *
     * @param bucketName bucket name
     * @param objectName object key
     * @param fileBytes  object content
     * @return whether the provider accepted the operation
     */
    boolean putObject(String bucketName, String objectName, byte[] fileBytes);

    /**
     * Uploads bytes using the configured default bucket.
     *
     * @param objectName object key
     * @param fileBytes  object content
     * @return whether the provider accepted the operation
     */
    boolean putObject(String objectName, byte[] fileBytes);

    /**
     * Retrieves an object from a named bucket.
     *
     * @param bucketName bucket name
     * @param objectName object key
     * @return whether the provider accepted the operation
     */
    boolean getObject(String bucketName, String objectName);

    /**
     * Retrieves an object using the configured default bucket.
     *
     * @param objectName object key
     * @return whether the provider accepted the operation
     */
    boolean getObject(String objectName);

    /**
     * Deletes an object from a named bucket.
     *
     * @param bucketName bucket name
     * @param objectName object key
     * @return whether the provider accepted the operation
     */
    boolean deleteObject(String bucketName, String objectName);

    /**
     * Deletes an object using the configured default bucket.
     *
     * @param objectName object key
     * @return whether the provider accepted the operation
     */
    boolean deleteObject(String objectName);
}
