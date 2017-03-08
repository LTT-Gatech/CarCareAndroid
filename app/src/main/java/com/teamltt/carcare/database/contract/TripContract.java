/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.database.contract;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.teamltt.carcare.database.DbHelper;

import java.util.Date;

public class TripContract {

    private static final String TAG = "TripContract";

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TripEntry.TABLE_NAME + " ("
            // trip_id INTEGER PRIMARY KEY AUTOINCREMENT
            + TripEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TripEntry.COLUMN_NAME_VEHICLE_ID + " INTEGER,"
            + TripEntry.COLUMN_NAME_START_TIME + " DATETIME,"
            + TripEntry.COLUMN_NAME_END_TIME + " DATETIME,"
            // FOREIGN KEY(vehicle_id) REFERENCES vehicles(vehicle_id)
            + "FOREIGN KEY(" + TripEntry.COLUMN_NAME_VEHICLE_ID + ") REFERENCES "
            + VehicleContract.VehicleEntry.TABLE_NAME + "(" + VehicleContract.VehicleEntry.COLUMN_NAME_ID + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE"
            + ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME;

    public static Cursor queryAll(SQLiteDatabase db) {
        String table = TripEntry.TABLE_NAME;
        String[] columns = {
                TripEntry.COLUMN_NAME_ID,
                TripEntry.COLUMN_NAME_VEHICLE_ID,
                TripEntry.COLUMN_NAME_START_TIME,
                TripEntry.COLUMN_NAME_END_TIME
        };
        String orderBy = TripEntry.COLUMN_NAME_ID + " ASC";

        return db.query(true, table, columns, null, null, null, null, orderBy, null);
    }

    public static long insert(SQLiteDatabase db, long vehicleId, Date startTime, Date endTime) {
        ContentValues values = new ContentValues();
        values.put(TripEntry.COLUMN_NAME_VEHICLE_ID, vehicleId);
        if (startTime == null) {
            values.put(TripEntry.COLUMN_NAME_START_TIME, DbHelper.now());
        } else {
            values.put(TripEntry.COLUMN_NAME_START_TIME, DbHelper.convertDate(startTime));
        }
        values.put(TripEntry.COLUMN_NAME_END_TIME, DbHelper.convertDate(endTime));
        return db.insert(TripEntry.TABLE_NAME, null, values);
    }

    // HACK: private to prevent someone from accidentally instantiating a contract
    private TripContract() {
    }

    public static class TripEntry {
        public static final String TABLE_NAME = "trips";
        public static final String COLUMN_NAME_ID = "trip_id";
        public static final String COLUMN_NAME_VEHICLE_ID = VehicleContract.VehicleEntry.COLUMN_NAME_ID;
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
    }
}
