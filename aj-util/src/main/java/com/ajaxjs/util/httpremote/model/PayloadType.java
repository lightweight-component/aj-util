package com.ajaxjs.util.httpremote.model;

/**
 * How data is sent to the server.
 */
public enum PayloadType {
    /**
     * Sends the payload as a JSON request body.
     */
    JSON_BODY,

    /**
     * Sends the payload as URL-encoded form data.
     */
    FORM,

    /**
     * Sends the payload as multipart/form-data for file uploads.
     */
    FILE_UPLOAD
}
