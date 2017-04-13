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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "DynamicActivity";

    private DbHelper mDbHelper;
    private List<Trip> mTrips;
    private ArrayAdapter<Trip> mSpinnerAdapter;
    private long mTripId;

    public void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_dynamic;
        includeDrawer = false;
        super.onCreate(savedInstanceState);
        mTrips = new ArrayList<>();
        mSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mTrips);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner_trips);
        if (spinner != null) {
            spinner.setAdapter(mSpinnerAdapter);
            spinner.setOnItemSelectedListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDbHelper = new DbHelper(this);
        if (mTrips == null) {
            mTrips = new ArrayList<>();
        }
        mTrips.addAll(mDbHelper.getAllTrips());
        Collections.sort(mTrips);
        mSpinnerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // this will get called on Spinner instantiation
        Log.i(TAG, "onItemSelected: " + adapterView + ", " + view + ", " + i + ", " + l);
        // TODO set mTripId here with a call to updateTripId
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.i(TAG, "onNothingSelected: " + adapterView);
    }

    private void updateTripId(long tripId) {
        mTripId = tripId;
        // TODO update graphs with mTripId
    }
}
