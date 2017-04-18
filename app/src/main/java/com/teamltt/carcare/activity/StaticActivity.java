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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.model.Trip;

import java.util.Collections;
import java.util.List;

public class StaticActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    protected String TAG = "StaticActivity";

    private DbHelper mDbHelper;
    private List<Trip> mTrips;
    private List<String> mNames;
    private ArrayAdapter<Trip> mSpinnerAdapter;
    /**
     * Database row id of the trip whose data is being displayed
     */
    private long displayedTripId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup activity
        activityContent = R.layout.activity_static;
        includeDrawer = false;
        super.onCreate(savedInstanceState);
        mDbHelper = new DbHelper(this);

        // Query all trips from database to populate spinner
        mTrips = mDbHelper.getAllTrips();
        Collections.sort(mTrips);

        // Set up spinner with queried trips
        mSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mTrips);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner_trips);
        if (spinner != null) {
            spinner.setAdapter(mSpinnerAdapter);
            spinner.setOnItemSelectedListener(this);
        }

        // Populate activity with data from the first trip if it exists
        if (!mTrips.isEmpty()) {
            updateTripId(mTrips.get(0).getId());
        }
    }

    private void updateTripId(long tripId) {
        if (tripId != displayedTripId) {
            displayedTripId = tripId;
            mNames = mDbHelper.getAllNamesInTripId(displayedTripId);

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // this will get called on Spinner construction
        Log.i(TAG, "onItemSelected: " + parent + ", " + view + ", " + position + ", " + id);
        Log.i(TAG, parent.getItemAtPosition(position).toString());
        updateTripId(mTrips.get(position).getId());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "onNothingSelected: " + parent);

    }
}
