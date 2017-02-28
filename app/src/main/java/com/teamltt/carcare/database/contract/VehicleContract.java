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

public class VehicleContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + VehicleEntry.TABLE_NAME + " (" +
            // vehicle_id INTEGER PRIMARY KEY
            VehicleEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            VehicleEntry.COLUMN_NAME_VIN + " TEXT," +
            VehicleEntry.COLUMN_NAME_MAKE + " TEXT," +
            VehicleEntry.COLUMN_NAME_MODEL + " TEXT," +
            VehicleEntry.COLUMN_NAME_YEAR + " TEXT," +
            VehicleEntry.COLUMN_NAME_COLOR + " TEXT," +
            VehicleEntry.COLUMN_NAME_NICKNAME + " TEXT," +
            VehicleEntry.COLUMN_NAME_PLATE_NUMBER + " TEXT" +
            ");";
    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME;

    public static Cursor query(SQLiteDatabase db, long vehicleId) {
        String table = VehicleEntry.TABLE_NAME;
        String[] columns = {
                VehicleEntry.COLUMN_NAME_ID,
                VehicleEntry.COLUMN_NAME_VIN,
                VehicleEntry.COLUMN_NAME_MAKE,
                VehicleEntry.COLUMN_NAME_MODEL,
                VehicleEntry.COLUMN_NAME_YEAR,
                VehicleEntry.COLUMN_NAME_COLOR,
                VehicleEntry.COLUMN_NAME_NICKNAME,
                VehicleEntry.COLUMN_NAME_PLATE_NUMBER
        };
        String selection = VehicleEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {Long.toString(vehicleId)};

        return db.query(table, columns, selection, selectionArgs, null, null, null);
    }

    /**
     * @param db
     * @param vin
     * @param make
     * @param model
     * @param year
     * @param color
     * @param nickname
     * @param plateNumber
     * @return the new vehicle's primary key
     */
    public static long insert(SQLiteDatabase db, String vin, String make, String model, String year,
                              String color, String nickname, String plateNumber) {
        ContentValues values = new ContentValues();
        values.put(VehicleEntry.COLUMN_NAME_VIN, vin);
        values.put(VehicleEntry.COLUMN_NAME_MAKE, make);
        values.put(VehicleEntry.COLUMN_NAME_MODEL, model);
        values.put(VehicleEntry.COLUMN_NAME_YEAR, year);
        values.put(VehicleEntry.COLUMN_NAME_COLOR, color);
        values.put(VehicleEntry.COLUMN_NAME_NICKNAME, nickname);
        values.put(VehicleEntry.COLUMN_NAME_PLATE_NUMBER, plateNumber);
        return db.insert(VehicleEntry.TABLE_NAME, null, values);
    }

    /**
     * @param db
     * @param vehicleId
     * @param vin
     * @param make
     * @param model
     * @param year
     * @param color
     * @param nickname
     * @param plateNumber
     * @return the number of rows affected
     */
    public static int update(SQLiteDatabase db, long vehicleId, String vin, String make, String model, String year,
                             String color, String nickname, String plateNumber) {
        ContentValues values = new ContentValues();
        values.put(VehicleEntry.COLUMN_NAME_VIN, vin);
        values.put(VehicleEntry.COLUMN_NAME_MAKE, make);
        values.put(VehicleEntry.COLUMN_NAME_MODEL, model);
        values.put(VehicleEntry.COLUMN_NAME_YEAR, year);
        values.put(VehicleEntry.COLUMN_NAME_COLOR, color);
        values.put(VehicleEntry.COLUMN_NAME_NICKNAME, nickname);
        values.put(VehicleEntry.COLUMN_NAME_PLATE_NUMBER, plateNumber);
        String whereClause = VehicleEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = {Long.toString(vehicleId)};
        return db.update(VehicleEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    // HACK: private to prevent someone from accidentally instantiating a contract
    private VehicleContract() {
    }

    public static class VehicleEntry {
        public static final String TABLE_NAME = "vehicles";
        public static final String COLUMN_NAME_ID = "vehicle_id";
        public static final String COLUMN_NAME_VIN = "vin";
        public static final String COLUMN_NAME_MAKE = "make";
        public static final String COLUMN_NAME_MODEL = "model";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_NICKNAME = "nickname";
        public static final String COLUMN_NAME_PLATE_NUMBER = "plate_number";
    }
}
