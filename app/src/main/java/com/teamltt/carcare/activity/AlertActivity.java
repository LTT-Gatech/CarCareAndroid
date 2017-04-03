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
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.teamltt.carcare.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlertActivity extends BaseActivity {

    private String alertTitle;
    private String alertType;
    private String alertName;
    private String alertValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_alert;
        includeDrawer = false;
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                alertTitle = "NullTitle";
                alertType = "NullType";
                alertName = "NullAlert";
                alertValue = "NullValue";
            } else {
                alertTitle = extras.getString("alert_title");
                alertType = extras.getString("alert_type");
                alertName = extras.getString("alert_name");
                alertValue = extras.getString("alert_value");
            }
        } else {
            alertTitle = (String) savedInstanceState.getSerializable("alert_title");
            alertType = (String) savedInstanceState.getSerializable("alert_type");
            alertName = (String) savedInstanceState.getSerializable("alert_name");
            alertValue = (String) savedInstanceState.getSerializable("alert_value");
        }
        updateUi();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_help):
                // TODO Add toast
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUi() {
        TextView alertTitleTv = (TextView) findViewById(R.id.text_alert_title);
        alertTitleTv.setText(alertTitle);
        TextView alertIntro = (TextView) findViewById(R.id.text_alert_intro);
        alertIntro.setText("Your reminder titled " + alertName + " was triggered.");
        if (alertType.equals("date")) {
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date convertedDate = new Date();
            String day = "";
            String month = "";
            String year = "";
            try {
                convertedDate = mdformat.parse(alertValue);
                day = (String) DateFormat.format("dd", convertedDate);
                month = (String) DateFormat.format("MM", convertedDate);
                year = (String) DateFormat.format("yyyy", convertedDate);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        } else {
            TextView alertContent = (TextView) findViewById(R.id.text_alert_content);
            alertContent.setText("Your reminder was set to " + alertType + alertValue + ".");
        }
    }
}
