package com.mobapde.vince.mobapde;

import java.util.Calendar;

/**
 * Created by avggo on 3/23/2017.
 */

public class TimeSlot {

    private long startMillis;
    private long endMillis;

    public TimeSlot() {
    }

    public TimeSlot(long startMillis, long endMillis) {
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }


    public Integer withinTimeSlot(){
        long currentTime = Calendar.getInstance().getTimeInMillis();

        if(currentTime >= getStartMillis() && currentTime <= getEndMillis())
            return 0;
        else if(currentTime < getStartMillis())
            return -1;
        else if(currentTime > getEndMillis())
            return 1;

        return null;
    }
}
