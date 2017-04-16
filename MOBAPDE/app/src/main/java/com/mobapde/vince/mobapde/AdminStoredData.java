package com.mobapde.vince.mobapde;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Bryan on 4/9/2017.
 */

public class AdminStoredData {

    //shared preferences
    private ArrayList<Long> initializedDates;

    public AdminStoredData(){
        initializedDates = new ArrayList<Long>();
    }

    public void initialize(){
        addInitializedDate(getDateToday());
    }

    public int getInitializedDateSize(){
        return initializedDates.size();
    }

    public boolean hasInitializedToDate(long date){
        return initializedDates.contains(date);
    }

    public void addInitializedDate(long date) {
        if (!initializedDates.contains(date)) {
            initializedDates.add(date);
        }
    }

    public static long getDateToday(){
        //get current time
        Calendar calendar = Calendar.getInstance();

        //replace the current time by the time provided in the parameter
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0);
        return calendar.getTimeInMillis();
    }
}
