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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.contract.ReminderContract;
import com.teamltt.carcare.database.contract.VehicleContract;
import com.teamltt.carcare.model.Reminder;
import com.teamltt.carcare.model.Vehicle;

public class ReminderActivity extends BaseActivity {

    private static final String TAG = "ReminderActivity";
    private DbHelper dbHelper;
    private boolean showArchive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_reminder;
        includeDrawer = false;
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_reminder);
        Switch toggle = (Switch) findViewById(R.id.switch_archived);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showArchive = true;
                    onStart();
                } else {
                    showArchive = false;
                    onStart();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        //grab info from database or whatever and put it on the text views
        dbHelper = new DbHelper(ReminderActivity.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor info = db.query(ReminderContract.ReminderEntry.TABLE_NAME, null, null, null, null, null,null);
        Log.i(TAG, "count: " + info.getCount());

        updateUi(info);
        info.close();
        db.close();
    }

    public void newReminder(View view) {
        editReminder(view, -2);
    }

    public void editReminder(View view, int reminderId) {
        Intent intent = new Intent(this, ReminderEditActivity.class);
        String key = "reminder_id";
        intent.putExtra(key, reminderId);
        startActivity(intent);
    }

    public void activateReminder(int reminderId) {
        Reminder reminder = dbHelper.getReminderByReminderId(reminderId);
        reminder.setArchived(false);
        long status = dbHelper.updateReminder(reminder);
        if (status < 0) {
            Log.e(TAG, "Error toggling reminder archive status");
        }
    }

    public void archiveReminder(int reminderId) {
        Reminder reminder = dbHelper.getReminderByReminderId(reminderId);
        reminder.setArchived(true);
        long status = dbHelper.updateReminder(reminder);
        if (status < 0) {
            Log.e(TAG, "Error toggling reminder archive status");
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_help):
                //TODO Add toast
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleLogging(MenuItem item) {
        //
    }
    protected void updateUi(final Cursor info) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.container_reminder);
        layout.removeAllViews();
        int id = 0;
        Button button;

        while (info.moveToNext()) {
            boolean isArchived = info.getInt(info.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_ARCHIVED)) != 0;
            if (isArchived == showArchive) {
                //create layout to hold row-/
                LinearLayout dynamicLayout = new LinearLayout(this);
                //adds edit button to
                button = new Button(this);
                button.setId(id);
                final int reminderId = info.getInt(info.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_ID));

                if (isArchived) {
                    button.setText("Activate");
                    dynamicLayout.addView(button);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //toggles the reminder's archive status (to archived)
                            activateReminder(reminderId);
                            onStart();
                        }
                    });
                } else {
                    button.setText("Edit");
                    dynamicLayout.addView(button);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editReminder(view, reminderId);
                        }
                    });
                }
                id++;

                button = new Button(this);
                button.setId(id);

                if (isArchived) {
                    button.setText("Del");
                    dynamicLayout.addView(button);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //use the cursor to delete the reminder by reminder id
                            //make sure updateui gets called after that
                            dbHelper.deleteReminder(reminderId);
                            onStart();
                        }
                    });
                } else {
                    button.setText("Archive");
                    dynamicLayout.addView(button);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //toggles the reminder's archive status (to archived)
                            archiveReminder(reminderId);
                            onStart();
                        }
                    });
                }

                id++;
                final String reminderName = info.getString(info.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_NAME));


                if (isArchived) {
                    Reminder reminder = dbHelper.getReminderByReminderId(reminderId);
                    final int value = reminder.getComparisonValue();
                    final String date = reminder.getDate();
                    String comparisonType = "";
                    if (reminder.getComparisonType() == 0) {
                        comparisonType = "<";
                    } else if (reminder.getComparisonType() == 1) {
                        comparisonType = "=";
                    } else if (reminder.getComparisonType() == 2) {
                        comparisonType = ">";
                    }
                    final String featureId = info.getString(info.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_FEATURE_ID)) + " "
                            + comparisonType + " ";


                    button = new Button(this);
                    button.setId(id);
                    button.setText("View");
                    dynamicLayout.addView(button);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //shows the archived reminder's contents in the Alert/Reminder viewing activity
                            Intent intent = new Intent(ReminderActivity.this, AlertActivity.class);
                            String keyTitle = "alert_title"; //this is either reminder or alert
                            String keyType = "alert_type";
                            String keyName = "alert_name";
                            String keyValue = "alert_value";
                            String keyDate = "alert_date";
                            intent.putExtra(keyTitle, "Archived Reminder");
                            intent.putExtra(keyType, featureId);
                            intent.putExtra(keyName, reminderName);
                            intent.putExtra(keyValue, Integer.toString(value));
                            intent.putExtra(keyDate, date);
                            startActivity(intent);
                        }
                    });

                    id++;
                }

                TextView textView = new TextView(this);
                textView.setText(reminderName);
                dynamicLayout.addView(textView);
                layout.addView(dynamicLayout);
            }
        }
    }
}
