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
    public String name;
    public String pId;
    public String value;

    public Response(long id, String name, String pId, String value) {
        this.id = id;
        this.name = name;
        this.pId = pId;
        this.value = value;
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
        return name + " (" + pId + ") : " + value;
    }

    protected Response(Parcel in) {
        id = in.readLong();
        name = in.readString();
        pId = in.readString();
        value = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(pId);
        dest.writeString(value);
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
