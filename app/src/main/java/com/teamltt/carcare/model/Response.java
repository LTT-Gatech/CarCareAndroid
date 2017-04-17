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

package com.teamltt.carcare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Response implements Parcelable {
    public long id;
    public String pId;
    public String name;
    public String value;
    public String unit;
    public String timestamp;

    public Response(long id, String pId, String name, String value, String unit, String timestamp) {
        this.id = id;
        this.pId = pId;
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    /**
     * Helper method that combines value and unit.
     *
     * @return value concatenated with unit
     */
    public String getFormattedResult() {
        return value + unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Response)) return false;
        Response that = (Response) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + pId + ") : " + value + unit;
    }

    protected Response(Parcel in) {
        // order is important, must match writeToParcel(Parcel dest, int flags)
        id = in.readLong();
        pId = in.readString();
        name = in.readString();
        value = in.readString();
        unit = in.readString();
        timestamp = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // order is important, must match Response(Parcel in)
        dest.writeLong(id);
        dest.writeString(pId);
        dest.writeString(name);
        dest.writeString(value);
        dest.writeString(unit);
        dest.writeString(timestamp);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Response> CREATOR = new Parcelable.Creator<Response>() {
        @Override
        public Response createFromParcel(Parcel in) {
            return new Response(in);
        }

        @Override
        public Response[] newArray(int size) {
            return new Response[size];
        }
    };
}