package com.teamltt.carcare.adapter.simulator;

import com.teamltt.carcare.adapter.IObdSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jack on 1/25/2017.
 */

public class SimulatedSocket implements IObdSocket {

    public SimulatedSocket() {
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
