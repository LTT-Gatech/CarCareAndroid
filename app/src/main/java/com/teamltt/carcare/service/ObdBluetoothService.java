/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.teamltt.carcare.R;
import com.teamltt.carcare.adapter.IObdSocket;
import com.teamltt.carcare.adapter.bluetooth.DeviceSocket;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.database.contract.TripContract;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Any Activity can start this service to request for the bluetooth to start logging OBD data to the database
 */
public class ObdBluetoothService extends Service {

    private static final String TAG = "ObdBluetoothService";
    // Text that appears in the phone's bluetooth manager for the adapter
    private final String OBDII_NAME = "OBDII";
    // UUID that is required to talk to the OBD II adapter
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Binder given to activities that use OBD data
    private final IBinder binder = new ObdServiceBinder();

    private BluetoothDevice obdDevice = null;
    private BluetoothAdapter bluetoothAdapter;
    private IObdSocket socket;

    /**
     * Abstraction layer over a database connection.
     */
    private DbHelper dbHelper;

    private int numBound = 0;

    private Set<IObserver> dbObservers = new HashSet<>();

    private boolean bluetoothReceiverRegistered = false;
    private boolean discoveryReceiverRegistered = false;

    private List<BtStatusDisplay> btActivities;

    /**
     * Used to catch bluetooth status related events.
     * Relies on the Android Manifest including android.bluetooth.adapter.action.STATE_CHANGED in a receiver tag
     */
    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "bt state receiver " + action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state){
                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        sendToDisplays(getString(R.string.connecting_bt));
                        getObdDevice();
                        break;

                }
            }
        }
    };

    /**
     * Used to catch bluetooth discovery related events.
     */
    BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "discovery receiver " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "potential device found");
                Log.i(TAG, device.toString());
                if (device.getName() != null && device.getName().equals(OBDII_NAME)) {
                    Log.i(TAG, "desired device found");
                    obdDevice = device;
                    obdDeviceObtained();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "discovery unsuccessful");
                sendToDisplays(getString(R.string.retry_connect));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "discovery started");
            }
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        btActivities = new LinkedList<>();
    }

    /**
     * Activities wanting to use this service will bind with it, but it will also be started, so in the absence of
     * activities, data should still be logged.
     */
    @Override
    public IBinder onBind(Intent intent) {
        numBound++;
        Log.i(TAG, "onBind " + numBound);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        numBound--;
        Log.i(TAG, "onUnbind " + numBound);
        return super.onUnbind(intent);
    }

    /**
     * Registers an Observer with the database
     * @param observer An observer entity, like an Activity that shows data from the Response table
     */
    public void observeDatabase(IObserver observer) {
        dbObservers.add(observer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        // Connect to the database
        dbHelper = new DbHelper(ObdBluetoothService.this);

        // Register the BroadcastReceiver
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, discoveryFilter);
        discoveryReceiverRegistered = true;

        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, bluetoothFilter);
        bluetoothReceiverRegistered = true;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Log.i(TAG, "requesting to enable BT");

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Gives the user a chance to reject Bluetooth privileges at this time
                startActivity(enableBtIntent);
                // goes to onActivityResult where requestCode == REQUEST_ENABLE_BT
            } else {
                Log.i(TAG, "adapter enabled");
                // If bluetooth is on, go ahead and use it
                getObdDevice();
            }
        } // Else does not support Bluetooth

        // START_NOT_STICKY means when the service is stopped/killed, do NOT automatically restart the service
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        // release resources
        Log.i(TAG, "onDestroy");
        if (discoveryReceiverRegistered) {
            unregisterReceiver(discoveryReceiver);
        }
        if (bluetoothReceiverRegistered) {
            unregisterReceiver(bluetoothReceiver);
        }
        if (dbHelper != null) {
            dbHelper.deleteObservers();
            dbHelper.close();
        }
    }

    /**
     * Returns a cached bluetooth device named OBDII_NAME or starts a discovery and bonds with a device like that.
     */
    private void getObdDevice() {
        // Query pair
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(OBDII_NAME)) {
                    obdDevice = device;
                    break;
                }
            }
        }
        if (obdDevice != null) {
            /* TODO a cached pair for device named "OBDII_NAME" is found if you have connected successfully. But what if
            you try using a new adapter with a new MAC address - this will need to be discovered */
            Log.i(TAG, "existing pair found");
            obdDeviceObtained();
        } else {
            Log.i(TAG, "starting discovery");
            if (bluetoothAdapter.startDiscovery()) {
                // Control goes to bluetoothReceiver member variable
                Log.i(TAG, "discovery started");
                sendToDisplays(getString(R.string.discovering));
            } else {
                Log.i(TAG, "discovery not started");
                sendToDisplays(getString(R.string.permission_fail_bt));
            }
        }
    }

    private void obdDeviceObtained() {
        Log.i(TAG, "trying socket creation");
        try {
            socket = new DeviceSocket(obdDevice.createRfcommSocketToServiceRecord(uuid));
            sendToDisplays(getString(R.string.connecting_bt));
            connectTask.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This task takes control of the device's bluetooth and opens a socket to the OBD adapter.
     */
    AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Android advises to cancel discovery before using socket.connect()
                if (bluetoothAdapter.isEnabled()) {

                    if (bluetoothAdapter.isDiscovering()) {
                        // Return value of cancelDiscovery will always be true here because bluetoothAdapter.isEnabled() is true
                        bluetoothAdapter.cancelDiscovery();
                    }
                    socket.connect();
                } else {
                    Log.i(TAG, "Bluetooth is not on");
                }

                for (IObserver observer : dbObservers) {
                    dbHelper.addObserver(observer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Add user feedback with Messenger and Handler
            // change R.id.status_bt to display connected
            Log.i(TAG, "bluetooth connected");
            if (dbHelper != null && socket.isConnected()) {
                queryTask.execute();
            } else {
                sendToDisplays(getString(R.string.retry_connect));
            }
        }
    };

    /**
     * This task checks periodically if the car is on, then starts cycling through commands that CarCare tracks and
     * storing them in the database.
     */
    AsyncTask<Void, Void, Void> queryTask = new AsyncTask<Void, Void, Void>() {

        boolean tripEstablished = false;
        // HACK until we find a way to get the true vehicle identifier
        long vehicleId = 1;
        long tripId;

        Set<Long> newResponseIds = new HashSet<>();

        @Override
        protected Void doInBackground(Void... ignore) {
            try {
                while (socket.isConnected()) {
                    // Check for can's "heartbeat"
                    ObdCommand heartbeat = new RPMCommand();
                    while (true) {
                        heartbeat.run(socket.getInputStream(), socket.getOutputStream()); // TODO catch NoDataException
                        String rpm = heartbeat.getCalculatedResult();
                        if (Integer.parseInt(rpm) > 0) {
                            break;
                        }
                    }

                    if (!tripEstablished) {
                        tripEstablished = true;
                        tripId = dbHelper.createNewTrip(vehicleId, null, null);
                        if (!(tripId > DbHelper.DB_OK)) {
                            Log.e(TAG, "could not create a trip");
                        }
                    }

                    Set<Class<? extends ObdCommand>> commands = new HashSet<>();
                    // TODO get these classes from somewhere else
                    commands.add(RuntimeCommand.class);
                    commands.add(SpeedCommand.class);

                    for (Class<? extends ObdCommand> commandClass : commands) {
                        ObdCommand sendCommand = commandClass.newInstance();
                        if (socket.isConnected()) {
                            sendCommand.run(socket.getInputStream(), socket.getOutputStream());
                        } else {
                            // In case the Bluetooth connection breaks suddenly
                            break;
                        }
                        long responseId = dbHelper.insertResponse(tripId, sendCommand.getName(),
                                sendCommand.getCommandPID(), sendCommand.getFormattedResult());
                        if (responseId > DbHelper.DB_OK) {
                            newResponseIds.add(responseId);
                        }
                    }
                    publishProgress();
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Void... ignore) {
            super.onProgressUpdate(ignore);
            if (!newResponseIds.isEmpty()) {
                Bundle args = new Bundle();
                args.putLong(TripContract.TripEntry.COLUMN_NAME_ID, tripId);

                // HACK because Java hates primitives
                Long[] foo = newResponseIds.toArray(new Long[0]);
                long[] bar = new long[foo.length];
                for (int i = 0; i < foo.length; i++) {
                    bar[i] = foo[i];
                }
                args.putLongArray(ResponseContract.ResponseEntry.COLUMN_NAME_ID + "_ARRAY", bar);
                newResponseIds.clear();
                dbHelper.notifyObservers(args);
            }
        }

        protected void onPostExecute(Void ignore) {
            // TODO Tell the user the socket was disconnected or there was another exception
            // Also give them a way to reconnect it
            Log.i(TAG, "query task on post execute");
        }

    };

    /**
     * Keeps track of actvities binding to the service so this service can update them on state.
     */
    public void addDisplay(BtStatusDisplay btActivity) {
        btActivities.add(btActivity);
    }

    /**
     * Calls displayStatus on all listening activities
     * @param status the current adapter state
     */
    private void sendToDisplays(String status) {
        for(BtStatusDisplay display : btActivities) {
            display.displayStatus(status);
        }
    }

    public class ObdServiceBinder extends Binder {
        public ObdBluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ObdBluetoothService.this;
        }
    }
}
