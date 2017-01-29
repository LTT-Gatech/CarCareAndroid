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

package com.teamltt.carcare.adapter.simulator;

import android.util.Log;

import com.teamltt.carcare.adapter.IObdSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

public class SimulatedSocket implements IObdSocket {

    private boolean connected = false;
    CountDownLatch latch;
    private byte[] buffer;
    private int numRead;

    public SimulatedSocket() {
        connected = true;
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
        return connected;
    }

    @Override
    public void close() throws IOException {
        // Noop, because we never really open a socket
    }

    @Override
    public void writeTo(byte[] bytes) throws IOException {
        if (buffer == null) {
            // TODO: null check on buffer
            // it sometimes is null when android studio does an Instant Run, but why?
            return;
        }

        String testResponseString = "read\n";
        byte[] testResponse = testResponseString.getBytes();
        numRead = testResponse.length;
        System.arraycopy(testResponse, 0, buffer, 0, testResponse.length);
        Log.i("debug Sim", "sending request to sim");
        latch.countDown();
    }

    @Override
    public int readFrom(byte[] buffer) throws IOException {
        this.buffer = buffer;
        latch = new CountDownLatch(1);
        try {
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return numRead;

    }
}
