package com.ajaxjs.net.ftp;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Listener for tracking upload/download progress.
 */
@Data
@Slf4j
public class ProgressListener {
    /**
     * Name of the file being transferred.
     */
    private String fileName;

    /**
     * Number of bytes already read, in kilobytes.
     */
    private volatile long bytesRead;

    /**
     * Total content length, in kilobytes.
     */
    private volatile long contentLength;

    /**
     * Updates the progress with the current byte counts.
     *
     * @param aBytesRead     number of bytes already read
     * @param aContentLength total content length in bytes
     */
    public void update(long aBytesRead, long aContentLength) {
        bytesRead = aBytesRead / 1024L;
        contentLength = aContentLength / 1024L;
        // long megaBytes = aBytesRead / 1048576L;

        log.info("Transfer file {}: {}/{}", fileName, aBytesRead, aContentLength);
    }

    /**
     * Copies data from the input stream to the output stream and reports progress.
     *
     * @param in   input stream to read from
     * @param out  output stream to write to
     * @param size expected total size of the data
     * @return total number of bytes copied
     * @throws IOException if reading, writing, or flushing fails
     */
    public long copy(InputStream in, OutputStream out, long size) throws IOException {
        if (in == null || out == null)
            throw new NullPointerException("input and output streams are required");

        byte[] buffer = new byte[8192];
        long total = 0L;
        int res;

        while ((res = in.read(buffer)) != -1) {
            if (res > 0) {
                total += res;
                out.write(buffer, 0, res);
                update(total, size);
            }
        }

        out.flush();

        return total;
    }
}
