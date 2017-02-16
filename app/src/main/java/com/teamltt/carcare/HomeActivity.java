package com.teamltt.carcare;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.teamltt.carcare.activity.DemoActivity;
import com.teamltt.carcare.activity.CarInfoActivity;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(drawerToggle);*/
        /* Find Tablelayout defined in main.xml */
        TableLayout table = (TableLayout) findViewById(R.id.table);
/* Create a new row to be added. */
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView tv = new TextView(this);
        tv.setText("R1Col1");

        TextView tv2 = new TextView(this);
        tv2.setText("R1Col2");
        tv2.setPadding(3,3,3,3);
        TableRow.LayoutParams p = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        p.weight = 1;
        tv.setLayoutParams(p);
        p.weight = 3;
        tv2.setLayoutParams(p);
        tr.addView(tv);
        tr.addView(tv2);
        table.addView(tr);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    protected void goToDemo(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }
    protected void goToStatoc(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }
    protected void goToDynamic(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
