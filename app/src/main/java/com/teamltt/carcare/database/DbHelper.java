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

package com.teamltt.carcare.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.teamltt.carcare.database.contract.OwnershipContract;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.database.contract.TripContract;
import com.teamltt.carcare.database.contract.UserContract;
import com.teamltt.carcare.database.contract.VehicleContract;
import com.teamltt.carcare.model.ObdContent;
import com.teamltt.carcare.model.Vehicle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DbHelper extends SQLiteOpenHelper implements IObservable {

    private static final String TAG = "DbHelper";

    // errors are negative, ok is 0, anything else is positive.
    public static final long DB_ERROR_NULL = -6;
    public static final long DB_ERROR_NOT_OPEN = -5;
    public static final long DB_ERROR_READ_ONLY = -4;
    public static final long DB_WRITE_ERROR = -1; // from SQLiteDatabase if an error occurred
    public static final long DB_OK = 0;

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "CarCare.db";

    // Format in which the database stores DateTimes. Example: 2004-12-13 13:14:15
    private static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // Format in which dates are displayed to the user. Example: Tue 07/14/02, 21:40
    private static final SimpleDateFormat readableFormat = new SimpleDateFormat("EEE MM/dd/yy, HH:mm");

    // for observer pattern to notify when data has been updated
    private Set<IObserver> observers = new HashSet<>();
    private boolean changed = false;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(OwnershipContract.SQL_CREATE_ENTRIES);
        db.execSQL(ResponseContract.SQL_CREATE_ENTRIES);
        db.execSQL(TripContract.SQL_CREATE_ENTRIES);
        db.execSQL(UserContract.SQL_CREATE_ENTRIES);
        db.execSQL(VehicleContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // HACK: lazy man's way is to drop all tables and recreate
        db.execSQL(OwnershipContract.SQL_DROP_ENTRIES);
        db.execSQL(ResponseContract.SQL_DROP_ENTRIES);
        db.execSQL(TripContract.SQL_DROP_ENTRIES);
        db.execSQL(UserContract.SQL_DROP_ENTRIES);
        db.execSQL(VehicleContract.SQL_DROP_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    private static long errorChecks(SQLiteDatabase db) {
        if (db == null) {
            return DB_ERROR_NULL;
        } else if (!db.isOpen()) {
            return DB_ERROR_NOT_OPEN;
        } else if (db.isReadOnly()) {
            return DB_ERROR_READ_ONLY;
        } else {
            return DB_OK;
        }
    }


    public static String convertDate(Date date) {
        if (date == null) {
            return null;
        }
        return sqlDateFormat.format(date);
    }


    public static String now() {
        // set the format to sql date time
        return sqlDateFormat.format(new Date());
    }

    public static String inClauseBuilder(String column, int numArgs) {
        // length will be length of column plus 5 characters " IN (" plus numArgs of "?" plus
        // numArgs of "," - 1 plus 1 for closing parentheses.
        StringBuilder res = new StringBuilder(column.length() + 5 + numArgs * 2);
        res.append(column).append(" IN (");
        for (int i = 0; i < numArgs; i++) {
            if (i != 0) {
                res.append(',');
            }
            res.append('?');
        }
        res.append(')');
        return res.toString();
    }

    private String getCursorColumn(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    public long createNewVehicle(Vehicle vehicle) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        status = VehicleContract.insert(db, vehicle.getVin(), vehicle.getMake(), vehicle.getModel(),
                vehicle.getYear(), vehicle.getColor(), vehicle.getNickname(), vehicle.getPlateNumber());
        db.close();
        setChanged(status);
        return status;
    }

    public Vehicle getVehicle(long vehicleId) {
        if (vehicleId == -1) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = VehicleContract.query(db, vehicleId);
//        long id = cursor.getLong(cursor.getColumnIndexOrThrow(VehicleContract.VehicleEntry.COLUMN_NAME_ID));
        String vin = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_VIN);
        String make = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_MAKE);
        String model = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_MODEL);
        String year = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_YEAR);
        String color = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_COLOR);
        String nickname = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_NICKNAME);
        String plateNumber = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER);
        cursor.close();
        db.close();
        return new Vehicle(vin, make, model, year, color, nickname, plateNumber);
    }

    public int updateVehicle(long vehicleId, Vehicle vehicle) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return (int) status;
        }
        if (vehicleId == -1) {
            return -1;
        }
        int numAffected = VehicleContract.update(db, vehicleId, vehicle.getVin(),
                vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getColor(),
                vehicle.getNickname(), vehicle.getPlateNumber());
        db.close();
        return numAffected;
    }

    public long createNewTrip(long vehicleId, Date startTime, Date endTime) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        status = TripContract.insert(db, vehicleId, startTime, endTime);
        db.close();
        setChanged(status);
        return status;
    }

    public long createNewUser(String google_user_id, String firstName, String lastName) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        status = UserContract.insert(db, google_user_id, firstName, lastName);
        db.close();
        return status;
    }

    public boolean containsUser(String google_user_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = UserContract.queryUserID(db, google_user_id);
        db.close();
        if (cursor != null) {
            cursor.close();
            return true;
        }
        return false;
    }

    public List<Long> getAllTripIds() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = TripContract.queryAll(db);
        List<Long> tripIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            tripIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_ID)));
        }
        cursor.close();
        db.close();
        return tripIds;
    }

    /**
     * Returns a map of start times to trip ids. Includes all trip ids in the database.
     */
    public Map<String, Long> getAllTripTimes() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = TripContract.queryAll(db);
        Map<String, Long> tripStartTimes = new HashMap<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_ID));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_START_TIME));
            Date startDate = null;
            try {
                startDate = sqlDateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tripStartTimes.put(readableFormat.format(startDate), id);
        }
        cursor.close();
        db.close();
        return tripStartTimes;
    }

    public List<ObdContent.ObdResponse> getResponsesByTrip(long tripId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ResponseContract.queryByTripId(db, tripId);
        List<ObdContent.ObdResponse> responses = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
            String value = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE));
            responses.add(ObdContent.createItemWithResponse(((Long) id).intValue(), name, value));
        }
        cursor.close();
        db.close();
        return responses;
    }

    public long insertResponse(long tripId, String name, String pId, String value) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        status = ResponseContract.insert(db, tripId, name, pId, value);
        db.close();
        setChanged(status);
        return status;
    }

    public List<ObdContent.ObdResponse> getResponsesById(long[] responseIds) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ResponseContract.queryByIds(db, responseIds);
        List<ObdContent.ObdResponse> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
            String value = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE));
            items.add(ObdContent.createItemWithResponse(((Long) id).intValue(), name, value));
        }
        cursor.close();
        db.close();
        return items;
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public int countObservers() {
        return observers.size();
    }

    @Override
    public void deleteObserver(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void deleteObservers() {
        observers.clear();
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void notifyObservers(Bundle args) {
        if (hasChanged()) {
            for (IObserver observer : observers) {
                observer.update(this, args);
            }
            clearChanged();
        }
    }

    @Override
    public void notifyObservers() {
        notifyObservers(null);
    }

    private void clearChanged() {
        changed = false;
    }

    private void setChanged(long status) {
        if (status > DB_OK) {
            changed = true;
        } else {
            Log.e(TAG, "error occurred when writing: " + status);
        }

    }
}
