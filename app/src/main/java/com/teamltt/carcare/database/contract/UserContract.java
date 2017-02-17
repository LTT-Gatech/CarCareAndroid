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

public class UserContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
            // user_id INTEGER PRIMARY KEY
            UserEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            UserEntry.COLUMN_NAME_FB_API_KEY + " TEXT," +
            UserEntry.COLUMN_NAME_FIRST_NAME + " TEXT," +
            UserEntry.COLUMN_NAME_LAST_NAME + " TEXT" +
            ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;

    // HACK: private to prevent someone from accidentally instantiating a contract
    private UserContract() {
    }

    public static class UserEntry {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_ID = "user_id";
        public static final String COLUMN_NAME_FB_API_KEY = "fb_api_key";
        public static final String COLUMN_NAME_FIRST_NAME = "first_name";
        public static final String COLUMN_NAME_LAST_NAME = "last_name";
    }

}
