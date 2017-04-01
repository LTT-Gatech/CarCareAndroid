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
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
//import com.teamltt.carcare.fragment.MyDynamicDataAdapter;
import com.teamltt.carcare.fragment.MyObdResponseRecyclerViewAdapter;
import com.teamltt.carcare.fragment.RealTimeUpdates;
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

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private GraphView gvDynamicData;
    private RealTimeUpdates realTimeUpdates;

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

        addDynamicData();

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
            List<Response> items = dbHelper.getResponsesByIds(responseIds);
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
                Toast.makeText(this, "Have you tried turning it off and back on?",
                        Toast.LENGTH_SHORT).show();
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

    public void displayStaticData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = preferences.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "");

        LinearLayout lvStaticData = ((LinearLayout) findViewById(R.id.static_data));
        lvStaticData.removeAllViews();

        boolean haveStatic = false;

        if (preferences.getBoolean(getString(R.string.key_engine_temp), false)) {
            //TODO Read from database
            lvStaticData.addView(createDataView(createTextView("Engine Temperature:"), createTextView("80 F")));

            haveStatic = true;
        }

        if (preferences.getBoolean(getString(R.string.key_mpg), false)) {
            //TODO Read from database
            lvStaticData.addView(createDataView(createTextView("Current Miles Per Gallon:"), createTextView("35 mpg")));
            haveStatic = true;
        }

        if (preferences.getBoolean(getString(R.string.key_mph), false)) {
            //TODO Read from database
            lvStaticData.addView(createDataView(createTextView("Current Miles Per Hour:"), createTextView("60 mph")));

            haveStatic = true;
        }


        if (!haveStatic) {
            findViewById(R.id.static_data_card).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.static_data_card).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Creates a horizontal Linear layout for static data so that it can be put in the static data Card
     *
     * @param title TextView of the content on the left side of the line
     * @param value TextView of the content on the right side of the line
     * @return A LinearLayout to be added to the static CardView's vertical LinearLayout
     */
    public LinearLayout createDataView(TextView title, TextView value) {
        // Default orientation is horizontal
        LinearLayout dataLine = new LinearLayout(this);
        // Add title/name of data
        dataLine.addView(title);

        // Add spacer to push the title to the left and the value to the right
        Space space = new Space(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        dataLine.addView(space);

        // Add value of data
        dataLine.addView(value);
        return dataLine;
    }

    /**
     * Creates a TextView out of text that needs to go in the static CardView
     *
     * @param text text of the text view
     * @return A TextView with LinearLayout LayoutParams
     */
    public TextView createTextView(String text) {
        TextView newTextView = new TextView(this);
        newTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 0f));
        newTextView.setText(text);
        return newTextView;
    }

    public void addDynamicData() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        realTimeUpdates = new RealTimeUpdates();

        // specify an adapter
        // This branch doesn't have the settings, so the graphs should change based on the
        // specified
//        mAdapter = new MyDynamicDataAdapter();
//        mRecyclerView.setAdapter(mAdapter);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RealTimeUpdates fragment = new RealTimeUpdates();
        fragmentTransaction.add(R.id.dynamic_data_frame, fragment);
        fragmentTransaction.commit();
    }
}
