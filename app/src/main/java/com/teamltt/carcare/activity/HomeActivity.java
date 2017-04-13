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
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.preference.PreferenceManager;
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
import com.teamltt.carcare.fragment.GraphFragment;
import com.teamltt.carcare.fragment.MyGraphAdapter;
import com.teamltt.carcare.fragment.StaticCard;
import com.teamltt.carcare.model.Reminder;
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

    ObdBluetoothService btService;
    Intent btServiceIntent;
    boolean bound;
    List<Reminder> reminders;

    private int comparisonValue = 95000; //hardcoded value to use with feature reminders until they are implemented

    private static final String TAG = "HomeActivity";

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

        staticCard = new StaticCard((CardView) findViewById(R.id.static_data_card), this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bound) {
            bindService(btServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }


        DbHelper helper = new DbHelper(HomeActivity.this);
        reminders = helper.getRemindersByVehicleId(0);
        checkReminders();
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

            IObservable observable = btService.getObservable();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
            List<String> names = new ArrayList<>();
            for (String preferenceKey : SettingsActivity.dynamicPreferenceTitles) {
                if (preferences.getBoolean("dynamic " + preferenceKey, false)) {
                    names.add(preferenceKey);
                }
            }
            mGraphAdapter = new MyGraphAdapter(HomeActivity.this, observable, names);
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

    private void checkReminders() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_alerts_content);
        if (layout != null) {
            layout.removeAllViews();
        }
        Log.i(TAG, "checking reminders");
        Iterator<Reminder> iterator = reminders.iterator();
        if (!iterator.hasNext()) {
            Log.e(TAG, "iterator does not have next");
            LinearLayout alertLayout = (LinearLayout) findViewById(R.id.layout_alerts);
            if (alertLayout != null) {
                alertLayout.setVisibility(View.GONE);
            }
        } else {
            LinearLayout alertLayout = (LinearLayout) findViewById(R.id.layout_alerts);
            if (alertLayout != null) {
                alertLayout.setVisibility(View.VISIBLE);
            }
        }
        while (iterator.hasNext()) {
            final Reminder reminder = iterator.next();
            if (reminder.getFeatureId() == -2) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = calendar.getTime();
                
                Log.i(TAG, reminder.getDate());
                try {
                    if (mdformat.parse(reminder.getDate()).before(date)) {
                        Log.i(TAG, "date is after date!");
                        //TextView textView = new TextView(this);
                        //textView.setText("Reminder " + reminder.getName() + " is triggered!");
                        TextView alertText = new TextView(this);
                        alertText.setText("Reminder " + reminder.getName() + " is active.");
                        alertText.setTextColor(Color.BLUE);
                        alertText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                viewAlert(view, "Reminder", "date", reminder.getName(), reminder.getDate());
                            }
                        });
                        if (layout != null) {
                            layout.addView(alertText);
                        }


                    } else {
                        Log.i(TAG, "date is not after date!");
                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            } else {
                Log.i(TAG, "checking for feature");
                Log.i(TAG, "comparisonType is " + reminder.getComparisonType());
                //check for hardcoded var here
                if (reminder.getComparisonType() == 0 && reminder.getComparisonValue() > comparisonValue
                        || reminder.getComparisonType() == 1 && reminder.getComparisonValue() == comparisonValue
                        || reminder.getComparisonType() == 2 && reminder.getComparisonValue() < comparisonValue) {

                    final String alertType;
                    if (reminder.getComparisonType() == 0) {
                        alertType = "mileage < ";
                        Log.i(TAG, "comparison type <");
                    } else if (reminder.getComparisonType() == 1) {
                        alertType = "mileage = ";
                        Log.i(TAG, "comparison type ==");
                    } else {
                        Log.i(TAG, "comparison type >");
                        alertType = "mileage > ";
                    }
                    TextView alertText = new TextView(this);
                    alertText.setText("Reminder " + reminder.getName() + " is active.");
                    alertText.setTextColor(Color.BLUE);
                    alertText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            viewAlert(view, "Reminder", alertType, reminder.getName(), Integer.toString(reminder.getComparisonValue()));
                            //the hardcoded mileage will eventually draw from somewhere depending on Reminder.featureId
                        }
                    });
                    if (layout != null) {
                        layout.addView(alertText);
                    }
                }

            }
        }
    }

    private void viewAlert(View view, String alertTitle, String alertType, String alertName, String alertValue) {
        Intent intent = new Intent(this, AlertActivity.class);
        String keyTitle = "alert_title"; //this is either reminder or alert
        String keyType = "alert_type";
        String keyName = "alert_name";
        String keyValue = "alert_value";
        intent.putExtra(keyTitle, alertTitle);
        intent.putExtra(keyType, alertType);
        intent.putExtra(keyName, alertName);
        intent.putExtra(keyValue, alertValue);
        startActivity(intent);
    }

}
