package com.teamltt.carcare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Demo code for Bluetooth functionality taken from
 * https://developer.android.com/guide/topics/connectivity/bluetooth.html
 */
public class DemoActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice obdDevice = null;
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("debug BT", "potential device found");
                Log.i("debug BT", device.toString());
                if (device.getName() != null && device.getName().equals("OBDII")) {
                    Log.i("debug BT", "desired device found");
                    obdDevice = device;
                    obdDeviceObtained();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("debug BT", "discovery unsuccessful");
                connecting = false;
                connectButton.setText("Retry Connect");
            }
        }
    };

    // True after the button to connect is pressed until connected becomes true
    private boolean connecting = false;
    // True while the app has a connected socket to the BT device
    private boolean connected = false;
    // UUID that is required to talk to the OBD II adapter
    private UUID uuid = new UUID(0L, 0x1101L);

    // A button view that opens the socket to the OBD adapter. Text on the buttons serves as connection status
    private Button connectButton;

    // Serial port input stream. Read to get responses from the OBD
    private InputStream inputStream;
    // Serial port output stream. Write to send requests to the OBD
    private OutputStream outputStream;


    /**
     * This task takes control of the device's bluetooth and opens a socket to the OBD adapter.
     * If successful, it starts another "communicate" AsyncTask
     */
    AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Android advises to cancel discovery before using socket.connect()
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (socket.isConnected()) {
                // Update connection status on UI
                displayConnected();
                communicateTask.execute();
            } else {
                connectButton.setText("Retry Connect");
            }
            connecting = false;
        }
    };

    /**
     * This AsyncTask constantly checks if there is data to be read from the OBD, and if there is, it publishes
     * that data to the UI.
     */
    private AsyncTask<Void, byte[], Void> communicateTask = new AsyncTask<Void, byte[], Void>() {
        @Override
        protected Void doInBackground(Void[] params) {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    int numRead = inputStream.read(buffer);
                    publishProgress(buffer);
                } catch (IOException e) {
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {

            readResponse(values[0]);
        }

        // If there is ever and IOException while reading data, the socket will close and the UI will be updated.
        @Override
        protected void onPostExecute(Void result) {
            connected = false;
            connectButton.setText("Reestablish Connection");
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        connectButton = ((Button) findViewById(R.id.connect_button));
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        // Will this always be the UUID in our case?
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothReceiver);
    }

    /**
     * Connects the Android device's bluetooth adapter with the OBD II adapter
     * @param view the onClick button
     */
    public void connect(View view) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!connecting && !(socket != null && socket.isConnected())) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // Gives the user a chance to reject Bluetooth privileges at this time
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    // If bluetooth is on, go ahead and use it
                    connecting = true;
                    getObdDevice();
                }
            }
        }
        // Else device does not support Bluetooth
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Log.i("debug BT", "bluetooth enabled");
            getObdDevice();
        }
    }

    /**
     * Returns a cached bluetooth device named OBDII or starts a discovery and bonds with a device like that.
     */
    private void getObdDevice() {
        // Query pair
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(("OBDII"))) {
                    obdDevice = device;
                    break;
                }
            }
        }
        if (obdDevice != null) {
            /* TODO a cached pair for device named "OBDII" is found if you have connected successfully. But what if
            you try using a new adapter with a new MAC address - this will need to be discovered 8*/
            Log.i("debug BT", "pair found");
            obdDeviceObtained();
        } else {
            Log.i("debug BT", "starting discovery");
            if (bluetoothAdapter.startDiscovery()) {
                Log.i("debug BT", "discovery started");
                connectButton.setText("Discovering Devices...");
            } else {
                Log.i("debug BT", "discovery not started");
                connectButton.setText("Bluetooth Permissions Insufficient");
            }
        }
    }

    private void displayConnected() {
        connectButton.setText("Connected");
    }

    private void obdDeviceObtained() {
        Log.i("debug BT", "trying socket creation");
        try {
            socket = obdDevice.createRfcommSocketToServiceRecord(uuid);
            connectButton.setText("Connecting...");


            connectTask.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the text from the custom request field, appends \r to it, and writes it to the socket's output stream
     * @param view onClick button
     */
    public void sendCustomRequest(View view) {
        String request = ((TextView) findViewById(R.id.requestContent)).getText().toString();
        if (socket != null && socket.isConnected()) {
            try {
                Log.i("debug BT", "sending request:\n " + request);
                outputStream.write((request + "\r").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("debug BT", "not sending request\n " + request + "\n" + " - no open socket");
        }
    }

    /**
     * A UI thread method that prints the latest response from the OBD adapter to the UI.
     * @param response a buffer of the response from the socket's output stream
     */
    public void readResponse(byte[] response) {
        Log.i("debug BT", "data read");
        String responseString = new String(response);
        Log.i("OBD response", responseString);
        ((TextView) findViewById(R.id.responseText)).setText(responseString);
    }
}

//Commands - all commands need '\r' at the end of them - all commands are sent in ascii
/*
AT commands
ATZ - reset
ATSP0 - auto find protocol
ATRV - find system voltage
IB 10 - set ISO baud rate to 10400
IB 48 - set ISO baud rate to 4800
IB 96 - set ISO baud rate to 9600
0100 - returns PIDs available under mode 01
010c - returns 4 times the RPM (in hex)
 */
