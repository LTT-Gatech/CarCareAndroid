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

package com.teamltt.carcare.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.github.pires.obd.commands.ObdCommand;
import com.teamltt.carcare.R;
import com.teamltt.carcare.adapter.IObdSocket;
import com.teamltt.carcare.adapter.bluetooth.DeviceSocket;
import com.teamltt.carcare.fragment.MyObdResponseRecyclerViewAdapter;
import com.teamltt.carcare.fragment.ObdResponseFragment;
import com.teamltt.carcare.fragment.SimpleDividerItemDecoration;
import com.teamltt.carcare.model.ObdContent;
import com.teamltt.carcare.model.ObdTranslator;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Demo code for Bluetooth functionality taken from
 * https://developer.android.com/guide/topics/connectivity/bluetooth.html
 */
public class DemoActivity extends AppCompatActivity implements ObdResponseFragment.OnListFragmentInteractionListener {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private IObdSocket socket;

    private BluetoothDevice obdDevice = null;

    /**
     * Controls whether the app searches for a car's OBD adapter or an internal simulator.
     */
    private boolean useSimulator = false;

    // TODO let the user edit this
    private final String obdii = "OBDII";

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
                if (device.getName() != null && device.getName().equals(obdii)) {
                    Log.i("debug BT", "desired device found");
                    obdDevice = device;
                    obdDeviceObtained();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("debug BT", "discovery unsuccessful");
                connecting = false;
                connectButton.setText(R.string.retry_connect);
            }
        }
    };

    // True after the button to connect is pressed until connected becomes true
    private boolean connecting = false;
    // UUID that is required to talk to the OBD II adapter
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // A button view that opens the socket to the OBD adapter. Text on the buttons serves as connection status
    private Button connectButton;

    // Used to keep track of the items in the RecyclerView
    private RecyclerView.Adapter mAdapter;

    /**
     * This task takes control of the device's bluetooth and opens a socket to the OBD adapter.
     */
    AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Android advises to cancel discovery before using socket.connect()
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            beginCommunication();
        }
    };

    private void beginCommunication() {
        if (socket.isConnected()) {
            // Update connection status on UI
            displayConnected();
        } else {
            connectButton.setText(R.string.retry_connect);
        }
        connecting = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        connectButton = ((Button) findViewById(R.id.connect_button));
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        // Set up the list for responses
        mAdapter = new MyObdResponseRecyclerViewAdapter(ObdContent.ITEMS, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.obd_reponse_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //FIXME I don't think onDestroy is the right place to put this. When I swiped the app from the app switcher,
        // this wasn't run. is onStop better?
        // Let go of resources
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        unregisterReceiver(bluetoothReceiver);
    }

    /**
     * Connects the Android device's bluetooth adapter with the OBD II adapter
     * TFhe user may call this function to retry connection by clicking the button again.
     *
     * @param view the onClick button @+id/connect_button
     */
    public void connect(View view) {
        if (socket == null || !socket.isConnected()) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                // Do not try to connect if the device is already trying to or if our socket is open
                if (!connecting && !(socket != null && socket.isConnected())) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        // Gives the user a chance to reject Bluetooth privileges at this time
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        // goes to onActivityResult where requestCode == REQUEST_ENABLE_BT
                    } else {
                        // If bluetooth is on, go ahead and use it
                        connecting = true;
                        getObdDevice();
                    }
                }
            }
            // Else device does not support Bluetooth
        }
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
                if (device.getName().equals(obdii)) {
                    obdDevice = device;
                    break;
                }
            }
        }
        if (obdDevice != null) {
            /* TODO a cached pair for device named "OBDII" is found if you have connected successfully. But what if
            you try using a new adapter with a new MAC address - this will need to be discovered */
            Log.i("debug BT", "pair found");
            obdDeviceObtained();
        } else {
            Log.i("debug BT", "starting discovery");
            if (bluetoothAdapter.startDiscovery()) {
                // Control goes to bluetoothReceiver member variable
                Log.i("debug BT", "discovery started");
                connectButton.setText(R.string.discovering);
            } else {
                Log.i("debug BT", "discovery not started");
                connectButton.setText(R.string.permission_fail_bt);
            }
        }
    }

    private void displayConnected() {
        connectButton.setText(R.string.connected_bt);
    }

    private void obdDeviceObtained() {
        Log.i("debug BT", "trying socket creation");
        try {
            socket = new DeviceSocket(obdDevice.createRfcommSocketToServiceRecord(uuid));
            connectButton.setText(R.string.connecting_bt);

            connectTask.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSupportedRequest(View view) {

        // Get UI Text
        Spinner selectedRequest = (Spinner) findViewById(R.id.supported_request_content);

        String request = "";
        if (selectedRequest != null) {
            request = ((String) selectedRequest.getSelectedItem());
        }

        // Translate to ObdCommand subclass
        ObdCommand command = ObdTranslator.translate(request);

        AsyncTask<ObdCommand, Void, ObdCommand> task = new AsyncTask<ObdCommand, Void, ObdCommand>() {
            @Override
            protected ObdCommand doInBackground(ObdCommand... obdCommands) {
                Log.i("library", "start thread");
                ObdCommand command = obdCommands[0];

                if (command != null) {
                    Log.i("library", command.toString());

                    if (socket != null && socket.isConnected()) {
                        try {
                            Log.i("library", "running command");
                            command.run(socket.getInputStream(), socket.getOutputStream());
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return null;
                    }
                }
                return command;
            }

            protected void onPostExecute(ObdCommand command) {

                if (command == null) {
                    return;
                }

                // Translate into english and post
                String commandEnglish = command.getName();
                String result = command.getFormattedResult();
                int nextId = mAdapter.getItemCount() + 1;
                ObdContent.addItem(ObdContent.createItemWithResponse(nextId, commandEnglish, result));
                mAdapter.notifyDataSetChanged();
            }

        };
        Log.i("library", "executing");
        task.execute(command);
        task.getStatus();
    }

    @Override
    public void onListFragmentInteraction(ObdContent.ObdResponse item) {
        Log.i("ObdResponse Card", item.toString());
    }
}