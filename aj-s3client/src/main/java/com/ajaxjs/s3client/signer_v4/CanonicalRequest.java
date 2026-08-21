package com.ajaxjs.s3client.signer_v4;

import com.ajaxjs.s3client.util.URLEncoding;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Immutable method, path, and raw-query representation used by Signature Version 4.
 */
public final class CanonicalRequest {
    /**
     * HTTP method included in the canonical request.
     */
    private final String method;

    /**
     * Raw URI path before SigV4 normalization.
     */
    private final String path;

    /**
     * Raw query string without the leading question mark.
     */
    private final String query;

    /**
     * Creates a canonical request from a URI.
     *
     * @param method HTTP method
     * @param uri    request URI
     * @throws IllegalArgumentException if the method is blank or the URI is {@code null}
     */
    public CanonicalRequest(String method, URI uri) {
        if (method == null || method.trim().isEmpty() || uri == null)
            throw new IllegalArgumentException("HTTP method and URI are required.");

        this.method = method;
        this.path = uri.getRawPath();
        this.query = uri.getRawQuery();
    }

    /**
     * Creates a canonical request from a raw path and optional query.
     *
     * @param method       HTTP method
     * @param pathAndQuery raw path followed by an optional query string
     * @throws IllegalArgumentException if the method is blank or the path is {@code null}
     */
    public CanonicalRequest(String method, String pathAndQuery) {
        if (method == null || method.trim().isEmpty() || pathAndQuery == null)
            throw new IllegalArgumentException("HTTP method and path are required.");

        this.method = method;
        int queryStart = pathAndQuery.indexOf('?');
        this.path = queryStart >= 0 ? pathAndQuery.substring(0, queryStart) : pathAndQuery;
        this.query = queryStart >= 0 ? pathAndQuery.substring(queryStart + 1) : null;
    }

    /**
     * Returns the HTTP method.
     *
     * @return HTTP method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the raw request path.
     *
     * @return raw path, possibly {@code null}
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the raw query string.
     *
     * @return raw query without {@code ?}, possibly {@code null}
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the S3 canonical path without normalizing significant slash segments.
     *
     * @return canonical S3 request path
     * @throws IllegalArgumentException if the path contains a malformed percent escape
     */
    public String getNormalizePath() {
        return getNormalizePath("s3");
    }

    /**
     * Canonicalizes the path according to service-specific normalization rules.
     *
     * @param service AWS service identifier
     * @return canonical request path
     * @throws IllegalArgumentException if the path cannot be decoded or normalized
     */
    String getNormalizePath(String service) {
        if (path == null || path.isEmpty())
            return "/";

        String encoded = URLEncoding.encodePath(decodePercent(path));

        if ("s3".equals(service))
            return encoded;

        try {
            return new URI("http://example.test" + encoded).normalize().getRawPath();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The request path cannot be canonicalized.", e);
        }
    }

    /**
     * Canonicalizes a raw query. Each component is percent-decoded once (never form-decoded),
     * AWS-encoded once, then sorted by encoded name and encoded value. Duplicate names and
     * empty/missing values are retained.
     *
     * @return canonical query string sorted by encoded name and value
     * @throws IllegalArgumentException if a query component contains a malformed percent escape
     */
    public String getNormalizeQuery() {
        if (query == null || query.isEmpty())
            return "";

        List<Parameter> parameters = new ArrayList<>();
        String[] parts = query.split("&", -1);

        for (String part : parts) {
            int equals = part.indexOf('=');
            String rawName = equals < 0 ? part : part.substring(0, equals);
            String rawValue = equals < 0 ? "" : part.substring(equals + 1);
            parameters.add(new Parameter(
                    URLEncoding.encodeQueryComponent(decodePercent(rawName)),
                    URLEncoding.encodeQueryComponent(decodePercent(rawValue))));
        }

        parameters.sort(Comparator.comparing(Parameter::getName).thenComparing(Parameter::getValue));

        StringBuilder result = new StringBuilder();
        for (Parameter parameter : parameters) {
            if (result.length() > 0)
                result.append('&');
            result.append(parameter.name).append('=').append(parameter.value);
        }

        return result.toString();
    }

    /**
     * Decodes percent-escaped UTF-8 bytes without applying form-url-decoding rules.
     *
     * @param raw raw URI component
     * @return decoded text
     * @throws IllegalArgumentException if a percent escape is malformed
     */
    static String decodePercent(String raw) {
        StringBuilder result = new StringBuilder(raw.length());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        for (int i = 0; i < raw.length(); ) {
            if (raw.charAt(i) != '%') {
                result.append(raw.charAt(i++));
                continue;
            }

            bytes.reset();

            while (i < raw.length() && raw.charAt(i) == '%') {
                if (i + 2 >= raw.length())
                    throw new IllegalArgumentException("Malformed percent escape in request URI.");

                int high = Character.digit(raw.charAt(i + 1), 16);
                int low = Character.digit(raw.charAt(i + 2), 16);

                if (high < 0 || low < 0)
                    throw new IllegalArgumentException("Malformed percent escape in request URI.");

                bytes.write((high << 4) | low);
                i += 3;
            }

            result.append(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        }

        return result.toString();
    }

    /**
     * Immutable encoded query parameter.
     */
    public static final class Parameter {
        /**
         * Encoded query parameter name.
         */
        private final String name;

        /**
         * Encoded query parameter value.
         */
        private final String value;

        /**
         * Creates an encoded query parameter.
         *
         * @param name  encoded parameter name
         * @param value encoded parameter value
         */
        Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the encoded parameter name.
         *
         * @return encoded name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the encoded parameter value.
         *
         * @return encoded value
         */
        public String getValue() {
            return value;
        }
    }
}
