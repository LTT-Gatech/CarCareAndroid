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

/**
 * This table serves to connect multiple users to multiple cars.
 * SQLite does not support a list entry so we opted for using a table
 * which encodes the pairs that exists, called ownerships.
 * I believe this is called a pivot table in SQL.
 */
public class OwnershipContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + OwnershipEntry.TABLE_NAME + " ("
            + OwnershipEntry.COLUMN_NAME_USER_ID + " INTEGER,"
            + OwnershipEntry.COLUMN_NAME_VEHICLE_ID + " INTEGER,"
            // FOREIGN KEY(user_id) REFERENCES users(user_id)
            + "FOREIGN KEY(" + OwnershipEntry.COLUMN_NAME_USER_ID + ") REFERENCES "
            + UserContract.UserEntry.TABLE_NAME + "(" + UserContract.UserEntry.COLUMN_NAME_ID + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE,"
            // FOREIGN KEY(vehicle_id) REFERENCES vehicles(vehicle_id)
            + "FOREIGN KEY(" + OwnershipEntry.COLUMN_NAME_VEHICLE_ID + ") REFERENCES "
            + VehicleContract.VehicleEntry.TABLE_NAME + "(" + VehicleContract.VehicleEntry.COLUMN_NAME_ID + ") "
            + "ON DELETE CASCADE ON UPDATE CASCADE"
            + ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + OwnershipEntry.TABLE_NAME;

    // HACK: private to prevent someone from accidentally instantiating a contract
    private OwnershipContract() {
    }

    public static class OwnershipEntry {
        public static final String TABLE_NAME = "ownerships";
        public static final String COLUMN_NAME_USER_ID = UserContract.UserEntry.COLUMN_NAME_ID;
        public static final String COLUMN_NAME_VEHICLE_ID = VehicleContract.VehicleEntry.COLUMN_NAME_ID;
    }
}
