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
    private UUID uuid = new UUID(0L, 0x1101L);

    private Button connectButton;
    private InputStream inputStream;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        connectButton = ((Button) findViewById(R.id.connect_button));
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothReceiver);
    }

    public void connect(View view) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!connecting && !(socket != null && socket.isConnected())) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    connecting = true;
                    getObdDevice();
                }
            }
        }
        // Else device does not support Bluetooth//
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Log.i("debug BT", "bluetooth enabled");
            getObdDevice();
            Log.i("tag", "play dammin");
        }
    }

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

            AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
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
                    AsyncTask<Void, byte[], Void> communicateTask = new AsyncTask<Void, byte[], Void>() {
                        @Override
                        protected Void doInBackground(Void[] params) {
                            byte[] buffer = new byte[1024];
                            int bytes;
                            Log.i("debug BT", "checking for data to be read");
                            while (true) {
                                try {
                                    int numRead = inputStream.read(buffer);
                                    Log.i("are we lifting", "try");
                                    publishProgress(buffer);
                                } catch (IOException e) {
                                    break;
                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(byte[]... values) {
                            Log.i("are we lifting", "try2");

                            readResponse(values[0]);
                        }

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

                    if (socket.isConnected()) {
                        displayConnected();
                        communicateTask.execute();
                        Log.i("communicating", "test");
                    } else {
                        connectButton.setText("Retry Connect");
                    }
                    connecting = false;
                }
            };
            connectTask.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUuid(View view) {
        if ((findViewById(view.getLabelFor())) != null) {
            try {
                uuid = UUID.fromString(((TextView) findViewById(view.getLabelFor())).getText().toString());
            } catch (IllegalArgumentException e) {
                TextView uuidInput = (TextView) findViewById(view.getLabelFor());
                String invalid = " - Invalid Format";
                uuidInput.setText(uuidInput.getText().toString().replace(invalid, "") + invalid);
            }
        }
    }

    public void setUuidElm(View view) {
        if ((findViewById(view.getLabelFor())) != null) {
            uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        }
    }

    public void sendRequest(View view) {
        if (socket != null && socket.isConnected()) {
            String request = ((TextView) findViewById(view.getLabelFor())).getText().toString();
            try {
                Log.i("debug BT", "writing");
                outputStream.write(request.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i("outta here", "test");
    }

    public void readResponse(byte[] response) {
        Log.i("debug BT", "data read");
        String responseString = new String(response);
        Log.i("OBD response", responseString);
        ((TextView) findViewById(R.id.responseText)).setText(responseString);
    }
}
