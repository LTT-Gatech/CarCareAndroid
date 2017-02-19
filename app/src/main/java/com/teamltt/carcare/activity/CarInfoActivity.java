package com.teamltt.carcare.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.contract.VehicleContract;

public class CarInfoActivity extends AppCompatActivity {

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);
        //grab info from database or whatever and put it on the text views
        DbHelper dbHelper = new DbHelper(CarInfoActivity.this);
        db = dbHelper.getWritableDatabase();


        Cursor info = db.query(VehicleContract.VehicleEntry.TABLE_NAME, null, null, null, null, null,null);
        //HACK auto populates the database with 1 vehicle
        if (info.getCount() < 1) {
            ContentValues values = new ContentValues();
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_ID, 777);
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_VIN, "123123");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MAKE, "Toyota");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MODEL, "Yaris");
            //values.put(VehicleContract.VehicleEntry.COLUMN_NAME_YEAR, );
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_COLOR, "Silver");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_NICKNAME, "CarName");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER, "BVL3636");
            long newRowId = db.insert(VehicleContract.VehicleEntry.TABLE_NAME, null, values);
            Log.i("table printing", ""+newRowId);
        }

        info.moveToFirst();
        Log.i("id", info.getString(info.getColumnIndex(VehicleContract.VehicleEntry.COLUMN_NAME_ID)));

        TextView tv = (TextView)findViewById(R.id.fieldVIN);
        tv.setText(info.getString(info.getColumnIndex(VehicleContract.VehicleEntry.COLUMN_NAME_VIN)));
        tv = (TextView)findViewById(R.id.fieldMake);
        tv.setText(info.getString(info.getColumnIndex(VehicleContract.VehicleEntry.COLUMN_NAME_MAKE)));
        tv = (TextView)findViewById(R.id.fieldModel);
        tv.setText(info.getString(info.getColumnIndex(VehicleContract.VehicleEntry.COLUMN_NAME_MODEL)));
        tv = (TextView)findViewById(R.id.fieldColor);
        tv.setText(info.getString(info.getColumnIndex(VehicleContract.VehicleEntry.COLUMN_NAME_COLOR)));
        tv = (TextView)findViewById(R.id.fieldPlate);
        tv.setText(info.getString(info.getColumnIndex(VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER)));
        db.close();
        info.close();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    protected void editInfo(View view) {
        //go to the car info edit screen
        Intent intent = new Intent(this, CarInfoEditActivity.class);
        startActivity(intent);
    }

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
