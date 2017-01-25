package com.teamltt.carcare.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Jack on 1/25/2017.
 */
public interface IObdSocket {

    void connect() throws IOException;

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    boolean isConnected();

    void close() throws IOException;
}