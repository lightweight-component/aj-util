package com.ajaxjs.s3client.signer_v4;

import com.ajaxjs.s3client.util.S3SigV4Utils;
import com.ajaxjs.util.HashHelper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable AWS Signature Version 4 signer.
 */
public final class SignBuilder {
    /**
     * Header that supplies the SigV4 timestamp.
     */
    private static final String X_AMZ_DATE = "X-Amz-Date";

    /**
     * Prefix applied to the secret key during signing-key derivation.
     */
    private static final String AUTH_TAG = "AWS4";

    /**
     * SigV4 authorization algorithm identifier.
     */
    private static final String ALGORITHM = AUTH_TAG + "-HMAC-SHA256";

    /**
     * Credentials used to calculate signatures.
     */
    private final AwsCredentials awsCredentials;

    /**
     * Region used in the credential scope.
     */
    private final String region;

    /**
     * Immutable signed-header values accumulated by the builder.
     */
    private final Map<String, String> headers;

    /**
     * Creates an immutable signer with no signed headers.
     *
     * @param awsCredentials credentials used to sign requests
     * @param region         signing region
     * @throws IllegalArgumentException if credentials are missing or the region is blank
     */
    public SignBuilder(AwsCredentials awsCredentials, String region) {
        this(awsCredentials, region, Collections.emptyMap());
    }

    /**
     * Creates an immutable signer from a complete header snapshot.
     *
     * @param awsCredentials credentials used to sign requests
     * @param region         signing region
     * @param headers        signed headers to copy
     * @throws IllegalArgumentException if credentials are missing or the region is blank
     */
    private SignBuilder(AwsCredentials awsCredentials, String region, Map<String, String> headers) {
        if (awsCredentials == null)
            throw new IllegalArgumentException("AWS credentials are required.");

        if (region == null || region.trim().isEmpty())
            throw new IllegalArgumentException("AWS region is required.");

        this.awsCredentials = awsCredentials;
        this.region = region;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
    }

    /**
     * Returns the signing credentials.
     *
     * @return immutable credential pair
     */
    public AwsCredentials getAwsCredentials() {
        return awsCredentials;
    }

    /**
     * Returns the signing region.
     *
     * @return signing region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the signed headers accumulated by this builder.
     *
     * @return immutable signed-header map
     */
    public Map<String, String> getMap() {
        return headers;
    }

    /**
     * Returns a new signer containing the supplied signed header.
     *
     * @param name  header name
     * @param value header value
     * @return new signer containing the header
     */
    public SignBuilder header(String name, String value) {
        Map<String, String> copy = new LinkedHashMap<>(headers);
        copy.put(name, value);

        return new SignBuilder(awsCredentials, region, copy);
    }

    /**
     * Builds a complete SigV4 Authorization header value.
     *
     * @param request       canonical request components
     * @param contentSha256 lowercase SHA-256 payload digest
     * @param service       AWS service identifier
     * @return SigV4 Authorization header value
     * @throws IllegalArgumentException if required request data or signed headers are missing or invalid
     */
    String build(CanonicalRequest request, String contentSha256, String service) {
        if (request == null || contentSha256 == null)
            throw new IllegalArgumentException("Canonical request and content SHA-256 are required.");

        CanonicalHeaders canonicalHeaders = CanonicalHeaders.build(headers);
        String date = canonicalHeaders.getFirstValue(X_AMZ_DATE).orElseThrow(() -> new IllegalArgumentException("Headers are missing '" + X_AMZ_DATE + "'."));
        CredentialScope scope = new CredentialScope(date, service, region);
        String scopeValue = scope.get();
        String canonicalRequestString = request.getMethod()
                + "\n" + request.getNormalizePath(service)
                + "\n" + request.getNormalizeQuery()
                + "\n" + canonicalHeaders.getCanonicalizedHeaders()
                + "\n" + canonicalHeaders.getNames()
                + "\n" + contentSha256;
        String stringToSign = ALGORITHM + "\n" + date + "\n" + scopeValue + "\n"
                + HashHelper.getSHA256(canonicalRequestString);
        byte[] kSecret = (AUTH_TAG + awsCredentials.getSecretKey()).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = S3SigV4Utils.hmacSha256(kSecret, scope.getDateWithoutTimestamp());
        byte[] kRegion = S3SigV4Utils.hmacSha256(kDate, scope.getRegion());
        byte[] kService = S3SigV4Utils.hmacSha256(kRegion, scope.getService());
        byte[] kSigning = S3SigV4Utils.hmacSha256(kService, CredentialScope.TERMINATION_STRING);
        String signature = S3SigV4Utils.toHex(S3SigV4Utils.hmacSha256(kSigning, stringToSign));

        return ALGORITHM + " Credential=" + awsCredentials.getAccessKey() + "/" + scopeValue
                + ", SignedHeaders=" + canonicalHeaders.getNames() + ", Signature=" + signature;
    }

    /**
     * Signs a request for the S3 service.
     *
     * @param request       canonical request components
     * @param contentSha256 lowercase SHA-256 payload digest
     * @return SigV4 Authorization header value
     * @throws IllegalArgumentException if required signing data is missing or invalid
     */
    public String getS3Signature(CanonicalRequest request, String contentSha256) {
        return build(request, contentSha256, "s3");
    }

    /**
     * Signs a request for the Glacier service.
     *
     * @param request       canonical request components
     * @param contentSha256 lowercase SHA-256 payload digest
     * @return SigV4 Authorization header value
     * @throws IllegalArgumentException if required signing data is missing or invalid
     */
    public String getGlacierSignature(CanonicalRequest request, String contentSha256) {
        return build(request, contentSha256, "glacier");
    }
}
