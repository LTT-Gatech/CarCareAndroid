package com.teamltt.carcare.activity;

import android.content.Intent;
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
import com.teamltt.carcare.model.Vehicle;

public class CarInfoEditActivity extends AppCompatActivity {

    private static final String TAG = "CarInfoEditActivity";

    private boolean edited = false;
    private DbHelper dbHelper;
    long vehicleId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);

        vehicleId = getIntent().getLongExtra(CarInfoActivity.EXTRA_VEHICLE_ID, -1);
        dbHelper = new DbHelper(CarInfoEditActivity.this);
        if (vehicleId == -1) {
            Log.e(TAG, "invalid vehicle id");
            finish();
        }
        Vehicle vehicle = dbHelper.getVehicle(vehicleId);
        if (vehicle == null || vehicleId == -1) {
            Log.e(TAG, "could not get vehicle properly");
        } else {
            ((TextView) findViewById(R.id.fieldVIN)).setText(vehicle.getVin());
            ((TextView) findViewById(R.id.fieldMake)).setText(vehicle.getMake());
            ((TextView) findViewById(R.id.fieldModel)).setText(vehicle.getModel());
            ((TextView) findViewById(R.id.fieldColor)).setText(vehicle.getColor());
            ((TextView) findViewById(R.id.fieldNickname)).setText(vehicle.getNickname());
            ((TextView) findViewById(R.id.fieldPlate)).setText(vehicle.getPlateNumber());
        }
    }


    public void saveInfo(View view) {
        Log.i(TAG, "saveInfo");
        edited = true;
        back(view);
    }

    public void back(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (edited) {
            String vin = ((TextView) findViewById(R.id.fieldVIN)).getText().toString();
            String make = ((TextView) findViewById(R.id.fieldMake)).getText().toString();
            String model = ((TextView) findViewById(R.id.fieldModel)).getText().toString();
            String year = ((TextView) findViewById(R.id.fieldYear)).getText().toString();
            String color = ((TextView) findViewById(R.id.fieldColor)).getText().toString();
            String nickname = ((TextView) findViewById(R.id.fieldNickname)).getText().toString();
            String plateNumber = ((TextView) findViewById(R.id.fieldPlate)).getText().toString();

            Vehicle vehicle = new Vehicle(vin, make, model, year, color, nickname, plateNumber);

            int numAffected = dbHelper.updateVehicle(vehicleId, vehicle);

            if (numAffected == 0) {
                Log.e(TAG, "database not updated");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
