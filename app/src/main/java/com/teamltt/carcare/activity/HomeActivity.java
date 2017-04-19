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
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.AlertFragment;
import com.teamltt.carcare.fragment.GraphFragment;
import com.teamltt.carcare.fragment.MyGraphAdapter;
import com.teamltt.carcare.fragment.StaticCard;
import com.teamltt.carcare.model.Reminder;
import com.teamltt.carcare.model.Response;
import com.teamltt.carcare.service.BtStatusDisplay;
import com.teamltt.carcare.service.ObdBluetoothService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HomeActivity extends BaseActivity implements BtStatusDisplay, GraphFragment.OnGraphFragmentInteractionListener {

    private RecyclerView.Adapter mGraphAdapter;
    private StaticCard staticCard;
    private AlertFragment alertFragment;

    ObdBluetoothService btService;
    Intent btServiceIntent;
    boolean bound;
    List<Reminder> reminders;

    private int comparisonValue = 95000; //hardcoded value to use with feature reminders until they are implemented

    private static final String TAG = "HomeActivity";
    private DbHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_home;
        includeDrawer = true;
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String firstName = intent.getStringExtra(LoginActivity.EXTRA_FIRST_NAME);
//        String lastName = intent.getStringExtra(LoginActivity.EXTRA_LAST_NAME);
//        String userId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID);

        // Add user's name to the screen to show successful sign-in for demo
        TextView tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        if (tvWelcome != null) {
            tvWelcome.setText(getString(R.string.welcome_text, firstName));
        }

        btServiceIntent = new Intent(this, ObdBluetoothService.class);
        // Stop any existing services, we don't need more than one running
        stopService(btServiceIntent);
        // Now start the new service
        startService(btServiceIntent);

        helper = new DbHelper(HomeActivity.this);
        reminders = helper.getRemindersByVehicleId(0);

        staticCard = new StaticCard((CardView) findViewById(R.id.static_data_card), this);
        alertFragment = new AlertFragment(reminders, this, (LinearLayout) findViewById(R.id.layout_alerts_content), (LinearLayout) findViewById(R.id.layout_alerts));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bound) {
            bindService(btServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        //DbHelper helper = new DbHelper(HomeActivity.this);
        //reminders = helper.getRemindersByVehicleId(0);
        //checkDateReminders();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            // should this be removed from here since it is done in mConnection.onServiceDisconnected?
            bound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        helper = new DbHelper(HomeActivity.this);
        alertFragment.updateRemindersList(helper.getRemindersByVehicleId(0));
        alertFragment.checkDateReminders();
    }

    @Override
    public void onGraphFragmentInteraction(String pId) {
        Log.i("Graph Card", pId);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Bound to the bluetooth service, cast binder and get service instance
            ObdBluetoothService.ObdServiceBinder binder = (ObdBluetoothService.ObdServiceBinder) service;
            btService = binder.getService();
            btService.addDisplay(HomeActivity.this);

            staticCard.displayStaticData();
            btService.observeDatabase(staticCard);

            btService.observeDatabase(alertFragment);

            IObservable observable = btService.getObservable();
            mGraphAdapter = new MyGraphAdapter(HomeActivity.this, observable, HomeActivity.this);
            RecyclerView graphRecyclerView = (RecyclerView) findViewById(R.id.graph_list);
            if (graphRecyclerView != null) {
                graphRecyclerView.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this);
                graphRecyclerView.setLayoutManager(layoutManager);
                graphRecyclerView.setAdapter(mGraphAdapter);

            }

            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

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
                Toast.makeText(this, "Have you tried turning it off and back on?",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void displayStatus(String status) {
        TextView tvStatusBt = (TextView) findViewById(R.id.status_bt);
        if (tvStatusBt != null) {
            tvStatusBt.setText(status);
        }
    }

    /**
     * Starts a new trip or finishes the current one
     *
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

}
