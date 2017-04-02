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

package com.teamltt.carcare.database.contract;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.teamltt.carcare.database.DbHelper;

public class ResponseContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ResponseEntry.TABLE_NAME + " ("
            // response_id INTEGER PRIMARY KEY AUTOINCREMENT
            + ResponseEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ResponseEntry.COLUMN_NAME_TRIP_ID + " INTEGER,"
            // this will automatically give each row the current timestamp upon insert
            + ResponseEntry.COLUMN_NAME_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + ResponseEntry.COLUMN_NAME_NAME + " TEXT,"
            + ResponseEntry.COLUMN_NAME_PID + " TEXT,"
            + ResponseEntry.COLUMN_NAME_VALUE + " TEXT,"
            + ResponseEntry.COLUMN_NAME_UNIT + " TEXT,"
            // FOREIGN KEY(trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE ON UPDATE CASCADE
            + "FOREIGN KEY(" + ResponseEntry.COLUMN_NAME_TRIP_ID + ") REFERENCES "
            + TripContract.TripEntry.TABLE_NAME + "(" + TripContract.TripEntry.COLUMN_NAME_ID + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE"
            + ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + ResponseEntry.TABLE_NAME;

    /**
     * Abstraction method
     *
     * @param db      the writable db
     * @param tripId the trip_id from {@link TripContract.TripEntry}
     * @param name    the name of the command
     * @param pId     the process id of the command
     * @param value   the formatted result after the command returns
     * @return the new row id or a {@link DbHelper} error code
     */
    public static long insert(SQLiteDatabase db, long tripId, String pId, String name, String value, String unit) {
        ContentValues values = new ContentValues();
        values.put(ResponseEntry.COLUMN_NAME_TRIP_ID, tripId);
        values.put(ResponseEntry.COLUMN_NAME_PID, pId);
        values.put(ResponseEntry.COLUMN_NAME_NAME, name);
        values.put(ResponseEntry.COLUMN_NAME_VALUE, value);
        values.put(ResponseEntry.COLUMN_NAME_UNIT, unit);
        return db.insert(ResponseEntry.TABLE_NAME, null, values);
    }

    public static Cursor queryByTripId(SQLiteDatabase db, long tripId) {
        String table = ResponseEntry.TABLE_NAME;
        String[] columns = {
                ResponseEntry.COLUMN_NAME_ID,
//                ResponseEntry.COLUMN_NAME_TRIP_ID, // shouldn't be necessary to include in cursor
                ResponseEntry.COLUMN_NAME_TIMESTAMP,
                ResponseEntry.COLUMN_NAME_PID,
                ResponseEntry.COLUMN_NAME_NAME,
                ResponseEntry.COLUMN_NAME_VALUE,
                ResponseEntry.COLUMN_NAME_UNIT
        };
        String selection = ResponseEntry.COLUMN_NAME_TRIP_ID + " = ?";
        String[] selectionArgs = {Long.toString(tripId)};
        String orderBy = ResponseEntry.COLUMN_NAME_TIMESTAMP + " ASC";

        return db.query(table, columns, selection, selectionArgs, null, null, orderBy);
    }

    public static Cursor queryByIds(SQLiteDatabase db, long... rowIds) {
        String table = ResponseEntry.TABLE_NAME;
        String[] columns = {
                ResponseEntry.COLUMN_NAME_ID,
                ResponseEntry.COLUMN_NAME_TRIP_ID,
                ResponseEntry.COLUMN_NAME_TIMESTAMP,
                ResponseEntry.COLUMN_NAME_PID,
                ResponseEntry.COLUMN_NAME_NAME,
                ResponseEntry.COLUMN_NAME_VALUE,
                ResponseEntry.COLUMN_NAME_UNIT
        };
        String selection = DbHelper.inClauseBuilder(ResponseEntry.COLUMN_NAME_ID, rowIds.length);

        String[] selectionArgs = new String[rowIds.length];
        for (int i = 0; i < rowIds.length; i++) {
            selectionArgs[i] = Long.toString(rowIds[i]);
        }
        String orderBy = ResponseEntry.COLUMN_NAME_TIMESTAMP + " ASC";

        return db.query(table, columns, selection, selectionArgs, null, null, orderBy);
    }

    // HACK: private to prevent someone from accidentally instantiating a contract
    private ResponseContract() {
    }

    public static class ResponseEntry {
        public static final String TABLE_NAME = "responses";
        public static final String COLUMN_NAME_ID = "response_id";
        public static final String COLUMN_NAME_TRIP_ID = TripContract.TripEntry.COLUMN_NAME_ID;
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_PID = "pid";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_UNIT = "unit";
    }
}
