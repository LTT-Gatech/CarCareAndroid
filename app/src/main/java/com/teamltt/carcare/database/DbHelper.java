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

package com.teamltt.carcare.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.teamltt.carcare.database.contract.OwnershipContract;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.database.contract.TripContract;
import com.teamltt.carcare.database.contract.UserContract;
import com.teamltt.carcare.database.contract.VehicleContract;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CarCare.db";

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
}
