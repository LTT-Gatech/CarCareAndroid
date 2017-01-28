package com.teamltt.carcare.adapter.bluetooth;

import android.bluetooth.BluetoothSocket;

import com.teamltt.carcare.adapter.IObdSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jack on 1/25/2017.
 */

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
