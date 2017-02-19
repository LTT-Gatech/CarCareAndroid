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

        import java.util.Calendar;

        import android.app.Activity;
        import android.app.DatePickerDialog;
        import android.app.Dialog;

        import android.app.DialogFragment;
        import android.app.Fragment;
        import android.app.FragmentManager;
        import android.content.Intent;
        import android.os.Bundle;

        import android.support.v7.app.AppCompatActivity;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;

        import android.view.ViewGroup;
        import android.widget.DatePicker;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.teamltt.carcare.R;
        import com.teamltt.carcare.fragment.DatePickerFragment;

public class DynamicActivity extends AppCompatActivity {
    private boolean from;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);
        from = true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dynamic, container, false);
        return view;
    }

    public void showDatePickerDialog(View v) {
        if (findViewById(R.id.buttonTo) == v) {
            from = false;
        } else {
            from = true;
        }
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.show(getFragmentManager(), "DateFragment");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void setDate(int year, int month, int day) {
        TextView tv;
        if (from) {
            tv = (TextView)findViewById(R.id.textFrom);
        } else {
            tv = (TextView)findViewById(R.id.textTo);
        }
        String date = month + "/" + day + "/" + year;
        tv.setText(date);
    }

    /*protected void openDrawer(View view) {
        if (drawer.isDrawerOpen(findViewById(android.R.id.home))) {
            drawer.closeDrawer(Gravity.LEFT);
        }
        else {
            drawer.openDrawer(Gravity.RIGHT);
        }
    }*/
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case (R.id.action_carInfo):
                intent = new Intent(this, CarInfoActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_demo):
                intent = new Intent(this, DemoActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_trips):
                intent = new Intent(this, TripsActivity.class);
                startActivity(intent);
                break;
            case (R.id.action_dynamic):
                intent = new Intent(this, DynamicActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleLogging(MenuItem item) {
        //
    }
}