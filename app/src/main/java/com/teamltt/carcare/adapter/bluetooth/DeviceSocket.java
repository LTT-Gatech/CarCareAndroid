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

    public DeviceSocket(BluetoothSocket socket) {
        this.socket = socket;
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
        socket.close();
    }
}
