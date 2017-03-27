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
import android.widget.LinearLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityContent = R.layout.activity_reminder;
        includeDrawer = false;
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_reminder);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case (R.id.action_carInfo):
                intent = new Intent(this, CarInfoActivity.class);
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
            case (R.id.action_reminder):
                intent = new Intent(this, ReminderActivity.class);
                startActivity(intent);
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
            //create layout to hold row-/
            LinearLayout dynamicLayout = new LinearLayout(this);
            //adds edit button to
            button = new Button(this);
            button.setId(id);
            button.setText("Edit"); //this is temporary text until the buttons become drawables
            id++;
            dynamicLayout.addView(button);
            final int reminderId = info.getInt(info.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_ID));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editReminder(view, reminderId);
                }
            });
            button = new Button(this);
            button.setId(id);
            button.setText("Del");
            id++;
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
            String reminderName = info.getString(info.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_NAME));
            TextView textView = new TextView(this);
            textView.setText(reminderName);
            dynamicLayout.addView(textView);
            layout.addView(dynamicLayout);
        }
    }
}
