package com.teamltt.carcare.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.teamltt.carcare.model.Vehicle;

public class CarInfoActivity extends AppCompatActivity {

    private static final String TAG = "CarInfoActivity";

    static final String EXTRA_VEHICLE_ID = "vehicle_id";

    private DbHelper dbHelper;
    private long vehicleId;

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        setContentView(R.layout.activity_car_info);
        //grab info from database or whatever and put it on the text views
        dbHelper = new DbHelper(CarInfoActivity.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor info = db.query(VehicleContract.VehicleEntry.TABLE_NAME, null, null, null, null, null,null);
        Log.i(TAG, "count: " + info.getCount());
        vehicleId = 1;
        //HACK auto populates the database with 1 vehicle
        if (info.getCount() == 0) {
            vehicleId = dbHelper.createNewVehicle(new Vehicle("", "", "", "", "", "", ""));
            if (vehicleId == -1) {
                Log.e(TAG, "problem creating new vehicle");
                finish();
            }
        }
        info.close();
        db.close();

        Log.i(TAG, "id: " + vehicleId);
        Vehicle vehicle = dbHelper.getVehicle(vehicleId);

        updateUi(vehicle);
    }

    @Override
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
            case (R.id.action_reminder):
                intent = new Intent(this, ReminderActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void editInfo(View view) {
        //go to the car info edit screen
        Intent intent = new Intent(this, CarInfoEditActivity.class);
        intent.putExtra(EXTRA_VEHICLE_ID, vehicleId);
        startActivity(intent);
    }

    public void toggleLogging(MenuItem item) {
        //
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
}
