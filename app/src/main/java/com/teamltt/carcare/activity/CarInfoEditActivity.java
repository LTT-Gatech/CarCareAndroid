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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        if (vehicleId == -1) {
            Log.e(TAG, "invalid vehicle id");
            finish();
        }
        dbHelper = new DbHelper(CarInfoEditActivity.this);
        Vehicle vehicle = dbHelper.getVehicle(vehicleId);
        if (vehicle == null || vehicleId == -1) {
            Log.e(TAG, "could not get vehicle properly");
        } else {
            updateUi(vehicle);
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
            if (updateVehicle()) {
                Log.e(TAG, "could not update vehicle");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    private void updateUi(Vehicle vehicle) {
        ((TextView) findViewById(R.id.fieldYear)).setText(vehicle.getYear());
        ((TextView) findViewById(R.id.fieldVIN)).setText(vehicle.getVin());
        ((TextView) findViewById(R.id.fieldMake)).setText(vehicle.getMake());
        ((TextView) findViewById(R.id.fieldModel)).setText(vehicle.getModel());
        ((TextView) findViewById(R.id.fieldColor)).setText(vehicle.getColor());
        ((TextView) findViewById(R.id.fieldNickname)).setText(vehicle.getNickname());
        ((TextView) findViewById(R.id.fieldPlate)).setText(vehicle.getPlateNumber());
    }

    private boolean updateVehicle() {
        String vin = ((TextView) findViewById(R.id.fieldVIN)).getText().toString();
        String make = ((TextView) findViewById(R.id.fieldMake)).getText().toString();
        String model = ((TextView) findViewById(R.id.fieldModel)).getText().toString();
        String year = ((TextView) findViewById(R.id.fieldYear)).getText().toString();
        String color = ((TextView) findViewById(R.id.fieldColor)).getText().toString();
        String nickname = ((TextView) findViewById(R.id.fieldNickname)).getText().toString();
        String plateNumber = ((TextView) findViewById(R.id.fieldPlate)).getText().toString();

        Vehicle vehicle = new Vehicle(vin, make, model, year, color, nickname, plateNumber);
        int numAffected = dbHelper.updateVehicle(vehicleId, vehicle);
        return numAffected > 0;
    }
}
