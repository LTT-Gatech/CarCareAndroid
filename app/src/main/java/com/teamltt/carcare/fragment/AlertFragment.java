/*
 *
 *  * Copyright 2017, Team LTT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.teamltt.carcare.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.activity.AlertActivity;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.IObservable;
import com.teamltt.carcare.database.IObserver;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.model.Reminder;
import com.teamltt.carcare.model.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AlertFragment implements IObserver {
    private List<Reminder> reminders;
    private Context context;
    private LinearLayout content;
    private LinearLayout mainLayout;

    private boolean debug = true;

    private static final String TAG = "AlertFragment";

    public AlertFragment(List<Reminder> reminders, Context context, LinearLayout content, LinearLayout mainLayout) {
        this.reminders = reminders;
        this.context = context;
        this.content = content;
        this.mainLayout = mainLayout;
    }

    public void update(IObservable o, Bundle args) {
        if (args != null && o instanceof DbHelper) {
            DbHelper dbHelper = (DbHelper) o;
            long[] responseIds = args.getLongArray(ResponseContract.ResponseEntry.COLUMN_NAME_ID + "_ARRAY");
            List<Response> items = dbHelper.getResponsesByIds(responseIds);

            for (Response response : items) {
                for (Reminder reminder : reminders) {
                    if (reminder.getFeatureId().equals(response.name)) {
                        checkFeatureReminder(reminder, response.value);
                    }
                }
            }
        }
    }

    public void checkFeatureReminder(final Reminder reminder, String value) {
        boolean showAlerts = false;
        if (!reminder.isArchived()) {
            Log.i(TAG, "checking for feature");
            Log.i(TAG, "comparisonType is " + reminder.getComparisonType());
            Log.i(TAG, "feature is " + reminder.getFeatureId());

            int intValue = 0;
            if (value != null && value.length() > 0) {
                Integer.parseInt(value);
            } else {
                Log.e(TAG, reminder.getFeatureId() + "'s response was null or an empty string, cannot compare.");
                return; //if the value cannot be parsed (due to being null or "") then we can't do any actual comparing
            }
            //check for hardcoded var here
            if (reminder.getComparisonType() == 0 && reminder.getComparisonValue() > intValue
                    || reminder.getComparisonType() == 1 && reminder.getComparisonValue() == intValue
                    || reminder.getComparisonType() == 2 && reminder.getComparisonValue() < intValue) {

                showAlerts = true;

                //add reminder to list of reminders to remove (so they don't constantly trigger)
                //triggered.add(reminder);
                //this sets the otherwise unused date field in a non date reminder to the latest trigger date for that reminder
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = calendar.getTime();
                reminder.setDate(mdformat.format(date));
                //after changing the date the reminder needs to be updated in the db
                DbHelper helper = new DbHelper(context);
                helper.updateReminder(reminder);

                final String alertType;
                if (reminder.getComparisonType() == 0) {
                    alertType = reminder.getFeatureId() + " < ";
                    Log.i(TAG, "comparison type <");
                } else if (reminder.getComparisonType() == 1) {
                    alertType = reminder.getFeatureId() + " = ";
                    Log.i(TAG, "comparison type ==");
                } else {
                    Log.i(TAG, "comparison type >");
                    alertType = reminder.getFeatureId() + " > ";
                }
                TextView alertText = new TextView(context);
                alertText.setText("Reminder " + reminder.getName() + " is active.");
                alertText.setTextColor(Color.BLUE);
                alertText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewAlert(view, "Reminder", alertType, reminder.getName(), Integer.toString(reminder.getComparisonValue()));
                        //the hardcoded mileage will eventually draw from somewhere depending on Reminder.featureId
                    }
                });
                content.addView(alertText);
                reminders.remove(reminder);
            }
        }
        if (showAlerts) {
            if (mainLayout != null) {
                mainLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
        }
    }

    public void checkDateReminders() {
        content.removeAllViews();
        Log.i(TAG, "checking reminders");

        boolean showAlerts = false;

        Iterator<Reminder> iterator = reminders.iterator();
        if (!iterator.hasNext()) {
            Log.e(TAG, "iterator does not have next");
            if (mainLayout != null) {
                mainLayout.setVisibility(View.GONE);
            }
        }
        while (iterator.hasNext()) {
            final Reminder reminder = iterator.next();
            //only check unarchived reminders
            if (!reminder.isArchived()) {
                if (reminder.getFeatureId().equals("Date")) {
                    Log.i(TAG, "checking for Date");
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = calendar.getTime();
                    Log.i(TAG, "Reminder's date is " + reminder.getDate());
                    try {
                        if (mdformat.parse(reminder.getDate()).before(date)) {
                            showAlerts = true;
                            Log.i(TAG, "today is after date!");
                            //TextView textView = new TextView(this);
                            //textView.setText("Reminder " + reminder.getName() + " is triggered!");
                            TextView alertText = new TextView(context);
                            alertText.setText("Reminder " + reminder.getName() + " is active.");
                            alertText.setTextColor(Color.BLUE);
                            alertText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    viewAlert(view, "Reminder", "Date", reminder.getName(), reminder.getDate());
                                }
                            });
                            content.addView(alertText);


                        } else {
                            Log.i(TAG, "today is not after date!");
                        }
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    if (showAlerts) {
        if (mainLayout != null) {
            mainLayout.setVisibility(View.VISIBLE);
        }
    } else {
        if (mainLayout != null) {
            mainLayout.setVisibility(View.GONE);
        }
    }

    }

    private void viewAlert(View view, String alertTitle, String alertType, String alertName, String alertValue) {
        Intent intent = new Intent(context, AlertActivity.class);
        String keyTitle = "alert_title"; //this is either reminder or alert
        String keyType = "alert_type";
        String keyName = "alert_name";
        String keyValue = "alert_value";
        intent.putExtra(keyTitle, alertTitle);
        intent.putExtra(keyType, alertType);
        intent.putExtra(keyName, alertName);
        intent.putExtra(keyValue, alertValue);
        context.startActivity(intent);
    }

    public void updateRemindersList(List<Reminder> reminders) {
        this.reminders = reminders;
    }
}
