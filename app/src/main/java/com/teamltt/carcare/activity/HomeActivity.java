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

package com.teamltt.carcare.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.MyObdResponseRecyclerViewAdapter;
import com.teamltt.carcare.fragment.ResponseFragment;
import com.teamltt.carcare.fragment.SimpleDividerItemDecoration;
import com.teamltt.carcare.model.ObdContent;
import com.teamltt.carcare.model.Response;
import com.teamltt.carcare.service.BtStatusDisplay;
import com.teamltt.carcare.service.ObdBluetoothService;

import java.util.List;

public class HomeActivity extends BaseActivity implements BtStatusDisplay, IObserver, ResponseFragment.OnListFragmentInteractionListener {

    // Used to keep track of the items in the RecyclerView
    private RecyclerView.Adapter responseListAdapter;

    ObdBluetoothService btService;
    Intent btServiceIntent;
    boolean bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_home;
        includeDrawer = true;
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String firstName = intent.getStringExtra(LoginActivity.EXTRA_FIRST_NAME);
        String lastName = intent.getStringExtra(LoginActivity.EXTRA_LAST_NAME);
        String userId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID);

        // Add user's name to the screen to show successful sign-in for demo
        ((TextView) findViewById(R.id.tv_welcome)).setText(getString(R.string.welcome_text, firstName));

        btServiceIntent = new Intent(this, ObdBluetoothService.class);
        // Stop any existing services, we don't need more than one running
        stopService(btServiceIntent);
        // Now start the new service
        startService(btServiceIntent);

        // Set up the list for responses
        responseListAdapter = new MyObdResponseRecyclerViewAdapter(ObdContent.ITEMS, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.obd_reponse_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
            recyclerView.setAdapter(responseListAdapter);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bound) {
            bindService(btServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        displayStaticData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            btService.unobserveDatabase(HomeActivity.this);
            unbindService(mConnection);
            // should this be removed from here since it is done in mConnection.onServiceDisconnected?
            bound = false;
        }
    }

    @Override
    public void onListFragmentInteraction(Response item) {
        Log.i("Response Card", item.toString());
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Bound to the bluetooth service, cast binder and get service instance
            ObdBluetoothService.ObdServiceBinder binder = (ObdBluetoothService.ObdServiceBinder) service;
            btService = binder.getService();
            btService.observeDatabase((HomeActivity.this));
            btService.addDisplay(HomeActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void update(IObservable o, Bundle args) {
        if (args != null && o instanceof DbHelper) {
            DbHelper dbHelper = (DbHelper) o;
            long[] responseIds = args.getLongArray(ResponseContract.ResponseEntry.COLUMN_NAME_ID + "_ARRAY");
            List<Response> items = dbHelper.getResponsesById(responseIds);
            ObdContent.setItems(items);
            responseListAdapter.notifyDataSetChanged();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case (R.id.action_settings):
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_help):
                // TODO Add toast
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void displayStatus(String status) {
        ((TextView) findViewById(R.id.status_bt)).setText(status);
    }

    /**
     * Starts a new trip or finishes the current one
     * @param item The button that was pressed
     */
    public void toggleLogging(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.stop_logging))) {
            // Stop the service's work
            item.setTitle(getString(R.string.start_logging));
            btService.stopTrip();
        } else {
            // Make service start doing work
            item.setTitle(getString(R.string.stop_logging));
            btService.startNewTrip();
        }

    }

    public void displayStaticData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = preferences.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "");

        TextView tvStaticDataName = ((TextView) findViewById(R.id.tvStaticDataName));
        TextView tvStaticDataValue = ((TextView) findViewById(R.id.tvStaticDataValue));

        String tvTextName = "";
        String tvTextValue = "";

        if (preferences.getBoolean("sEngineTemp", false)) {
            //TODO Read from database
            tvTextName +="Engine Temperature:";
            tvTextValue += "80 F ";
        }

        if (preferences.getBoolean("sMPG", false)) {
            //TODO Read from database
            tvTextName += "\nCurrent Miles Per Gallon:";
            tvTextValue += "\n35 mpg";
        }

        if (preferences.getBoolean("sMPH", false)) {
            //TODO Read from database
            tvTextName += "\nCurrent Miles Per Hour:";
            tvTextValue += "\n60 mph";
        }

        tvStaticDataName.setText(tvTextName);
        tvStaticDataValue.setText(tvTextValue);
    }
}
