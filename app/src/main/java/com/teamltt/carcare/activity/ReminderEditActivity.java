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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.contract.ReminderContract;
import com.teamltt.carcare.fragment.DatePickerFragment;
import com.teamltt.carcare.model.Reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReminderEditActivity extends AppCompatActivity implements OnItemSelectedListener {

    private boolean edited;
    private String featureId; //based on some id for the features reminders can check for, i.e. oil pressure, "Date" if type is date
    private int reminderId; //if editing a reminder, id is passed in as an extra from ReminderActivity. Set to -2 otherwise
    private int vehicleId;
    private String formattedDate;
    private static final String TAG = "ReminderEditActivity";

    private DbHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                reminderId = -2;
            } else {
                reminderId = extras.getInt("reminder_id");
            }
        } else {
            reminderId = (Integer) savedInstanceState.getSerializable("reminder_id");
        }


        dbHelper = new DbHelper(this);
        //TODO: set vehicleId somewhere
        vehicleId = 0;

        edited = false;
        updateUI();
    }

    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        updateUI();

    }

    public void saveInfo(View view) {
        edited = true;
        cancel(view);
    }
    public void cancel(View view) { finish(); }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (edited) {
            if (saveReminder()) {
                Log.e(TAG, "Could not update reminder");
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    //changes visibility of allowed user reminder settings depending on whether 'reminder by feature'
    //or 'reminder by date' is selected
    //also updates featureId
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        RelativeLayout featureLayout = (RelativeLayout) findViewById(R.id.layout_feature);
        LinearLayout timeLayout = (LinearLayout) findViewById(R.id.layout_time);
        Log.e(TAG, "pos = "+pos);
        if (pos == 0) {
            featureLayout.setVisibility(View.VISIBLE);
            timeLayout.setVisibility(View.GONE);
            Spinner spinner = (Spinner) findViewById(R.id.spinner_feature);
            featureId = spinner.getSelectedItem().toString();
        } else if (pos == 1) {
            featureLayout.setVisibility(View.GONE);
            timeLayout.setVisibility(View.VISIBLE);
            featureId = "Date"; //if checking for date, featureId is -2 for now
        }
    }

    //necessary for implementing OnItemSelectedListener
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void updateUI() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_reminder_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.reminder_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinner = (Spinner) findViewById(R.id.spinner_feature);
        adapter = ArrayAdapter.createFromResource(this, R.array.feature_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner = (Spinner) findViewById(R.id.spinner_comparison);
        adapter = ArrayAdapter.createFromResource(this, R.array.comparison_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (reminderId >= 0) {
            Reminder reminder = dbHelper.getReminderByReminderId(reminderId);
            ((TextView) findViewById(R.id.field_reminder_name)).setText(reminder.getName());
            //check feature id to see if reminder checks for date or for a feature
            if (!(reminder.getFeatureId().equals("Date"))) { //if checks for feature, set feature type spinner, value, and comparison type spinner
                int position = 0;
                Spinner featureSpinner = ((Spinner) findViewById(R.id.spinner_feature));
                while (position < featureSpinner.getAdapter().getCount() &&
                        !(featureSpinner.getSelectedItem().toString().equals(reminder.getFeatureId()))) {
                    position++;
                    featureSpinner.setSelection(position, false);
                    Log.i(TAG, "checking position " + position);
                    Log.i(TAG, reminder.getFeatureId() + " vs " + featureSpinner.getSelectedItem().toString());
                }
                if (position > featureSpinner.getAdapter().getCount()) {
                    Log.i(TAG, "position went out of position, was " + position + " and max was " + featureSpinner.getAdapter().getCount());
                    featureSpinner.setSelection(0);
                }
                ((EditText) findViewById(R.id.field_value)).setText(""+reminder.getComparisonValue());
            } else {
                ((Spinner) findViewById(R.id.spinner_reminder_type)).setSelection(1); //if checks for date, set the spinner to date, populate with date
                formattedDate = reminder.getDate();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date displayableDate = format.parse(formattedDate);
                    String day = (String) DateFormat.format("dd", displayableDate);
                    String month = (String) DateFormat.format("MM", displayableDate);
                    String year = (String) DateFormat.format("yyyy", displayableDate);
                    ((TextView) findViewById(R.id.text_date)).setText(month + "/" + day + "/" + year);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    private boolean saveReminder() {
        String name = ((EditText) findViewById(R.id.field_reminder_name)).getText().toString();
        int comparison = ((Spinner) findViewById(R.id.spinner_comparison)).getSelectedItemPosition();
        String valueStr = ((EditText) findViewById(R.id.field_value)).getText().toString();
        if (!(featureId.equals("Date"))) {
            featureId = ((Spinner) findViewById(R.id.spinner_feature)).getSelectedItem().toString();
        }
        int value;
        if (valueStr.equals("")) {
            value = -1;
        } else {
            value = Integer.parseInt(valueStr);
        }

        if (reminderId < 0) {
            Log.i(TAG, "adding new reminder");
            long status = dbHelper.createNewReminder(new Reminder(reminderId, vehicleId, name, featureId, comparison, value, formattedDate, false));
            return status > 0;
        } else {
            Log.i(TAG, "editing reminder of id " + reminderId);
            long status = dbHelper.updateReminder(new Reminder(reminderId, vehicleId, name, featureId, comparison, value, formattedDate, false));
            return status > 0;
        }
    }

    public void showDatePickerDialog(View view) {
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.show(getFragmentManager(), "DateFragment");
    }

    public void setDate(int year, int month, int day) {
        TextView text = (TextView) findViewById(R.id.text_date);
        String date = month + "/" + day + "/" + year;
        text.setText(date);
        String formattedMonth = "" + month;
        String formattedDay = "" + day;
        if (formattedMonth.length() < 2) {
            formattedMonth = "0" + formattedMonth;
        }
        if (formattedDay.length() < 2) {
            formattedDay = "0" + formattedDay;
        }
        formattedDate = year + "-" + formattedMonth + "-" + formattedDay + " 00:00:00"; //TODO format this with simpleDateFormat in saveReminder instead of saving it here
    }
}
