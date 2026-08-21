/*
 * Copyright (c) 1994, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.ajaxjs.net.ftp.sun;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This is the base class for network clients.
 *
 * @author Jonathan Payne
 */
public class NetworkClient implements Closeable {
    /**
     * Proxy to use for server connections.
     */
    protected Proxy proxy = Proxy.NO_PROXY;

    /**
     * Socket for communicating with server.
     */
    protected Socket serverSocket = null;

    /**
     * Stream for printing to the server.
     */
    protected PrintStream serverOutput;

    /**
     * Buffered stream for reading replies from server.
     */
    protected InputStream serverInput;

    /**
     * Default socket read timeout in milliseconds.
     */
    protected static int defaultSoTimeout;

    /**
     * Default socket connect timeout in milliseconds.
     */
    protected static int defaultConnectTimeout;

    /**
     * Socket read timeout for this client, in milliseconds.
     */
    protected int readTimeout = -1;

    /**
     * Socket connect timeout for this client, in milliseconds.
     */
    protected int connectTimeout = -1;

    /**
     * Name of encoding to use for output.
     */
    protected String encoding = "ISO-8859-1";

    static {
        final int[] vals = {0, 0};
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 300000);
            vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 300000);

            return null;
        });

        if (vals[0] == 0)
            defaultSoTimeout = -1;
        else
            defaultSoTimeout = vals[0];

        if (vals[1] == 0)
            defaultConnectTimeout = -1;
        else
            defaultConnectTimeout = vals[1];
    }

    /**
     * Changes the control connection encoding without reconnecting.
     *
     * @param encoding character encoding name
     * @throws IOException if the encoding is unsupported or the output stream cannot be obtained
     */
    protected void setControlEncoding(String encoding) throws IOException {
        this.encoding = encoding;

        if (serverSocket != null)
            serverOutput = new PrintStream(new BufferedOutputStream(serverSocket.getOutputStream()), true, encoding);
    }

    /**
     * Open a connection to the server.
     *
     * @param server server host name or IP address
     * @param port   server port
     * @throws IOException if the connection cannot be established
     */
    public void openServer(String server, int port) throws IOException {
        if (serverSocket != null)
            closeServer();

        serverSocket = doConnect(server, port);

        try {
            serverOutput = new PrintStream(new BufferedOutputStream(serverSocket.getOutputStream()), true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found");
        }

        serverInput = new BufferedInputStream(serverSocket.getInputStream());
    }

    /**
     * Return a socket connected to the server, with any
     * appropriate options pre-established.
     *
     * @param server server host name or IP address
     * @param port   server port
     * @return connected socket
     * @throws IOException if the connection cannot be established
     */
    protected Socket doConnect(String server, int port) throws IOException {
        Socket s;

        if (proxy != null) {
            if (proxy.type() == Proxy.Type.SOCKS)
                s = AccessController.doPrivileged((PrivilegedAction<Socket>) () -> new Socket(proxy));
            else if (proxy.type() == Proxy.Type.DIRECT)
                s = createSocket();
            else
                // Still connecting through a proxy server & port will be the proxy address and port
                s = new Socket(Proxy.NO_PROXY);
        } else
            s = createSocket();

        // Instance-specific timeouts do have priority, that means
        // connectTimeout & readTimeout (-1 means not set)
        // Then global default timeouts
        // Then no timeout.
        if (connectTimeout >= 0)
            s.connect(new InetSocketAddress(server, port), connectTimeout);
        else {
            if (defaultConnectTimeout > 0)
                s.connect(new InetSocketAddress(server, port), defaultConnectTimeout);
            else
                s.connect(new InetSocketAddress(server, port));
        }

        if (readTimeout >= 0)
            s.setSoTimeout(readTimeout);
        else if (defaultSoTimeout > 0)
            s.setSoTimeout(defaultSoTimeout);

        return s;
    }

    /**
     * The following method, createSocket, is provided to allow the
     * https client to override it so that it may use its socket factory
     * to create the socket.
     *
     * @return a new unconnected socket
     */
    protected Socket createSocket() {
        return new Socket();
    }

    /**
     * Returns the local address of the current server connection.
     *
     * @return local InetAddress
     * @throws IOException if not connected
     */
    protected InetAddress getLocalAddress() throws IOException {
        if (serverSocket == null)
            throw new IOException("not connected");

        return AccessController.doPrivileged((PrivilegedAction<InetAddress>) () -> serverSocket.getLocalAddress());
    }

    /**
     * Close an open connection to the server.
     *
     * @throws IOException if an I/O error occurs while closing
     */
    public void closeServer() throws IOException {
        if (!serverIsOpen())
            return;

        serverSocket.close();
        serverSocket = null;
        serverInput = null;
        serverOutput = null;
    }

    /**
     * Closes the current server connection.
     *
     * @throws IOException if closing the socket fails
     */
    @Override
    public void close() throws IOException {
        closeServer();
    }

    /**
     * Return server connection status.
     *
     * @return true if connected to the server
     */
    public boolean serverIsOpen() {
        return serverSocket != null;
    }

    /**
     * Create connection with host <i>host</i> on port <i>port</i>.
     *
     * @param host server host name or IP address
     * @param port server port
     * @throws IOException if the connection cannot be established
     */
    public NetworkClient(String host, int port) throws IOException {
        openServer(host, port);
    }

    /**
     * Creates an uninitialized network client.
     */
    public NetworkClient() {
    }

    /**
     * Sets the connect timeout for this client.
     *
     * @param timeout connect timeout in milliseconds
     */
    public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
    }

    /**
     * Returns the connect timeout for this client.
     *
     * @return connect timeout in milliseconds
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the read timeout for this client.
     *
     * @param timeout read timeout in milliseconds
     */
    public void setReadTimeout(int timeout) {
        if (serverSocket != null && timeout >= 0) {
            try {
                serverSocket.setSoTimeout(timeout);
            } catch (IOException e) {
                // We tried...
            }
        }

        readTimeout = timeout;
    }

    /**
     * Returns the read timeout for this client.
     *
     * @return read timeout in milliseconds
     */
    public int getReadTimeout() {
        return readTimeout;
    }
}
