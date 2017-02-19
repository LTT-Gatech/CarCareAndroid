package com.teamltt.carcare.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.DbHelper;
import com.teamltt.carcare.database.contract.VehicleContract;

public class CarInfoEditActivity extends AppCompatActivity {

    private boolean edited = false;
    private DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);

        dbHelper = new DbHelper(CarInfoEditActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        /*ContentValues values = new ContentValues();
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_ID, 777);
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_VIN, "123123");
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MAKE, "Toyota");
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MODEL, "Yaris");
        //values.put(VehicleContract.VehicleEntry.COLUMN_NAME_YEAR, );
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_COLOR, "Silver");
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_NICKNAME, "CarName");
        values.put(VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER, "BVL3636");
        long newRowId = db.insert(VehicleContract.VehicleEntry.TABLE_NAME, null, values);
        Log.i("table printing", ""+newRowId);*/

        Cursor info = db.query(VehicleContract.VehicleEntry.TABLE_NAME, null, null, null, null, null, null);

        info.moveToFirst();

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
        info.close();
        db.close();

    }


    public void saveInfo(View view) {
        edited = true;
        back(view);
    }

    public void back(View view) {
        finish();
    }

    //on exit: write to database from all fields
    protected void onPause() {
        super.onPause();
        if (edited) {
             /*ContentValues values = new ContentValues();
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_ID, 777);
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_VIN, "123123");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MAKE, "Toyota");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MODEL, "Yaris");
            //values.put(VehicleContract.VehicleEntry.COLUMN_NAME_YEAR, );
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_COLOR, "Silver");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_NICKNAME, "CarName");
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER, "BVL3636");
            long newRowId = db.insert(VehicleContract.VehicleEntry.TABLE_NAME, null, values);
            Log.i("table printing", ""+newRowId);*/

            String input;
            ContentValues values = new ContentValues();
            int id = 777;//this id is hardcoded until we have somewhere to store preferences

            TextView tv = (TextView)findViewById(R.id.fieldVIN);
            input = tv.getText().toString();

            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_VIN, input);
            tv = (TextView)findViewById(R.id.fieldMake);
            input = tv.getText().toString();
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MAKE, input);
            tv = (TextView)findViewById(R.id.fieldModel);
            input = tv.getText().toString();
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_MODEL, input);
            tv = (TextView)findViewById(R.id.fieldColor);
            input = tv.getText().toString();
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_COLOR, input);
            tv = (TextView)findViewById(R.id.fieldPlate);
            input = tv.getText().toString();
            values.put(VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER, input);
            //values.put(VehicleContract.VehicleEntry.COLUMN_NAME_ID, id);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long newRowId = db.update(VehicleContract.VehicleEntry.TABLE_NAME, values, "vehicle_id="+id, null);
            db.close();

            //i have no idea why this is here but im going to keep it until i can test it
            //Cursor cursor = db.query(VehicleContract.VehicleEntry.TABLE_NAME, null, null, null, null, null,null);
            //cursor.moveToFirst();


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
