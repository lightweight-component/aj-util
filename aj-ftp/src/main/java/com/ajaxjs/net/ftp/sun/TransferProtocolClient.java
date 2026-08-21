/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * This class implements that basic interfaces of transfer protocols.
 * It is used by subclasses implementing specific protocols.
 *
 * @author Jonathan Payne
 * @see sun.net.ftp.FtpClient
 */

public class TransferProtocolClient extends NetworkClient {
    /**
     * Debug flag for tracing protocol messages.
     */
    static final boolean debug = false;

    /**
     * Array of strings (usually one entry) for the last reply from the server.
     */
    protected Vector<String> serverResponse = new Vector<>(1);

    /**
     * Code for last reply.
     */
    protected int lastReplyCode;

    /**
     * Pulls the response from the server and returns the code as a
     * number. Returns -1 on failure.
     *
     * @return numeric reply code from the server, or -1 on failure
     * @throws IOException if an I/O error occurs
     */
    public int readServerResponse() throws IOException {
        StringBuilder replyBuf = new StringBuilder(32);
        int c;
        int continuingCode = -1;
        int code;
        String response;
        serverResponse.setSize(0);

        while (true) {
            while ((c = serverInput.read()) != -1) {
                if (c == '\r') {
                    if ((c = serverInput.read()) != '\n')
                        replyBuf.append('\r');
                }

                replyBuf.append((char) c);

                if (c == '\n')
                    break;
            }

            response = replyBuf.toString();
            replyBuf.setLength(0);

            if (debug)
                System.out.print(response);

            if (response.isEmpty())
                code = -1;
            else {
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch (NumberFormatException e) {
                    code = -1;
                } catch (StringIndexOutOfBoundsException e) {
                    /* this line doesn't contain a response code, so
                       we just completely ignore it */
                    continue;
                }
            }
            serverResponse.addElement(response);

            if (continuingCode != -1) {
                // we've seen an XXX-sequence
                if (code != continuingCode || (response.length() >= 4 && response.charAt(3) == '-'))
                    continue;
                else
                    continuingCode = -1;  // seen the end of code sequence
                break;
            } else if (response.length() >= 4 && response.charAt(3) == '-') {
                continuingCode = code;
            } else
                break;
        }

        return lastReplyCode = code;
    }

    /**
     * Sends command <i>cmd</i> to the server.
     *
     * @param cmd command string to send
     */
    public void sendServer(String cmd) {
        serverOutput.print(cmd);
        if (debug)
            System.out.print("Sending: " + cmd);
    }

    /**
     * Converts the server response into a string.
     *
     * @return first line of the server response
     */
    public String getResponseString() {
        return serverResponse.isEmpty() ? null : serverResponse.elementAt(0);
    }

    /**
     * Returns all server response strings.
     *
     * @return vector containing all response lines
     */
    public List<String> getResponseStrings() {
        return Collections.unmodifiableList(new ArrayList<String>(serverResponse));
    }

    /**
     * Standard constructor to host <i>host</i>, port <i>port</i>.
     *
     * @param host server host name or IP address
     * @param port server port
     * @throws IOException if the connection cannot be established
     */
    public TransferProtocolClient(String host, int port) throws IOException {
        super(host, port);
    }

    /**
     * Creates an uninitialized instance of this class.
     */
    public TransferProtocolClient() {
    }
}
