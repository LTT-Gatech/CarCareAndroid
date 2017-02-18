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
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.teamltt.carcare.service.BtStatusDisplay;
import com.teamltt.carcare.service.ObdBluetoothService;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements BtStatusDisplay, IObserver, ObdResponseFragment.OnListFragmentInteractionListener {

    // Used to keep track of the items in the RecyclerView
    private RecyclerView.Adapter responseListAdapter;

    ObdBluetoothService btService;
    Intent btServiceIntent;
    boolean bound;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        String firstName = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE + ".FIRSTNAME");
        String lastName = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE + ".LASTNAME");
        String userId = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE + ".USERID");

        // Add user's name to the screen to show successful sign-in for demo
        ((TextView) findViewById(R.id.tvWelcome)).setText(getString(R.string.welcome_text, firstName));

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

        DbHelper dbHelper = new DbHelper(HomeActivity.this);
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ResponseContract.ResponseEntry.TABLE_NAME, null, null, null, null, null,null);
        //placeholder table making code

        TableLayout table = (TableLayout) findViewById(R.id.table);

        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView tv = new TextView(this);
        tv.setText("R1Col1");

        TextView tv2 = new TextView(this);
        tv2.setText("R1Col777");
        tv2.setPadding(3,3,3,3);
        TableRow.LayoutParams p = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        p.weight = 1;
        tv.setLayoutParams(p);
        p.weight = 3;
        tv2.setLayoutParams(p);
        tr.addView(tv);
        tr.addView(tv2);
        table.addView(tr);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!bound) {
            bindService(btServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            bound = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection); //TODO rebind in onContinue
            bound = false;
        }
    }

    @Override
    public void onListFragmentInteraction(ObdContent.ObdResponse item) {
        Log.i("ObdResponse Card", item.toString());
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
            List<ObdContent.ObdResponse> items = dbHelper.getResponsesById(responseIds);
            ObdContent.setItems(items);
            responseListAdapter.notifyDataSetChanged();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    protected void goToDemo(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }
    protected void goToStatic(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }
    protected void goToDynamic(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }

    /**
     * From the android:onClick parameter of R.id.readData in R.layout.activity_home
     *
     * @param view the R.id.readData button
     */
    public void readData(View view) {
        // Log.i(TAG, "readData");
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
}
