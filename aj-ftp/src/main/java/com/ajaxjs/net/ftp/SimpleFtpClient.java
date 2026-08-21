package com.ajaxjs.net.ftp;

import com.ajaxjs.net.ftp.sun.TelnetInputStream;
import com.ajaxjs.net.ftp.sun.TelnetOutputStream;
import com.ajaxjs.net.ftp.sun.ftp.FtpClient;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A simple FTP client for uploading and downloading files.
 */
@Slf4j
public class SimpleFtpClient extends FtpClient {
    /**
     * Creates and connects an FTP client.
     *
     * @param server FTP server host name or IP address
     * @param port   FTP server port
     * @throws IOException if the control connection cannot be established
     */
    public SimpleFtpClient(String server, int port) throws IOException {
        super(server, port);
    }

    /**
     * Uploads a local file to the FTP server in binary mode.
     *
     * @param source local source file path
     * @param target remote destination path
     * @throws IOException if the local file cannot be read or the FTP transfer fails
     */
    public void upload(String source, String target) throws IOException {
        binary();
        Path path = new File(source).toPath();
        ProgressListener listener = new ProgressListener();
        listener.setFileName(source);

        try (TelnetOutputStream ftp = put(target);
             InputStream file = new BufferedInputStream(Files.newInputStream(path))) {
            listener.copy(file, ftp, Files.size(path));
        }

        completePendingCommand();
        log.info("Put file from {} to {}", source, target);
    }

    /**
     * Downloads to a sibling temporary file and publishes it only after the
     * server confirms the transfer. An existing target is left intact on failure.
     *
     * @param source remote source path
     * @param target local destination file path
     * @throws IOException if the temporary file cannot be created, the transfer fails,
     *                     or the completed file cannot be published
     */
    public void getFile(String source, String target) throws IOException {
        binary();
        long size = getFileSize(source);
        Path targetPath = new File(target).toPath().toAbsolutePath();
        Path parent = targetPath.getParent();

        if (parent == null)
            throw new IOException("Target has no parent directory: " + target);

        String name = targetPath.getFileName().toString();
        String prefix = name.length() >= 3 ? name : "ftp-" + name;
        Path temporary = Files.createTempFile(parent, prefix + ".", ".part");
        boolean published = false;

        try {
            try (TelnetInputStream ftp = get(source);
                 OutputStream file = new BufferedOutputStream(Files.newOutputStream(temporary))) {
                ProgressListener listener = new ProgressListener();
                listener.setFileName(target);
                listener.copy(ftp, file, size);
            }

            completePendingCommand();
            publish(temporary, targetPath);
            published = true;
            log.info("Get file from {} to {}", source, target);
        } finally {
            if (!published)
                Files.deleteIfExists(temporary);
        }
    }

    /**
     * Moves a completed temporary download to its target, preferring an atomic move.
     *
     * @param temporary completed temporary file
     * @param target    final destination file
     * @throws IOException if the file cannot be moved
     */
    static void publish(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Queries the size of a remote file.
     *
     * @param source remote file path
     * @return file size in bytes, or {@code -1} if the response has no valid size
     * @throws IOException if the FTP command cannot be completed
     */
    protected long getFileSize(String source) throws IOException {
        if (issueCommand("SIZE " + source) == FTP_SUCCESS && lastReplyCode == 213) {
            String msg = getResponseString();

            try {
                return Long.parseLong(msg.substring(3).trim());
            } catch (NumberFormatException e) {
                return -1L;
            }
        }

        return -1L;
    }
}
