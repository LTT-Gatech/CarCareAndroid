package com.teamltt.carcare.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.teamltt.carcare.*;

public class CarInfoEditActivity extends AppCompatActivity {

    private boolean edited = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);
        //grab info from database or whatever
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    protected void saveInfo(View view) {
        edited = true;
        back(view);
    }
    protected void back(View view) {
        Intent intent = new Intent(this, com.teamltt.carcare.HomeActivity.class);
        startActivity(intent);
    }
    //on exit: write to database from all fields
    protected void onPause() {
        super.onPause();
        if (edited) {
            //save info to database here only if the correct button was pressed

        }
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
        }
        return super.onOptionsItemSelected(item);
    }
}
