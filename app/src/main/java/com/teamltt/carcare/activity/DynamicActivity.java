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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.GraphFragment;
import com.teamltt.carcare.fragment.MyGraphAdapter;
import com.teamltt.carcare.model.Response;
import com.teamltt.carcare.model.Trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamicActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, GraphFragment.OnGraphFragmentInteractionListener, IObservable {

    private static final String TAG = "DynamicActivity";

    private DbHelper mDbHelper;
    private List<Trip> mTrips;
    private List<String> mNames;
    private ArrayAdapter<Trip> mSpinnerAdapter;
    private RecyclerView.Adapter mGraphAdapter;
    private long mTripId;

    public void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_dynamic;
        includeDrawer = false;
        super.onCreate(savedInstanceState);

        mDbHelper = new DbHelper(this);

        mNames = new ArrayList<>();
        mGraphAdapter = new MyGraphAdapter(DynamicActivity.this, DynamicActivity.this, mNames);
        RecyclerView graphRecyclerView = (RecyclerView) findViewById(R.id.graph_list);
        if (graphRecyclerView != null) {
            graphRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DynamicActivity.this);
            graphRecyclerView.setLayoutManager(layoutManager);
            graphRecyclerView.setAdapter(mGraphAdapter);
        }

        mTrips = mDbHelper.getAllTrips();
        Collections.sort(mTrips);
        mSpinnerAdapter = new ArrayAdapter<>(DynamicActivity.this, android.R.layout.simple_spinner_item, mTrips);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner_trips);
        if (spinner != null) {
            spinner.setAdapter(mSpinnerAdapter);
            spinner.setOnItemSelectedListener(DynamicActivity.this);
            // HACK if the above line doesn't automatically call onItemSelected, uncomment the lines below
            /*
            if (mTrips.size() > 0) {
                spinner.setSelection(0, false);
            }
            */
        }

        Log.i(TAG, "mTrips: " + Arrays.toString(mTrips.toArray()));
        Log.i(TAG, "mNames: " + Arrays.toString(mNames.toArray()));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // this will get called on Spinner construction
        Log.i(TAG, "onItemSelected: " + parent + ", " + view + ", " + pos + ", " + id);
        Log.i(TAG, parent.getItemAtPosition(pos).toString());
        updateTripId(mTrips.get(pos).getId());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.i(TAG, "onNothingSelected: " + adapterView);
    }

    private void updateTripId(long tripId) {
        if (tripId != mTripId) {
            setChanged();
            mTripId = tripId;
            changed = true;
            Bundle args = new Bundle();
            args.putBoolean("RESET", true);
            mNames = mDbHelper.getAllNamesInTripId(mTripId);
            mGraphAdapter.notifyDataSetChanged();

            List<Response> responses = mDbHelper.getResponsesByTrip(mTripId);
            Map<String, ArrayList<Response>> nameToResponse = new HashMap<>();
            for (Response response : responses) {
                String name = response.name;
                if (!nameToResponse.containsKey(name)) {
                    nameToResponse.put(name, new ArrayList<Response>());
                }
                nameToResponse.get(name).add(response);
            }
            for (Map.Entry<String, ArrayList<Response>> entry : nameToResponse.entrySet()) {
                String name = entry.getKey();
                List<Response> currentResponses = entry.getValue();
                Collections.sort(currentResponses);
                args.putParcelableArrayList(ResponseContract.ResponseEntry.COLUMN_NAME_NAME + "_LIST_" + name, (ArrayList<Response>) currentResponses);
            }

            notifyObservers(args);
        }
    }

    @Override
    public void onGraphFragmentInteraction(String pId) {
        Log.i(TAG, "onGraphFragmntInteraction: " + pId);
    }

    private Set<IObserver> mObservers = new HashSet<>();

    @Override
    public void addObserver(IObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public int countObservers() {
        return mObservers.size();
    }

    @Override
    public void deleteObserver(IObserver observer) {
        mObservers.remove(observer);
    }

    @Override
    public void deleteObservers() {
        mObservers.clear();
    }

    private boolean changed = false;

    @Override
    public boolean hasChanged() {
        return changed;
    }

    private void setChanged() {
        changed = true;
    }

    private void clearChanged() {
        changed = false;
    }

    @Override
    public void notifyObservers(Bundle args) {
        if (hasChanged()) {
            for (IObserver observer : mObservers) {
                observer.update(this, args);
            }
            clearChanged();
        }
    }

    @Override
    public void notifyObservers() {
        notifyObservers(null);
    }
}
