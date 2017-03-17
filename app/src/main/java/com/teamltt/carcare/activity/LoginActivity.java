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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient googleApiClient;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private TextView tvStatus;
    private ProgressDialog progressDialog;

    public final static String EXTRA_FIRST_NAME = "com.teamltt.carcare.activity.LoginActivity.FIRSTNAME";
    public final static String EXTRA_LAST_NAME = "com.teamltt.carcare.activity.LoginActivity.LASTNAME";
    public final static String EXTRA_USER_ID = "com.teamltt.carcare.activity.LoginActivity.USERID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildNewGoogleApiClient();
        setContentView(R.layout.activity_login);

        // Set view
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        SignInButton signInButton = (SignInButton) findViewById(R.id.btnGoogleSignIn);
        if (signInButton != null) {
            signInButton.setOnClickListener(this);
            signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        }
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            handleSignInResult(opr.get());
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                    hideProgressDialog();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            tvStatus.setText(getString(R.string.signed_in_fmt, googleSignInAccount.getDisplayName()));

            String firstName = googleSignInAccount.getGivenName();
            String lastName = googleSignInAccount.getFamilyName();
            String googleId = googleSignInAccount.getId();

            if (!addUserIfNew(googleId, firstName, lastName)) {
                Log.e(TAG, "could not create new user");
            }

            // Go to Home Screen
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(EXTRA_FIRST_NAME, firstName);
            intent.putExtra(EXTRA_LAST_NAME, lastName);
            intent.putExtra(EXTRA_USER_ID, googleId);
            startActivity(intent);
            finish();
        } else {
            // Signed out, show unauthenticated UI.
            tvStatus.setText(R.string.please_sign_in);
        }
    }

    private boolean addUserIfNew(String googleId, String firstName, String lastName) {
        // Add user to database
        long result = DbHelper.DB_WRITE_ERROR;
        DbHelper dbHelper = new DbHelper(this);
        // Make sure you're not adding duplicates to the database
        if (!dbHelper.containsUser(googleId)) {
            result = dbHelper.createNewUser(googleId, firstName, lastName);
        }
        dbHelper.close();
        return result > DbHelper.DB_OK;
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGoogleSignIn:
                signIn();
                break;
        }
    }

}
