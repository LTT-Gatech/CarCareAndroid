/*
 ** Copyright 2017, Team LTT
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.teamltt.carcare.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IObdSocket {

    void connect() throws IOException;

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    boolean isConnected();

    void close() throws IOException;

    /**
     * Writes a request to the socket so that it is sent to the OBD adapter
     * @param bytes the ASCII message to send
     * @throws IOException
     */
    void writeTo(byte[] bytes) throws IOException;

    /**
     * Populates buffer with the response from the OBD adapter
     * @param buffer an array to put the ASCII response in
     * @return the number of bytes that the buffer was populated with
     * @throws IOException
     */
    int readFrom(byte[] buffer) throws IOException;
}