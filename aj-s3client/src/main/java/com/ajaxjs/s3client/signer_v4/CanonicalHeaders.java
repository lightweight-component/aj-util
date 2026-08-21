package com.ajaxjs.s3client.signer_v4;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Immutable canonical representation of the HTTP headers signed by SigV4.
 */
public final class CanonicalHeaders {
    /**
     * Pattern accepted for an HTTP field name participating in a signature.
     */
    private static final Pattern HEADER_NAME = Pattern.compile("[!#$%&'*+.^_`|~0-9A-Za-z-]+");

    /**
     * Runs of optional HTTP linear whitespace collapsed during canonicalization.
     */
    private static final Pattern LINEAR_WHITESPACE = Pattern.compile("[ \\t]+");

    /**
     * Semicolon-separated normalized signed-header names.
     */
    private final String names;

    /**
     * Canonical header block used in the SigV4 canonical request.
     */
    private final String canonicalizedHeaders;

    /**
     * Immutable normalized headers keyed by lowercase field name.
     */
    private final SortedMap<String, List<String>> internalMap;

    /**
     * Creates an immutable canonical header value.
     *
     * @param names                semicolon-separated signed-header names
     * @param canonicalizedHeaders canonical header block
     * @param internalMap          immutable normalized header map
     */
    private CanonicalHeaders(String names, String canonicalizedHeaders,
                             SortedMap<String, List<String>> internalMap) {
        this.names = names;
        this.canonicalizedHeaders = canonicalizedHeaders;
        this.internalMap = internalMap;
    }

    /**
     * Returns the semicolon-separated signed-header names.
     *
     * @return normalized signed-header names
     */
    public String getNames() {
        return names;
    }

    /**
     * Returns the canonical header block.
     *
     * @return canonicalized headers, ending with a newline
     */
    public String getCanonicalizedHeaders() {
        return canonicalizedHeaders;
    }

    /**
     * Returns an immutable, sorted view of the normalized signed headers.
     *
     * @return immutable normalized header map
     */
    public SortedMap<String, List<String>> getInternalMap() {
        return internalMap;
    }

    /**
     * Returns the first normalized value for a case-insensitive header name.
     *
     * @param name header name to find
     * @return first value, or an empty optional when the header is absent
     */
    public Optional<String> getFirstValue(String name) {
        if (name == null)
            return Optional.empty();

        List<String> values = internalMap.get(name.toLowerCase(Locale.ROOT));

        return values == null || values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    /**
     * Validates and canonicalizes headers according to the SigV4 rules.
     * Header values containing CR or LF are rejected to prevent header injection.
     *
     * @param headers headers to normalize and sign
     * @return immutable canonical header representation
     * @throws IllegalArgumentException if the map is empty or contains an invalid name or value
     */
    public static CanonicalHeaders build(Map<String, String> headers) {
        if (headers == null || headers.isEmpty())
            throw new IllegalArgumentException("At least one signed header is required.");

        TreeMap<String, List<String>> normalized = new TreeMap<>();
        headers.forEach((name, value) -> {
            validateHeader(name, value);
            String lowerName = name.toLowerCase(Locale.ROOT);
            normalized.computeIfAbsent(lowerName, ignored -> new ArrayList<>()).add(normalizeHeaderValue(value));
        });

        String names = String.join(";", normalized.keySet());
        StringBuilder block = new StringBuilder();
        TreeMap<String, List<String>> immutableValues = new TreeMap<>();

        normalized.forEach((name, values) -> {
            List<String> copy = Collections.unmodifiableList(new ArrayList<>(values));
            immutableValues.put(name, copy);
            block.append(name).append(':').append(String.join(",", copy)).append('\n');
        });

        return new CanonicalHeaders(names, block.toString(),
                Collections.unmodifiableSortedMap(immutableValues));
    }

    /**
     * Validates a signed HTTP header name and value.
     *
     * @param name  header field name
     * @param value header field value
     * @throws IllegalArgumentException if the name is invalid or the value is null or contains CR/LF
     */
    static void validateHeader(String name, String value) {
        if (name == null || !HEADER_NAME.matcher(name).matches())
            throw new IllegalArgumentException("Invalid signed header name.");

        if (value == null)
            throw new IllegalArgumentException("Signed header '" + name + "' has a null value.");

        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)
            throw new IllegalArgumentException("Signed header '" + name + "' contains CR or LF.");
    }

    /**
     * Trims a header value and collapses linear whitespace runs.
     *
     * @param value validated header value
     * @return normalized header value
     */
    static String normalizeHeaderValue(String value) {
        return LINEAR_WHITESPACE.matcher(value.trim()).replaceAll(" ");
    }
}
