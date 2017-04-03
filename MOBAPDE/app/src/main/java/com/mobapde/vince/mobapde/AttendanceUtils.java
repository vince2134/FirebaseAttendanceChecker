package com.mobapde.vince.mobapde;

import android.util.Log;

import com.mobapde.vince.mobapde.Attendance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Bryan on 4/2/2017.
 */

public class AttendanceUtils {

    public static final String ALL_BUILDINGS_FILTER = "ALL";
    private static final String TAG = "AttendanceUtils";

    /**
     * if current date is [April 8, 2017 9:30] and time is 12:45
     * then the resulting date will be [April 8, 2017 12:45]
     * plus it will be converted to milliseconds
     * @param time the time for replacement
     * @return
     */
    public static long replaceCurrentTime(String time){

        String[] hourMinute = time.split(":");

        int hour    = Integer.parseInt(hourMinute[0]);
        int minute  = Integer.parseInt(hourMinute[1]);

        //get current time
        Calendar calendar = Calendar.getInstance();

        //replace the current time by the time provided in the parameter
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hour,
                minute,
                0);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static List<String> getCombinationFilters(Attendance a){
        String rotationId   = a.getRotationId();
        String status       = a.getStatus();
        String building     = a.getBuilding();
        String startTime    = Long.toString(a.getStartTime());
        Log.i(TAG,a.getStartTime() + ", "+startTime);

        if(rotationId == null)
            throw new NullPointerException("rotationId is null");
        if(status == null)
            throw new NullPointerException("status is null");
        if(building == null)
            throw new NullPointerException("rotationId is null");
        if(startTime.equals("0"))
            throw new NullPointerException("rotationId is null");


        List<String> rotationIdFilters = Arrays.asList(rotationId);
        List<String> statusFilters = Arrays.asList(status);
        List<String> buildingFilters = Arrays.asList(building, ALL_BUILDINGS_FILTER);
        List<String> startTimeFilters = Arrays.asList(startTime, "_");

        List<String> combinationFilters = new ArrayList<String>();
        a.setMainCombinationFilter(rotationId + "-" + status + "-" + building + "-" + startTime);

        for (String r : rotationIdFilters)
            for (String s: statusFilters)
                for (String b : buildingFilters)
                    for (String st: startTimeFilters) {
                        combinationFilters.add(r+"-"+s+"-"+b+"-"+st);
                    }

        return combinationFilters;
    }
}
