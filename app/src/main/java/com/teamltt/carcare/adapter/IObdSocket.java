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