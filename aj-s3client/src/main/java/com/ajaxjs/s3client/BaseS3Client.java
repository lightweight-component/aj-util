package com.ajaxjs.s3client;

import com.ajaxjs.s3client.util.URLEncoding;
import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Put;
import com.ajaxjs.util.httpremote.Response;
import lombok.Data;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * Common configuration, convenience methods, and response checks for S3 clients.
 */
@Data
public abstract class BaseS3Client implements S3Client {
    /**
     * HTTP Date header name.
     */
    public static final String DATE = "Date";

    /**
     * HTTP Authorization header name.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * HTTPS URI scheme prefix.
     */
    public static final String HTTPS = "https://";

    /**
     * Standard canned ACL value for publicly readable objects.
     */
    public static final String ACL_PUBLIC_READ = "public-read";

    /**
     * Provider credentials and endpoint configuration.
     */
    private Config config;

    /**
     * Whether SigV4 requests explicitly include the endpoint Host header.
     */
    private boolean isSetHost;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putObject(String objectName, byte[] fileBytes) {
        validateConfiguration(true);

        return putObject(getConfig().getBucketName(), objectName, fileBytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getObject(String objectName) {
        validateConfiguration(true);

        return getObject(getConfig().getBucketName(), objectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteObject(String objectName) {
        validateConfiguration(true);

        return deleteObject(getConfig().getBucketName(), objectName);
    }

    /**
     * Tests whether a response has a successful HTTP status code.
     *
     * @param result response to inspect; may be {@code null}
     * @return {@code true} for any status from 200 through 299
     */
    public static boolean check(Response result) {
        Integer httpCode = result == null ? null : result.getHttpCode();

        return httpCode != null && httpCode >= 200 && httpCode < 300;
    }

    /**
     * Checks an upload response by status, ETag presence, and an optional hash.
     *
     * @param result response containing the HTTP connection and status
     * @param hash   expected unquoted ETag value, or {@code null} to skip value comparison
     * @return {@code true} when the response is HTTP 200 and its ETag is acceptable
     */
    protected static boolean eTagCheck(Response result, String hash) {
        return hash == null ? check(result) : singlePartMd5ETagCheck(result, hash);
    }

    /**
     * Verifies an ETag as MD5 only when the caller knows the upload was single-part and
     * was not transformed or encrypted by the service.
     *
     * @param result response containing the HTTP connection and status
     * @param md5Hex expected lowercase or uppercase MD5 hexadecimal value
     * @return {@code true} when a successful single-part response contains the expected ETag
     */
    protected static boolean singlePartMd5ETagCheck(Response result, String md5Hex) {
        if (!check(result) || result.getConnection() == null)
            return false;

        String etag = result.getConnection().getHeaderField("ETag");

        if (etag == null)
            return false;

        etag = etag.trim();

        if (etag.length() >= 2 && etag.startsWith("\"") && etag.endsWith("\""))
            etag = etag.substring(1, etag.length() - 1);

        if (etag.indexOf('-') >= 0)
            return false;

        return etag.equalsIgnoreCase(md5Hex);
    }

    /**
     * Sends a raw binary PUT request using the media type expected by S3 services.
     *
     * @param url     target URL
     * @param body    request body
     * @param headers callback that adds signed request headers
     * @return completed HTTP response
     * @throws RuntimeException when the underlying HTTP request cannot be completed
     */
    protected static Response putBinary(String url, byte[] body, Consumer<HttpURLConnection> headers) {
        return new Put(url, body, HttpConstant.FILE_TYPE, headers).getResp();
    }

    /**
     * Validates credentials, endpoint, region, and optionally the default bucket.
     *
     * @param requireBucket whether the configured default bucket is required
     * @throws IllegalStateException    if no configuration has been assigned
     * @throws IllegalArgumentException if a required configuration value is missing or invalid
     */
    protected void validateConfiguration(boolean requireBucket) {
        Config value = config;

        if (value == null)
            throw new IllegalStateException("S3 configuration is required.");

        requireText(value.getEndPoint(), "S3 endpoint");
        requireText(value.getAccessKey(), "S3 access key");
        requireText(value.getSecretKey(), "S3 secret key");
        requireText(value.getRegion(), "S3 region");
        endpointUri();

        if (requireBucket)
            validateBucketName(value.getBucketName());
    }

    /**
     * Returns the validated endpoint in normalized form.
     *
     * @return normalized HTTPS endpoint without a trailing slash
     * @throws IllegalStateException    if no configuration has been assigned
     * @throws IllegalArgumentException if the endpoint is invalid
     */
    protected String normalizedEndpoint() {
        URI uri = endpointUri();
        String value = uri.toASCIIString();

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * Builds a path-style URL for a bucket.
     *
     * @param bucketName bucket name
     * @return path-style bucket URL
     * @throws IllegalArgumentException if the configuration or bucket name is invalid
     */
    protected String bucketUrl(String bucketName) {
        validateConfiguration(false);
        validateBucketName(bucketName);

        return normalizedEndpoint() + "/" + bucketName;
    }

    /**
     * Builds an encoded path-style URL for an object.
     *
     * @param bucketName bucket name
     * @param objectName object key
     * @return encoded object URL
     * @throws IllegalArgumentException if the bucket name or object key is invalid
     */
    protected String objectUrl(String bucketName, String objectName) {
        validateBucketName(bucketName);
        validateObjectName(objectName);

        return bucketUrl(bucketName) + "/" + URLEncoding.encodePath(objectName);
    }

    /**
     * Validates a bucket name before it is inserted into a URL or signature.
     *
     * @param bucketName bucket name to validate
     * @throws IllegalArgumentException if the name is blank or contains path separators or control characters
     */
    protected static void validateBucketName(String bucketName) {
        requireText(bucketName, "S3 bucket name");

        if (bucketName.indexOf('/') >= 0 || bucketName.indexOf('\\') >= 0 || containsControl(bucketName))
            throw new IllegalArgumentException("The S3 bucket name is invalid.");
    }

    /**
     * Validates an object key before it is inserted into a URL or signature.
     *
     * @param objectName object key to validate
     * @throws IllegalArgumentException if the key is blank or contains control characters
     */
    protected static void validateObjectName(String objectName) {
        requireText(objectName, "S3 object name");

        if (containsControl(objectName))
            throw new IllegalArgumentException("The S3 object name contains control characters.");
    }

    /**
     * Parses and validates the configured HTTPS endpoint origin.
     *
     * @return validated endpoint URI
     * @throws IllegalStateException    if no configuration has been assigned
     * @throws IllegalArgumentException if the endpoint is not a valid HTTPS origin
     */
    URI endpointUri() {
        if (config == null)
            throw new IllegalStateException("S3 configuration is required.");

        String endpoint = config.getEndPoint().trim();

        if (!endpoint.contains("://"))
            endpoint = HTTPS + endpoint;

        try {
            URI uri = new URI(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null)
                throw new IllegalArgumentException("The S3 endpoint must be an HTTPS origin without credentials, query, or fragment.");

            String path = uri.getPath();
            if (path != null && !path.isEmpty() && !"/".equals(path))
                throw new IllegalArgumentException("The S3 endpoint must not contain a path.");

            return uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The S3 endpoint is invalid.", e);
        }
    }

    /**
     * Requires a nonblank text value.
     *
     * @param value text to validate
     * @param label non-sensitive field label used in errors
     * @throws IllegalArgumentException if the value is {@code null}, empty, or blank
     */
    protected static void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(label + " is required.");
    }

    /**
     * Tests whether text contains an ISO control character.
     *
     * @param value text to inspect
     * @return {@code true} when at least one control character is present
     */
    static boolean containsControl(String value) {
        for (int i = 0; i < value.length(); i++)
            if (Character.isISOControl(value.charAt(i)))
                return true;

        return false;
    }
}
