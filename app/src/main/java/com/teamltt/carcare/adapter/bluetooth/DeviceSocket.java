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

package com.teamltt.carcare.adapter.bluetooth;

import android.bluetooth.BluetoothSocket;

import com.teamltt.carcare.adapter.IObdSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DeviceSocket implements IObdSocket {
    private BluetoothSocket socket;
    private int numRead;

    public DeviceSocket(BluetoothSocket socket) {
        this.socket = socket;
        numRead = 0;
    }

    @Override
    public void connect() throws IOException {
        socket.connect();
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void close() throws IOException {
        getInputStream().close();
        getOutputStream().close();
        socket.close();
    }

    @Override
    public void writeTo(byte[] bytes) throws IOException {
        getOutputStream().write(bytes);

    }

    @Override
    public int readFrom(byte[] buffer) throws IOException {

        // Read at most 50 bytes from the stream into the buffer at offset numRead
        numRead += getInputStream().read(buffer, numRead, 50);
        // If last byte in the buffer is '>', then return a value indicating data read
        if ((char) buffer[numRead - 1] == '>') {
            int numReadFull = numRead;
            numRead = 0;
            return numReadFull;
        } else {
            return 0;
        }
    }
}
