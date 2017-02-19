package com.teamltt.carcare.model;

import java.util.Date;

public class Trip implements Comparable<Trip> {

    private Date startTime, endTime;

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
        return startTime.toString() + " : " + endTime.toString();
    }
}
