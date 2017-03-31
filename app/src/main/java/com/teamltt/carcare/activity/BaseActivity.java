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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    // Logging tag
    String TAG = "BaseActivity";

    /**
     * The layout resource id that determines the content of the activity other than the drawer and toolbar
     */
    protected int activityContent;

    /**
     * Determines whether the navigation drawer can be pulled out from the left side
     * includeDrawer == false means only the app toolbar displays
     */
    protected boolean includeDrawer = false;

    /**
     * The actual navigation drawer view
     */
    protected DrawerLayout drawer;

    /**
     * Used for signing out
     */
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (includeDrawer) {
            setContentView(R.layout.activity_base);
        } else {
            setContentView(R.layout.app_bar_base);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationViewTop = (NavigationView) findViewById(R.id.nav_view_top);
            NavigationView navigationViewBottom = (NavigationView) findViewById(R.id.nav_view_bottom);
            if (navigationViewTop != null && navigationViewBottom != null) {
                navigationViewTop.setNavigationItemSelectedListener(this);
                navigationViewBottom.setNavigationItemSelectedListener(this);
            }

            drawer.findViewById(R.id.nav_view_top).bringToFront();
            drawer.findViewById(R.id.nav_view_bottom).bringToFront();
        }

        ViewStub stub = (ViewStub) findViewById(R.id.content_base);
        if (stub != null) {
            stub.setLayoutResource(activityContent);
            stub.inflate();
        }

        buildNewGoogleApiClient();
    }

    @Override
    public void onBackPressed() {
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final Intent intent;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_car_information) {
            intent = new Intent(this, CarInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.export_database) {
            DbHelper dbHelper = new DbHelper(this);
            dbHelper.exportDatabase(this);
        } else if(id == R.id.nav_search) {
            intent = new Intent(this, StaticActivity.class);
            startActivity(intent);
        } else if (id == R.id.sign_out) {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void buildNewGoogleApiClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "google api connection failed");
    }
}
