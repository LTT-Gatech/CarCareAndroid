/*
 ** Copyright 2017, Team LTT
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.teamltt.carcare.database.contract;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.teamltt.carcare.database.DbHelper;

public class TripContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TripEntry.COLUMN_NAME_ID + " (" +
            // trip_id INTEGER PRIMARY KEY AUTOINCREMENT
            TripEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            // vehicle_id INTEGER REFERENCES vehicles(vehicle_id)
            TripEntry.COLUMN_NAME_VEHICLE_ID + " INTEGER REFERENCES " +
            VehicleContract.VehicleEntry.TABLE_NAME + "(" + VehicleContract.VehicleEntry.COLUMN_NAME_ID + ")," +
            TripEntry.COLUMN_NAME_START_TIME + " DATETIME," +
            TripEntry.COLUMN_NAME_END_TIME + " DATETIME" +
            ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME;

    public static long createNewTrip(SQLiteDatabase db, long vehicleId) {
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        ContentValues values = new ContentValues();
        values.put(TripEntry.COLUMN_NAME_VEHICLE_ID, vehicleId);
        values.put(TripEntry.COLUMN_NAME_START_TIME, DbHelper.now());
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
