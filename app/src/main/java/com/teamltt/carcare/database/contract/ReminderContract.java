/*
 *
 *  * Copyright 2017, Team LTT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.teamltt.carcare.database.contract;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.teamltt.carcare.model.Reminder;

public class ReminderContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ReminderEntry.TABLE_NAME + " ("
            // vehicle_id INTEGER PRIMARY KEY
            + ReminderEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ReminderEntry.COLUMN_NAME_VEHICLE_ID + " INTEGER,"
            + ReminderEntry.COLUMN_NAME_NAME + " STRING,"
            + ReminderEntry.COLUMN_NAME_FEATURE_ID + " INTEGER,"
            + ReminderEntry.COLUMN_NAME_COMPARISON + " INTEGER,"
            + ReminderEntry.COLUMN_NAME_VALUE + " INTEGER,"
            + ReminderEntry.COLUMN_NAME_DATE + " DATE,"
            + "FOREIGN KEY(" + ReminderEntry.COLUMN_NAME_VEHICLE_ID + ") REFERENCES "
            + VehicleContract.VehicleEntry.TABLE_NAME + "(" + VehicleContract.VehicleEntry.COLUMN_NAME_ID + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE"
            + ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + ReminderContract.ReminderEntry.TABLE_NAME;

    //get one specific reminder corresponding to reminderId
    public static Cursor queryByReminderId(SQLiteDatabase db, long reminderId) {
        String table = ReminderEntry.TABLE_NAME;
        String[] columns = {
                ReminderEntry.COLUMN_NAME_VEHICLE_ID,
                ReminderEntry.COLUMN_NAME_NAME,
                ReminderEntry.COLUMN_NAME_FEATURE_ID,
                ReminderEntry.COLUMN_NAME_COMPARISON,
                ReminderEntry.COLUMN_NAME_VALUE,
                ReminderEntry.COLUMN_NAME_DATE
        };
        String selection = ReminderEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {Long.toString(reminderId)};

        return db.query(table, columns, selection, selectionArgs, null, null, null);
    }

    //get all the reminders owned by a specific vehicle
    public static Cursor queryByVehicleId(SQLiteDatabase db, long vehicleId) {
        String table = ReminderEntry.TABLE_NAME;
        String[] columns = {
                ReminderEntry.COLUMN_NAME_ID,
                ReminderEntry.COLUMN_NAME_NAME,
                ReminderEntry.COLUMN_NAME_FEATURE_ID,
                ReminderEntry.COLUMN_NAME_COMPARISON,
                ReminderEntry.COLUMN_NAME_VALUE,
                ReminderEntry.COLUMN_NAME_DATE
        };
        String selection = ReminderEntry.COLUMN_NAME_VEHICLE_ID + " = ?";
        String[] selectionArgs = {Long.toString(vehicleId)};

        return db.query(table, columns, selection, selectionArgs, null, null, null);
    }

    /**
     * @param db the writabtle SQLiteDatabase
     * @param featureId a long, the id of the feature being checked, 0 if feature is date
     * @param comparison an int, 0 if <, 1 if =, 2 if >
     * @param value an int, the value used in the comparison
     * @param date a String
     * @return the new vehicle's primary key
     */
    public static long insert(SQLiteDatabase db, long vehicleId, String name, int featureId, int comparison, int value, String date) {
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_NAME_VEHICLE_ID, vehicleId);
        values.put(ReminderEntry.COLUMN_NAME_NAME, name);
        values.put(ReminderEntry.COLUMN_NAME_FEATURE_ID, featureId);
        values.put(ReminderEntry.COLUMN_NAME_COMPARISON, comparison);
        values.put(ReminderEntry.COLUMN_NAME_VALUE, value);
        values.put(ReminderEntry.COLUMN_NAME_DATE, date);
        return db.insert(ReminderEntry.TABLE_NAME, null, values);
    }

    /**
     * @param db the writable SQLiteDatabase
     * @param reminderId a long
     * @param featureId a long, the id of the feature being checked, 0 if feature is date
     * @param comparison an int, 0 if <, 1 if =, 2 if >
     * @param value an int, the value used in the comparison
     * @param date a String
     * @return the number of rows affected
     */
    public static int update(SQLiteDatabase db, long reminderId, long vehicleId, String name, int featureId, int comparison, int value, String date) {
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_NAME_VEHICLE_ID, vehicleId);
        values.put(ReminderEntry.COLUMN_NAME_NAME, name);
        values.put(ReminderEntry.COLUMN_NAME_FEATURE_ID, featureId);
        values.put(ReminderEntry.COLUMN_NAME_COMPARISON, comparison);
        values.put(ReminderEntry.COLUMN_NAME_VALUE, value);
        values.put(ReminderEntry.COLUMN_NAME_DATE, date);
        String whereClause = ReminderEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = {Long.toString(reminderId)};
        return db.update(ReminderEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    public static int delete(SQLiteDatabase db, long reminderId) {
        String whereClause = ReminderEntry.COLUMN_NAME_ID + " = ?";
        String[] whereArgs = {Long.toString(reminderId)};
        return db.delete(ReminderEntry.TABLE_NAME, whereClause, whereArgs);
    }

    // HACK: private to prevent someone from accidentally instantiating a contract
    private ReminderContract() {
    }

    public static class ReminderEntry {
        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_NAME_ID = "reminder_id";
        public static final String COLUMN_NAME_VEHICLE_ID = VehicleContract.VehicleEntry.COLUMN_NAME_ID;
        public static final String COLUMN_NAME_NAME = "reminder_name";
        public static final String COLUMN_NAME_FEATURE_ID = "feature_id";
        public static final String COLUMN_NAME_COMPARISON = "comparison_type";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_DATE = "date";

    }
}
