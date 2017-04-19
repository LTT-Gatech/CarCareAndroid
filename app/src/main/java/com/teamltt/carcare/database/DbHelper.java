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

package com.teamltt.carcare.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.teamltt.carcare.database.contract.OwnershipContract;
import com.teamltt.carcare.database.contract.ReminderContract;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.database.contract.TripContract;
import com.teamltt.carcare.database.contract.UserContract;
import com.teamltt.carcare.database.contract.VehicleContract;
import com.teamltt.carcare.model.Reminder;
import com.teamltt.carcare.model.Response;
import com.teamltt.carcare.model.Trip;
import com.teamltt.carcare.model.Vehicle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstraction over a SQLiteDatabase to be used by Activities and Fragments.
 * To query the DB, methods here should open a connection to the SQLiteDatabase
 * and query the specific Contract classes with the open connection.
 * In all queries, the SQLiteDatabase connection should be closed. In queries with
 * a Cursor, the Cursor should be closed before the SQLiteDatabase.
 * When possible, this class accepts Models as arguments and returns Models to its caller.
 * The Contract classes should favor interaction with individual arguments over Models.
 * Methods in this class should return List objects instead of a Cursor.
 * If the query is writing, open a writable connection, otherwise use read-only.
 * This class handles upgrades when the DB schema changes. This is handled by
 * the monotonically increasing DATABASE_VERSION constant.
 * This class is an observable subject in the Observer pattern. When the state changes,
 * its observers are notified.
 */
public class DbHelper extends SQLiteOpenHelper implements IObservable {

    private static final String TAG = "DbHelper";

    private static final String SQL_FOREIGN_KEY = "PRAGMA foreign_keys = on;";
    private static final String SQL_INIT = SQL_FOREIGN_KEY;

    // errors are negative, ok is 0, anything else is positive.
    public static final long DB_ERROR_NULL = -6;
    public static final long DB_ERROR_NOT_OPEN = -5;
    public static final long DB_ERROR_READ_ONLY = -4;
    public static final long DB_ERROR_BAD_INPUT = -2;
    public static final long DB_WRITE_ERROR = -1; // from SQLiteDatabase if an error occurred
    public static final long DB_OK = 0;

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "CarCare.db";

    // Format in which the database stores DateTimes. Example: 2004-12-13 13:14:15
    private static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // for observer pattern to notify when data has been updated
    private Set<IObserver> observers = new HashSet<>();
    private boolean changed = false;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // db is being created for the first time.
        // db-wide constraints
        db.execSQL(SQL_INIT);

        // individual table creates
        db.execSQL(OwnershipContract.SQL_CREATE_ENTRIES);
        db.execSQL(ResponseContract.SQL_CREATE_ENTRIES);
        db.execSQL(TripContract.SQL_CREATE_ENTRIES);
        db.execSQL(UserContract.SQL_CREATE_ENTRIES);
        db.execSQL(VehicleContract.SQL_CREATE_ENTRIES);
        db.execSQL(ReminderContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // HACK: lazy man's way is to drop all tables and recreate
        db.execSQL(OwnershipContract.SQL_DROP_ENTRIES);
        db.execSQL(ResponseContract.SQL_DROP_ENTRIES);
        db.execSQL(TripContract.SQL_DROP_ENTRIES);
        db.execSQL(UserContract.SQL_DROP_ENTRIES);
        db.execSQL(VehicleContract.SQL_DROP_ENTRIES);
        db.execSQL(ReminderContract.SQL_DROP_ENTRIES);
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

    /**
     * Helper method to convert from a Date object to a SQLite Date String
     *
     * @param date the Date object
     * @return a SQLite Date String
     */
    public static String convertDate(Date date) {
        if (date == null) {
            return null;
        }
        return sqlDateFormat.format(date);
    }


    /**
     * Helper method which returns a SQLite Date String for the NOW() function
     * @return the NOW() function
     */
    public static String now() {
        // set the format to sql date time
        return sqlDateFormat.format(new Date());
    }

    /**
     * Helper method which builds a multi-argument where clause.
     * returns in the format "COLUMN IN (?,?,?,...,?)" to select an array of arguments.
     * @param column the column to select
     * @param numArgs the number of "?" to append
     * @return a String SQLite "COLUMN IN (?,...,?)" clause
     */
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

    /**
     * A helper method which returns a String column from a cursor
     * @param cursor the cursor
     * @param column the column name
     * @return the String value at the column
     */
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
        if (cursor.moveToFirst()) {
            String vin = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_VIN);
            String make = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_MAKE);
            String model = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_MODEL);
            String year = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_YEAR);
            String color = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_COLOR);
            String nickname = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_NICKNAME);
            String plateNumber = getCursorColumn(cursor, VehicleContract.VehicleEntry.COLUMN_NAME_PLATE_NUMBER);
            cursor.close();
            db.close();
            return new Vehicle(vin, make, model, year, color, nickname, plateNumber);}
        else {
            cursor.close();
            db.close();
            return null;
        }
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

    public long createNewReminder(Reminder reminder)  {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        status = ReminderContract.insert(db, reminder.getVehicleId(), reminder.getName(),
                reminder.getFeatureId(), reminder.getComparisonType(), reminder.getComparisonValue(), reminder.getDate(), false);
        db.close();
        setChanged(status);
        return status;
    }

    public long updateReminder(Reminder reminder) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        if (reminder.getReminderId() < 0) {
            return DB_ERROR_BAD_INPUT;
        }
        int numAffected = ReminderContract.update(db, reminder.getReminderId(), reminder.getVehicleId(),
                reminder.getName(), reminder.getFeatureId(), reminder.getComparisonType(), reminder.getComparisonValue(),
                reminder.getDate(), reminder.isArchived());
        db.close();
        return numAffected;
    }

    //takes a vehicle id, returns a list of all reminders associated with that vehicle
    public List<Reminder> getRemindersByVehicleId(long vehicleId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ReminderContract.queryByVehicleId(db, vehicleId);
        List<Reminder> reminders = new ArrayList<>();
        while (cursor.moveToNext()) {
            long reminderId = cursor.getLong(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_ID));
            String name = getCursorColumn(cursor, ReminderContract.ReminderEntry.COLUMN_NAME_NAME);
            String featureId = getCursorColumn(cursor, ReminderContract.ReminderEntry.COLUMN_NAME_FEATURE_ID);
            int comparison = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_COMPARISON));
            int value = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_VALUE));
            String date = getCursorColumn(cursor, ReminderContract.ReminderEntry.COLUMN_NAME_DATE);
            boolean archived = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_ARCHIVED)) != 0;
            reminders.add(new Reminder(reminderId, vehicleId, name, featureId, comparison, value, date, archived));
        }
        cursor.close();
        db.close();
        return reminders;
    }

    //takes a reminder id (the integer identifier from the table) and returns a Reminder object with the
    //values associated with that reminder
    public Reminder getReminderByReminderId(long reminderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ReminderContract.queryByReminderId(db, reminderId);
        cursor.moveToFirst();
        int vehicleId = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_VEHICLE_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_NAME));
        String featureId = getCursorColumn(cursor, ReminderContract.ReminderEntry.COLUMN_NAME_FEATURE_ID);
        int comparison = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_COMPARISON));
        int value = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_VALUE));
        String date = getCursorColumn(cursor, ReminderContract.ReminderEntry.COLUMN_NAME_DATE);
        boolean archived = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_NAME_ARCHIVED)) != 0;
        Reminder reminder = new Reminder(reminderId, vehicleId, name, featureId, comparison, value, date, archived);
        return reminder;

    }

    public int deleteReminder(long reminderId) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return (int) status;
        }
        if (reminderId < 0) {
            return (int) DB_ERROR_BAD_INPUT;
        }
        int numAffected = ReminderContract.delete(db, reminderId);
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
     * Returns a List of Trip objects
     */
    public List<Trip> getAllTrips() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = TripContract.queryAll(db);
        List<Trip> trips = new ArrayList<>();
        while (cursor.moveToNext()) {
            long tripId = cursor.getLong(cursor.getColumnIndexOrThrow(TripContract.TripEntry.COLUMN_NAME_ID));
            String startTime = getCursorColumn(cursor, TripContract.TripEntry.COLUMN_NAME_START_TIME);
            String endTime = getCursorColumn(cursor, TripContract.TripEntry.COLUMN_NAME_END_TIME);
            Date startDate, endDate;
            try {
                startDate = sqlDateFormat.parse(startTime);
                if (endTime == null) {
                    endDate = sqlDateFormat.parse(startTime);
                } else {
                    endDate = sqlDateFormat.parse(endTime);
                }
                trips.add(new Trip(tripId, startDate, endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        return trips;
    }

    public List<String> getAllNamesInTripId(long tripId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ResponseContract.queryDistinctNamesByTripId(db, tripId);
        List<String> names = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
            names.add(name);
        }
        cursor.close();
        db.close();
        return names;
    }

    public List<Response> getResponsesByTrip(long tripId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ResponseContract.queryByTripId(db, tripId);
        List<Response> responses = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_ID));
            String pId = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_PID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
            String value = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_UNIT));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_TIMESTAMP));
            responses.add(new Response(id, pId, name, value, unit, timestamp));
        }
        cursor.close();
        db.close();
        return responses;
    }

    public long insertResponse(long tripId, Response response) {
        SQLiteDatabase db = getWritableDatabase();
        long status = DbHelper.errorChecks(db);
        if (status != DbHelper.DB_OK) {
            return status;
        }
        status = ResponseContract.insert(db, tripId, response.pId, response.name, response.value, response.unit);
        db.close();
        setChanged(status);
        return status;
    }

    public List<Response> getResponsesByIds(long[] responseIds) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = ResponseContract.queryByIds(db, responseIds);
        List<Response> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_ID));
            String pId = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_PID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
            String value = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_UNIT));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_TIMESTAMP));
            items.add(new Response(id, pId, name, value, unit, timestamp));
        }
        cursor.close();
        db.close();
        return items;
    }

    // Observer pattern. See IObservable and IObserver.
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

    /**
     * Used by notifyObservers
     */
    private void clearChanged() {
        changed = false;
    }

    /**
     * used by query methods to set whether the state has changed.
     * The state has changed if the status is not an error code (>0)
     * @param status The status code.
     */
    private void setChanged(long status) {
        if (status > DB_OK) {
            changed = true;
        } else {
            Log.e(TAG, "error occurred when writing: " + status);
        }
    }


}
