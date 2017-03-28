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
import android.view.View;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.fragment.DatePickerFragment;

public class DynamicActivity extends BaseActivity {
    private boolean from;

    public void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_dynamic;
        includeDrawer = false;
        super.onCreate(savedInstanceState);
        from = true;
    }


    public void showDatePickerDialog(View v) {
        from = findViewById(R.id.buttonTo) != v;
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.show(getFragmentManager(), "DateFragment");
    }

    /**
     * Called by a date picker fragment. TODO see DatePickerFragment
     * @param year an int
     * @param month an int
     * @param day an int
     */

    public void setDate(int year, int month, int day) {
        TextView tv;
        if (from) {
            tv = (TextView) findViewById(R.id.textFrom);
        } else {
            tv = (TextView) findViewById(R.id.textTo);
        }
        String date = month + "/" + day + "/" + year;

        if (tv != null) {
            tv.setText(date);
        }
    }
}
