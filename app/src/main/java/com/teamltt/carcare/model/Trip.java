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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Trip implements Comparable<Trip> {

    private Date startTime, endTime;
    // Format in which dates are displayed to the user. Example: Tue 07/14/02, 21:40
    private static final SimpleDateFormat readableFormat = new SimpleDateFormat("EEE MM/dd/yy, HH:mm");

    public Trip(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Trip)) {
            return false;
        }
        Trip that = (Trip) o;
        return this.startTime.equals(that.startTime) && this.endTime.equals(that.endTime);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(Trip trip) {
        int c = this.startTime.compareTo(trip.startTime);
        if (c == 0) {
            return this.endTime.compareTo(trip.endTime);
        }
        return c;
    }

    @Override
    public String toString() {
        return readableFormat.format(startTime) + " : " + readableFormat.format(endTime);
    }
}
