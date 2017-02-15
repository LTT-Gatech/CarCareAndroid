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

package com.teamltt.carcare.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.MyObdResponseRecyclerViewAdapter;
import com.teamltt.carcare.fragment.ObdResponseFragment;
import com.teamltt.carcare.fragment.SimpleDividerItemDecoration;
import com.teamltt.carcare.model.ObdContent;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements IObserver, ObdResponseFragment.OnListFragmentInteractionListener {

    private static final String TAG = "HomeActivity";

    boolean bound = false;
    // started in onCreate, bound in onStart, and unbound in onStop
    private ObdBluetoothService btService;
    private Intent btServiceIntent = new Intent(this, ObdBluetoothService.class);
    // Used to keep track of the items in the RecyclerView
    private RecyclerView.Adapter responseListAdapter;
    private TextView mStatusTextView;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Bound to the bluetooth service, cast binder and get service instance
            ObdBluetoothService.ObdServiceBinder binder = (ObdBluetoothService.ObdServiceBinder) service;
            btService = binder.getService();
            btService.observeDatabase(HomeActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Stop any existing services, we don't need more than one running
        stopService(btServiceIntent); // is this immediate?
        // Now start the new service
        startService(btServiceIntent);

        mStatusTextView = (TextView) findViewById(R.id.status_bt);
        if (mStatusTextView != null) {
            mStatusTextView.setText(R.string.connecting_bt);
        }

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
    public void onListFragmentInteraction(ObdContent.ObdResponse item) {
        Log.i(TAG, item.toString());
    }

    @Override
    public void update(IObservable o, Bundle args) {
        if (args != null && o instanceof DbHelper) {
            DbHelper dbHelper = (DbHelper) o;
            long[] responseIds = args.getLongArray(ResponseContract.ResponseEntry.COLUMN_NAME_ID + "_ARRAY");
            Cursor cursor = ResponseContract.queryByIds(dbHelper.getReadableDatabase(), responseIds);
            List<ObdContent.ObdResponse> items = new ArrayList<>();
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
                String value = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE));
                items.add(ObdContent.createItemWithResponse(((Long) id).intValue(), name, value));
            }
            cursor.close();
            ObdContent.addItems(items);
            responseListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bound) {
            bindService(btServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    /**
     * From the android:onClick parameter of R.id.readData in R.layout.activity_home
     *
     * @param view the R.id.readData button
     */
    public void readData(View view) {
        Log.i(TAG, "readData");
    }
}
