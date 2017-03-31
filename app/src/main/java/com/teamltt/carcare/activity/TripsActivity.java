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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.fragment.MyObdResponseRecyclerViewAdapter;
import com.teamltt.carcare.fragment.ResponseFragment;
import com.teamltt.carcare.fragment.SimpleDividerItemDecoration;
import com.teamltt.carcare.model.Response;
import com.teamltt.carcare.model.Trip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripsActivity extends AppCompatActivity implements ResponseFragment.OnListFragmentInteractionListener {

    private static final String TAG = "TripsActivity";

    private DbHelper dbHelper;

    private Spinner spinner;
    private Map<Trip, Long> tripLongMap;
    private List<Trip> trips;
    private ArrayAdapter<Trip> spinnerAdapter;

    // Used to keep track of the items in the RecyclerView
    private RecyclerView.Adapter responseListAdapter;
    private List<Response> responses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        spinner = (Spinner) findViewById(R.id.trips_spinner);
        tripLongMap = new HashMap<>();
        trips = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trips);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        responses = new ArrayList<>();
        // Set up the list for responses
        responseListAdapter = new MyObdResponseRecyclerViewAdapter(responses, this);
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
        dbHelper = new DbHelper(this);
        tripLongMap.clear();
        trips.clear();
        tripLongMap.putAll(dbHelper.getAllTrips());
        trips.addAll(tripLongMap.keySet());
        Collections.sort(trips);
        spinnerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

    @Override
    public void onListFragmentInteraction(Response item) {
        Log.i(TAG, item.toString());
    }

    /**
     * @param view from R.id.readData in R.layout.activity_trips
     */
    public void readData(View view) {
        Log.i(TAG, "readData");
        Trip trip = (Trip) spinner.getSelectedItem();
        if (tripLongMap.isEmpty()) {
            Log.e(TAG, "tripLongMap is empty");
        }
        long tripId = tripLongMap.get(trip);
        responses.clear();
        responses.addAll(dbHelper.getResponsesByTrip(tripId));
        responseListAdapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case (R.id.action_car_info):
                intent = new Intent(this, CarInfoActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_trips):
                intent = new Intent(this, TripsActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_dynamic):
                intent = new Intent(this, DynamicActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_reminder):
                intent = new Intent(this, ReminderActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleLogging(MenuItem item) {
        //
    }
}
