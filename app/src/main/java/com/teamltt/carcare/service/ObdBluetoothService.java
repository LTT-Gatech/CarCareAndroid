/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnknownErrorException;
import com.teamltt.carcare.R;
import com.teamltt.carcare.adapter.IObdSocket;
import com.teamltt.carcare.adapter.bluetooth.DeviceSocket;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.database.contract.TripContract;
import com.teamltt.carcare.model.Response;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
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

    /**
     * Set of activities which receive status updates from the service
     */
    private Set<BtStatusDisplay> btActivities;

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

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        getObdDevice();
                        break;
                }
            }
        }
    };
    private boolean isBluetoothReceiverRegistered = false;

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
    private boolean isDiscoveryReceiverRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();
        btActivities = new HashSet<>();
        dbHelper = new DbHelper(ObdBluetoothService.this);
    }

    @Override
    public void onDestroy() {
        // release resources
        Log.i(TAG, "onDestroy");
        if (isDiscoveryReceiverRegistered) {
            unregisterReceiver(discoveryReceiver);
        }
        if (isBluetoothReceiverRegistered) {
            unregisterReceiver(bluetoothReceiver);
        }
        if (dbHelper != null) {
            dbHelper.deleteObservers();
            dbHelper.close();
        }

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");


        // Register the BroadcastReceiver
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, discoveryFilter);
        isDiscoveryReceiverRegistered = true;

        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, bluetoothFilter);
        isBluetoothReceiverRegistered = true;

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

    /**
     * Registers an Observer with the database
     *
     * @param observer An observer entity, like an Activity that shows data from the Response table
     */
    public void observeDatabase(IObserver observer) {
        dbHelper.addObserver(observer);
    }

    public void unobserveDatabase(IObserver observer) {
        dbHelper.deleteObserver(observer);
    }

    public IObservable getObservable() {
        return dbHelper;
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
            startNewTrip();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopTrip() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startNewTrip() {
        new ConnectTask().execute();
    }

    /**
     * This task takes control of the device's bluetooth and opens a socket to the OBD adapter.
     */
    private class ConnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // Connect the device's bluetooth to the OBD adapter
            try {
                // Android advises to back discovery before using socket.connect()
                if (bluetoothAdapter.isEnabled()) {

                    if (bluetoothAdapter.isDiscovering()) {
                        // Return value of cancelDiscovery will always be true here because bluetoothAdapter.isEnabled() is true
                        bluetoothAdapter.cancelDiscovery();
                    }
                    socket.connect();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // change R.id.status_bt to display connected
            if (dbHelper != null && socket.isConnected()) {
                Log.i(TAG, "bluetooth connected");
                sendToDisplays(getString(R.string.connected_bt));
                queryTask.execute();
            } else if (!bluetoothAdapter.isEnabled()) {
                Log.i(TAG, "Bluetooth is not on");
                sendToDisplays(getString(R.string.not_connecting_bt));
            } else {
                Log.i(TAG, "bluetooth not connected");
                sendToDisplays(getString(R.string.retry_connect));
            }
        }
    }

    /**
     * This task checks periodically if the car is on, then starts cycling through commands that CarCare tracks and
     * storing them in the database.
     */
    AsyncTask<Void, Void, Void> queryTask = new AsyncTask<Void, Void, Void>() {

        boolean tripEstablished = false;
        // HACK until we find a way to get the true vehicle identifier
        long vehicleId = 1;
        long tripId;

        Set<Response> newResponses = new HashSet<>();

        @Override
        protected Void doInBackground(Void... ignore) {
            try {
                if (socket.isConnected()) {
                    EchoOffCommand echo = new EchoOffCommand();
                    echo.run(socket.getInputStream(), socket.getOutputStream());
                }

                while (socket.isConnected()) {
                    // Check for car's "heartbeat"
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
                    commands.add(AirIntakeTemperatureCommand.class);
                    commands.add(BarometricPressureCommand.class);
                    commands.add(RuntimeCommand.class);
                    commands.add(SpeedCommand.class);
                    commands.add(RPMCommand.class);

                    for (Class<? extends ObdCommand> commandClass : commands) {
                        ObdCommand sendCommand = commandClass.newInstance();
                        try {
                            if (socket.isConnected()) {
                                sendCommand.run(socket.getInputStream(), socket.getOutputStream());
                            } else {
                                // In case the Bluetooth connection breaks suddenly
                                break;
                            }
                            // Get information about the command that should go in the database
                            String pId = sendCommand.getCommandPID();
                            String name = sendCommand.getName();
                            String value = sendCommand.getCalculatedResult();
                            String unit = sendCommand.getResultUnit();
                            String timestamp = DbHelper.convertDate(new Date(sendCommand.getEnd()));
                            Response response = new Response(-1, pId, name, value, unit, timestamp);
                            long responseId = dbHelper.insertResponse(tripId, response);
                            response.id = responseId;
                            if (responseId > DbHelper.DB_OK) {
                                newResponses.add(response);
                            }
                        } catch (NoDataException | UnknownErrorException e) {
                            String pId = sendCommand.getCommandPID();
                            String name = sendCommand.getName();
                            String unit = sendCommand.getResultUnit();
                            Response response = new Response(-1, pId, name, null, unit, null);
                            newResponses.add(response);
                        }
                    }
                    publishProgress();
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Void... ignore) {
            super.onProgressUpdate(ignore);
            if (!newResponses.isEmpty()) {
                // Use this bundle to notify observers
                Bundle args = new Bundle();
                // Put Trip ID in the bundle
                args.putLong(TripContract.TripEntry.COLUMN_NAME_ID, tripId);
                long[] newResponseIds = new long[newResponses.size()];
                int i = 0;
                for (Response newResponse : newResponses) {
                    // Put the row ID of the response in the bundle
                    args.putParcelable(ResponseContract.ResponseEntry.COLUMN_NAME_NAME + "_" + newResponse.name, newResponse);
                    newResponseIds[i++] = newResponse.id;
                }
                // todo remove the next line, it's no longer used
                args.putLongArray(ResponseContract.ResponseEntry.COLUMN_NAME_ID + "_ARRAY", newResponseIds);
                newResponses.clear();
                // Send the bundle to all observers
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
     *
     * @param status the current adapter state
     */
    private void sendToDisplays(String status) {
        for (BtStatusDisplay display : btActivities) {
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
